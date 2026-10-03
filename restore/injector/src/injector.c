/* injector.c — ptrace 注入器，同时支持 x86_64 与 AArch64。
 *
 * 用法:
 *     injector -pkg <包名> -lib <目标 .so 的绝对路径>
 *     injector -pid <PID>  -lib <目标 .so 的绝对路径>
 *
 * 关键前提（本机实测得出）:
 *   注入器必须与「目标进程在内核眼里的架构」一致。
 *   本机 MuMu 是 aarch64 内核 + x86_64 userspace + houdini 跑 ARM 游戏，
 *   游戏进程在内核眼里是 x86_64，所以必须用 x86_64 版注入器，
 *   用 AArch64 版会得到 PTRACE_GETREGSET = EINVAL。
 *   本工具会自动挑选与自己架构相同的 libc / libdl。
 *
 * 与原始 assets/64 的差异（有意为之）:
 *   - 不执行 `su -c setenforce 0`，不改动整机 SELinux。
 *   - 不做 /dev/socket/zygote 兜底，失败就明确报错。
 *
 * 原理:
 *   1. 按包名或 pid 找到目标进程
 *   2. 读 /proc/<pid>/maps，挑出与本工具同架构的 libc.so / libdl.so
 *   3. 从磁盘上该库的 .dynsym 解析 mmap / dlopen / dlerror 的偏移
 *   4. PTRACE_ATTACH + 保存寄存器现场
 *   5. 远程 mmap 出「路径缓冲 + 调用栈」
 *   6. 写入 .so 路径
 *   7. 远程 dlopen(路径, RTLD_NOW)  —— 返回地址设为 0，靠 SIGSEGV 判定调用结束
 *   8. 恢复寄存器 + DETACH（不杀进程）
 *
 * 编译:
 *   x86_64-linux-android21-clang  -O2 -o injector_x64   injector.c
 *   aarch64-linux-android21-clang -O2 -o injector_arm64 injector.c
 *
 * 仅用于你拥有或获授权的环境。
 */
#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <signal.h>
#include <sys/ptrace.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/mman.h>
#include <sys/uio.h>
#include <elf.h>
#include <stdint.h>
#include <sys/syscall.h>

/* ------------------------------------------------------------------ */
/* 架构抽象                                                            */
/* ------------------------------------------------------------------ */

#if defined(__x86_64__)

#define ARCH_NAME "x86_64"
#define GREGSET_SIZE 216            /* sizeof(struct user_regs_struct) */

#ifndef PTRACE_GETREGS
#define PTRACE_GETREGS 12
#endif

typedef struct {
    unsigned long r15, r14, r13, r12, rbp, rbx;
    unsigned long r11, r10, r9, r8, rax, rcx, rdx, rsi, rdi;
    unsigned long orig_rax, rip, cs, eflags, rsp, ss;
    unsigned long fs_base, gs_base, ds, es, fs, gs;
} regs_t;

/* x86_64 System V: 参数依次 rdi, rsi, rdx, rcx, r8, r9
 * mmap 有 6 个参数，第 5、6 个（fd / offset）必须显式设置，
 * 否则会沿用调用者的垃圾寄存器值，导致 mmap 直接返回 MAP_FAILED。 */
static void set_args6(regs_t *r, unsigned long a0, unsigned long a1,
                      unsigned long a2, unsigned long a3,
                      unsigned long a4, unsigned long a5)
{
    r->rdi = a0;
    r->rsi = a1;
    r->rdx = a2;
    r->rcx = a3;
    r->r8  = a4;
    r->r9  = a5;
    /* 必须清掉这些：orig_rax 若残留系统调用号，内核会把返回当成 syscall
     * 退出路径处理，rax 会被覆盖成垃圾值；rax 也一并清零便于判定。 */
    r->orig_rax = (unsigned long)-1;
    r->rax      = 0;
}

static void set_pc(regs_t *r, unsigned long pc) { r->rip = pc; }
static void set_sp(regs_t *r, unsigned long sp) { r->rsp = sp; }
static unsigned long get_sp(const regs_t *r)    { return r->rsp; }
static unsigned long get_ret(const regs_t *r)   { return r->rax; }
static unsigned long get_pc(const regs_t *r)    { return r->rip; }

#elif defined(__aarch64__)

#define ARCH_NAME "AArch64"
#define GREGSET_SIZE 272            /* sizeof(struct user_pt_regs) = 34*8 */

typedef struct { unsigned long regs[34]; } regs_t;   /* x0..x30, sp, pc, pstate */

#ifndef PTRACE_GETREGS
#define PTRACE_GETREGS 12
#endif

static void set_args6(regs_t *r, unsigned long a0, unsigned long a1,
                      unsigned long a2, unsigned long a3,
                      unsigned long a4, unsigned long a5)
{
    r->regs[0] = a0;
    r->regs[1] = a1;
    r->regs[2] = a2;
    r->regs[3] = a3;
    r->regs[4] = a4;
    r->regs[5] = a5;
}

static void set_pc(regs_t *r, unsigned long pc) { r->regs[32] = pc; }
static void set_sp(regs_t *r, unsigned long sp) { r->regs[31] = sp; }
static unsigned long get_sp(const regs_t *r)    { return r->regs[31]; }
static unsigned long get_ret(const regs_t *r)   { return r->regs[0]; }
static unsigned long get_pc(const regs_t *r)    { return r->regs[32]; }

/* aarch64: 返回地址放 LR(x30) */
static void set_lr(regs_t *r, unsigned long lr) { r->regs[30] = lr; }

#else
#error "只支持 x86_64 与 AArch64"
#endif

/* ------------------------------------------------------------------ */
/* 通用小工具                                                          */
/* ------------------------------------------------------------------ */

struct iovl { void *base; size_t len; };

/* dlopen 的 flag。不 include <dlfcn.h> 以免和自实现的远程调用混淆，
 * RTLD_NOW = 2 在 bionic 上是稳定 ABI。 */
#ifndef RTLD_NOW
#define RTLD_NOW 2
#endif

#ifndef SYS_process_vm_readv
#define SYS_process_vm_readv 270
#endif
#ifndef SYS_process_vm_writev
#define SYS_process_vm_writev 271
#endif

static void die(const char *what)
{
    fprintf(stderr, "[-] %s: %s\n", what, strerror(errno));
    exit(1);
}

static int read_mem(pid_t pid, unsigned long addr, void *out, size_t len)
{
    struct iovl l = { out, len }, r = { (void *)addr, len };
    if (syscall(SYS_process_vm_readv, pid, &l, 1UL, &r, 1UL, 0UL) == (ssize_t)len)
        return 0;
    /* 回退 PTRACE_PEEKDATA */
    size_t done = 0;
    while (done < len) {
        errno = 0;
        long w = ptrace(PTRACE_PEEKDATA, pid, (void *)(addr + done), NULL);
        if (errno)
            return -1;
        size_t n = len - done < sizeof(w) ? len - done : sizeof(w);
        memcpy((char *)out + done, &w, n);
        done += sizeof(w);
    }
    return 0;
}

static int write_mem(pid_t pid, unsigned long addr, const void *in, size_t len)
{
    struct iovl l = { (void *)in, len }, r = { (void *)addr, len };
    if (syscall(SYS_process_vm_writev, pid, &l, 1UL, &r, 1UL, 0UL) == (ssize_t)len)
        return 0;
    size_t done = 0;
    while (done < len) {
        long w;
        size_t n = len - done < sizeof(w) ? len - done : sizeof(w);
        if (n < sizeof(w)) {
            if (read_mem(pid, addr + done, &w, sizeof(w)) < 0)
                return -1;
        }
        memcpy(&w, (const char *)in + done, n);
        if (ptrace(PTRACE_POKEDATA, pid, (void *)(addr + done), (void *)w) < 0)
            return -1;
        done += sizeof(w);
    }
    return 0;
}

/* ------------------------------------------------------------------ */
/* 寄存器存取（必须传 iovec，传裸指针会 EINVAL）                        */
/* ------------------------------------------------------------------ */

static int get_regs(pid_t pid, regs_t *out)
{
    struct iovl iov = { out, sizeof(*out) };
    return ptrace(PTRACE_GETREGSET, pid, (void *)NT_PRSTATUS, &iov);
}

static int set_regs(pid_t pid, const regs_t *in)
{
    struct iovl iov = { (void *)in, sizeof(*in) };
    return ptrace(PTRACE_SETREGSET, pid, (void *)NT_PRSTATUS, &iov);
}

/* ------------------------------------------------------------------ */
/* 读 ELF 头，判断架构是否与本工具一致                                  */
/* ------------------------------------------------------------------ */

static int elf_matches_self(const char *path)
{
    int fd = open(path, O_RDONLY);
    if (fd < 0)
        return 0;
    unsigned char eh[64];
    ssize_t n = read(fd, eh, sizeof(eh));
    close(fd);
    if (n < 20 || memcmp(eh, ELFMAG, SELFMAG) != 0)
        return 0;
    uint16_t machine = (uint16_t)(eh[18] | (eh[19] << 8));
#if defined(__x86_64__)
    return machine == EM_X86_64;
#else
    return machine == EM_AARCH64;
#endif
}

/* ------------------------------------------------------------------ */
/* 从 /proc/<pid>/maps 找模块：返回基址，并输出路径                     */
/* ------------------------------------------------------------------ */

static unsigned long find_module(pid_t pid, const char *name, char *out, size_t outlen)
{
    char path[64], line[2048];
    snprintf(path, sizeof(path), "/proc/%d/maps", pid);
    FILE *fp = fopen(path, "r");
    if (!fp)
        return 0;

    unsigned long base = 0;
    char best[1024] = {0};
    while (fgets(line, sizeof(line), fp)) {
        unsigned long start, end;
        char perms[8], offset[32], dev[16];
        unsigned long inode;
        char pathname[1024] = {0};
        int k = sscanf(line, "%lx-%lx %7s %31s %15s %lu %1023s",
                       &start, &end, perms, offset, dev, &inode, pathname);
        if (k < 7)
            continue;
        const char *bn = strrchr(pathname, '/');
        bn = bn ? bn + 1 : pathname;
        if (strcmp(bn, name) != 0)
            continue;
        if (strcmp(offset, "00000000") != 0)
            continue;
        /* 只挑与本工具同架构的那一份（apex 的 x86_64 vs arm64/ 的 AArch64） */
        if (!elf_matches_self(pathname))
            continue;
        base = start;
        snprintf(best, sizeof(best), "%s", pathname);
        break;
    }
    fclose(fp);
    if (out && outlen)
        snprintf(out, outlen, "%s", best);
    return base;
}

/* ------------------------------------------------------------------ */
/* 按包名找 PID                                                        */
/* ------------------------------------------------------------------ */

static pid_t pid_of_pkg(const char *pkg)
{
    DIR *d = opendir("/proc");
    if (!d)
        return -1;
    struct dirent *e;
    pid_t found = -1;
    while ((e = readdir(d))) {
        if (e->d_name[0] < '0' || e->d_name[0] > '9')
            continue;
        char path[64], buf[512] = {0};
        snprintf(path, sizeof(path), "/proc/%s/cmdline", e->d_name);
        int fd = open(path, O_RDONLY);
        if (fd < 0)
            continue;
        ssize_t n = read(fd, buf, sizeof(buf) - 1);
        close(fd);
        if (n <= 0)
            continue;
        if (strcmp(buf, pkg) == 0) {
            pid_t p = (pid_t)atoi(e->d_name);
            if (found < 0)
                found = p;
            /* 优先选有 libil2cpp 映射的那个（游戏主进程） */
            char tmp[1024];
            if (find_module(p, "libil2cpp.so", tmp, sizeof(tmp))) {
                found = p;
                break;
            }
        }
    }
    closedir(d);
    return found;
}

/* ------------------------------------------------------------------ */
/* 从磁盘 ELF 的 .dynsym 解析符号 st_value                             */
/* ------------------------------------------------------------------ */

static unsigned long elf_symbol_offset(const char *libpath, const char *sym)
{
    int fd = open(libpath, O_RDONLY);
    if (fd < 0)
        return 0;

    Elf64_Ehdr eh;
    if (pread(fd, &eh, sizeof(eh), 0) != (ssize_t)sizeof(eh) ||
        memcmp(eh.e_ident, ELFMAG, SELFMAG) != 0) {
        close(fd);
        return 0;
    }
    if (eh.e_shnum == 0 || eh.e_shoff == 0) {
        close(fd);
        return 0;
    }

    Elf64_Shdr *sh = calloc(eh.e_shnum, sizeof(Elf64_Shdr));
    if (!sh) { close(fd); return 0; }
    if (pread(fd, sh, eh.e_shnum * sizeof(Elf64_Shdr), eh.e_shoff)
            != (ssize_t)(eh.e_shnum * sizeof(Elf64_Shdr))) {
        free(sh); close(fd); return 0;
    }

    unsigned long result = 0;
    for (int i = 0; i < eh.e_shnum && !result; i++) {
        if (sh[i].sh_type != SHT_DYNSYM || sh[i].sh_entsize == 0)
            continue;
        size_t count = sh[i].sh_size / sizeof(Elf64_Sym);
        Elf64_Sym *syms = calloc(count, sizeof(Elf64_Sym));
        if (!syms)
            break;
        if (pread(fd, syms, sh[i].sh_size, sh[i].sh_offset) == (ssize_t)sh[i].sh_size) {
            Elf64_Shdr *strh = &sh[sh[i].sh_link];
            char *strs = malloc(strh->sh_size + 1);
            if (strs && pread(fd, strs, strh->sh_size, strh->sh_offset)
                        == (ssize_t)strh->sh_size) {
                strs[strh->sh_size] = 0;
                for (size_t k = 0; k < count; k++) {
                    if (syms[k].st_name &&
                        strcmp(strs + syms[k].st_name, sym) == 0) {
                        result = syms[k].st_value;
                        break;
                    }
                }
            }
            free(strs);
        }
        free(syms);
    }
    free(sh);
    close(fd);
    return result;
}

/* ------------------------------------------------------------------ */
/* 远程调用                                                            */
/* ------------------------------------------------------------------ */

/* 在目标进程执行 fn(a0..a3)。
 * 用自己 mmap 出来的栈，栈顶写 0 作为返回地址 —— 函数返回时跳到 0 触发
 * SIGSEGV，我们借此判定调用结束。
 *
 * scratch == 0 表示「还没有可用缓冲」，此时借用目标进程自己的栈
 * （往下让 0x8000 字节）。第一次 mmap 必须走这条路，否则就是先有鸡
 * 还是先有蛋：没有 mmap 就没有 scratch，没有 scratch 又没法调 mmap。 */
static unsigned long remote_call6(pid_t pid, unsigned long fn,
                                  unsigned long a0, unsigned long a1,
                                  unsigned long a2, unsigned long a3,
                                  unsigned long a4, unsigned long a5,
                                  unsigned long scratch, size_t scratch_len,
                                  const regs_t *saved)
{
    regs_t r = *saved;
    set_args6(&r, a0, a1, a2, a3, a4, a5);
    set_pc(&r, fn);

    unsigned long stack_top;
    if (scratch && scratch_len)
        stack_top = (scratch + scratch_len) & ~0xFUL;
    else
        stack_top = (get_sp(saved) - 0x8000) & ~0xFUL;

#if defined(__x86_64__)
    /* x86_64: 栈顶放返回地址 0 */
    unsigned long sp = stack_top - 8;
    unsigned long zero = 0;
    if (write_mem(pid, sp, &zero, sizeof(zero)) < 0)
        return 0;
    set_sp(&r, sp);
#else
    /* aarch64: 返回地址放 LR */
    set_sp(&r, stack_top);
    set_lr(&r, 0);
#endif

    if (set_regs(pid, &r) < 0)
        return 0;
    if (ptrace(PTRACE_CONT, pid, NULL, NULL) < 0)
        return 0;

    int status;
    waitpid(pid, &status, 0);

    regs_t after;
    if (get_regs(pid, &after) < 0)
        return 0;
    return get_ret(&after);
}

/* 4 参数版本；多余的两个参数位清零，避免脏寄存器影响被调函数。 */
static unsigned long remote_call(pid_t pid, unsigned long fn,
                                 unsigned long a0, unsigned long a1,
                                 unsigned long a2, unsigned long a3,
                                 unsigned long scratch, size_t scratch_len,
                                 const regs_t *saved)
{
    return remote_call6(pid, fn, a0, a1, a2, a3, 0, 0,
                        scratch, scratch_len, saved);
}

/* ------------------------------------------------------------------ */

static void usage(const char *a0)
{
    fprintf(stderr,
        "用法:\n"
        "  %s -pkg <包名> -lib <SO 绝对路径>\n"
        "  %s -pid <PID>  -lib <SO 绝对路径>\n"
        "\n"
        "例:\n"
        "  %s -pkg com.nerversoft.ark.recode -lib /data/local/tmp/agent.so\n"
        "\n"
        "本工具架构: %s\n", a0, a0, a0, ARCH_NAME);
    exit(2);
}

int main(int argc, char **argv)
{
    const char *pkg = NULL, *lib = NULL;
    pid_t pid = -1;

    for (int i = 1; i < argc; i++) {
        if (!strncmp(argv[i], "-pkg=", 5))
            pkg = argv[i] + 5;
        else if (!strcmp(argv[i], "-pkg") && i + 1 < argc)
            pkg = argv[++i];
        else if (!strncmp(argv[i], "-pid=", 5))
            pid = (pid_t)atoi(argv[i] + 5);
        else if (!strcmp(argv[i], "-pid") && i + 1 < argc)
            pid = (pid_t)atoi(argv[++i]);
        else if (!strncmp(argv[i], "-lib=", 5))
            lib = argv[i] + 5;
        else if (!strcmp(argv[i], "-lib") && i + 1 < argc)
            lib = argv[++i];
        else
            usage(argv[0]);
    }
    if (!lib || (!pkg && pid <= 0))
        usage(argv[0]);

    printf("[*] 注入器架构: %s\n", ARCH_NAME);

    if (pid <= 0) {
        printf("[*] 按包名查找进程: %s\n", pkg);
        pid = pid_of_pkg(pkg);
        if (pid <= 0) {
            fprintf(stderr, "[-] 找不到进程 %s，游戏启动了吗？\n", pkg);
            return 1;
        }
    }
    printf("[+] PID: %d\n", pid);

    /* 等 libil2cpp 映射（加固壳解密完成） */
    char gamepath[1024] = {0};
    unsigned long game_base = 0;
    for (int i = 0; i < 80; i++) {
        game_base = find_module(pid, "libil2cpp.so", gamepath, sizeof(gamepath));
        if (game_base)
            break;
        usleep(250 * 1000);
    }
    if (!game_base)
        fprintf(stderr, "[!] 未发现 libil2cpp.so 映射（可能还没解密完），继续尝试注入\n");
    else
        printf("[+] libil2cpp.so @ 0x%lx\n", game_base);

    /* 找同架构的 libc / libdl / linker */
    char libc_path[1024] = {0}, libdl_path[1024] = {0}, linker_path[1024] = {0};
    unsigned long libc_base = find_module(pid, "libc.so", libc_path, sizeof(libc_path));
    unsigned long libdl_base = find_module(pid, "libdl.so", libdl_path, sizeof(libdl_path));
    unsigned long linker_base = find_module(pid, "linker64", linker_path, sizeof(linker_path));
    if (!libc_base) {
        fprintf(stderr, "[-] 在目标进程里找不到与本工具同架构(%s)的 libc.so\n", ARCH_NAME);
        fprintf(stderr, "    提示：注入器架构必须与目标进程在内核眼里的架构一致\n");
        return 1;
    }
    printf("[+] libc.so   = %s @ 0x%lx\n", libc_path, libc_base);
    if (libdl_base)
        printf("[+] libdl.so  = %s @ 0x%lx\n", libdl_path, libdl_base);
    if (linker_base)
        printf("[+] linker64  = %s @ 0x%lx\n", linker_path, linker_base);

    /* mmap 在 libc 里 */
    unsigned long fn_mmap = 0, fn_dlopen = 0, fn_dlerr = 0;
    unsigned long off_mmap = elf_symbol_offset(libc_path, "mmap");
    if (off_mmap)
        fn_mmap = libc_base + off_mmap;

    /* dlopen：必须用 linker64 的 __loader_dlopen。
     * libdl.so 里的 dlopen 只是 9 字节 PLT 跳板，labelled UND —— 真正的实现在
     * linker 里。远程调用跳板时拿不到正确的 linker 上下文，会返回垃圾值。 */
    if (linker_base) {
        unsigned long o = elf_symbol_offset(linker_path, "__loader_dlopen");
        if (o) {
            fn_dlopen = linker_base + o;
            printf("[+] dlopen = linker64 __loader_dlopen (+0x%lx)\n", o);
        }
        unsigned long oe = elf_symbol_offset(linker_path, "__loader_dlerror");
        if (oe)
            fn_dlerr = linker_base + oe;
    }
    /* 退路：普通 dlopen（老 Android 或非 bionic 环境） */
    if (!fn_dlopen) {
        const char *cands[2];
        int nc = 0;
        if (libdl_base && libdl_path[0])
            cands[nc++] = libdl_path;
        cands[nc++] = libc_path;
        for (int i = 0; i < nc && !fn_dlopen; i++) {
            unsigned long o = elf_symbol_offset(cands[i], "dlopen");
            if (!o)
                continue;
            unsigned long base = (cands[i] == libdl_path) ? libdl_base : libc_base;
            fn_dlopen = base + o;
            printf("[+] dlopen = 退路 %s (+0x%lx)\n", cands[i], o);
            unsigned long oe = elf_symbol_offset(cands[i], "dlerror");
            if (oe)
                fn_dlerr = base + oe;
        }
    }

    if (!fn_mmap || !fn_dlopen) {
        fprintf(stderr, "[-] 无法解析 mmap/dlopen（mmap=%d dlopen=%d）\n",
                fn_mmap ? 1 : 0, fn_dlopen ? 1 : 0);
        return 1;
    }
    printf("[+] mmap   = 0x%lx\n", fn_mmap);
    printf("[+] dlopen = 0x%lx\n", fn_dlopen);

    /* 检查目标 so */
    {
        int fd = open(lib, O_RDONLY);
        if (fd < 0) {
            fprintf(stderr, "[-] 打不开 %s: %s\n", lib, strerror(errno));
            return 1;
        }
        close(fd);
        if (!elf_matches_self(lib)) {
            fprintf(stderr, "[!] 警告: %s 的架构与注入器不一致，dlopen 很可能失败\n", lib);
        }
    }

    /* ATTACH */
    if (ptrace(PTRACE_ATTACH, pid, NULL, NULL) < 0)
        die("PTRACE_ATTACH 失败（需要 root，且目标未被其他调试器附加）");
    int status;
    waitpid(pid, &status, 0);
    printf("[+] PTRACE_ATTACH OK\n");

    regs_t saved;
    if (get_regs(pid, &saved) < 0) {
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        die("PTRACE_GETREGSET 失败（多半是注入器架构与目标进程不匹配）");
    }
    printf("[+] 已保存寄存器现场 (pc=0x%lx)\n", get_pc(&saved));

    /* 远程 mmap 一块写字区 + 栈区 */
    /* mmap 6 个参数: addr, length, prot, flags, fd, offset
     * fd 必须是 -1，offset 必须是 0（匿名映射）
     * prot 含 PROT_EXEC：我们要在这块内存上放调用桩并执行它 */
    unsigned long scratch = remote_call6(pid, fn_mmap,
                                         0, 0x10000,
                                         PROT_READ | PROT_WRITE | PROT_EXEC,
                                         MAP_PRIVATE | MAP_ANONYMOUS,
                                         (unsigned long)-1, 0,
                                         0, 0, &saved);  /* scratch=0: 借目标进程自己的栈 */
    if (!scratch || scratch == (unsigned long)-1) {
        fprintf(stderr, "[-] 远程 mmap 失败: 0x%lx\n", scratch);
        set_regs(pid, &saved);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }
    printf("[+] 远程内存 = 0x%lx\n", scratch);

    /* 写路径 */
    size_t plen = strlen(lib) + 1;
    if (write_mem(pid, scratch, lib, plen) < 0) {
        fprintf(stderr, "[-] 写入路径失败\n");
        set_regs(pid, &saved);
        ptrace(PTRACE_DETACH, pid, NULL, NULL);
        return 1;
    }
    printf("[+] 路径已写入: %s\n", lib);

    /* 远程 dlopen，栈用后半段 */
    unsigned long handle = remote_call(pid, fn_dlopen,
                                       scratch, RTLD_NOW, 0, 0,
                                       scratch + 0x2000, 0x2000, &saved);
    printf("[+] dlopen 返回 = 0x%lx\n", handle);

    if (!handle && fn_dlerr) {
        unsigned long errp = remote_call(pid, fn_dlerr, 0, 0, 0, 0,
                                         scratch + 0x2000, 0x2000, &saved);
        if (errp) {
            char msg[512] = {0};
            if (read_mem(pid, errp, msg, sizeof(msg) - 1) == 0 && msg[0])
                fprintf(stderr, "[-] dlerror: %s\n", msg);
        }
    }

    if (set_regs(pid, &saved) < 0)
        fprintf(stderr, "[!] 恢复寄存器失败，目标进程可能异常\n");
    if (ptrace(PTRACE_DETACH, pid, NULL, NULL) < 0)
        die("PTRACE_DETACH 失败");

    if (handle) {
        printf("[+] 注入成功\n");
        return 0;
    }
    fprintf(stderr, "[-] 注入失败：dlopen 返回 NULL\n");
    return 1;
}
