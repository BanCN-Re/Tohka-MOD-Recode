# 模块还原 · 注入器（assets/64）

对 `assets/64`（14,120 B，AArch64 ELF 可执行文件）的行为还原，
以及我用 C 重写的等价实现。

---

## 一、文件身份

| 项 | 值 |
| --- | --- |
| 大小 | 14,120 B |
| 类型 | ELF64，**ET_EXEC**（可执行文件，不是共享库） |
| 架构 | AArch64 (EM_AARCH64, 0xB7) |
| 链接 | **动态链接**（有 `.interp` → `/system/bin/linker64`、`.dynsym`） |
| 导出 | 1 个：`main` @ 0x1228（652 B） |
| 导入 | 39 个 |
| 字符串 | 112 条 |
| 段 | `.interp` `.hash` `.dynsym` `.dynstr` `.rela.dyn` `.rela.plt` `.plt` `.text` `.rodata` `.eh_frame` `.preinit_array` `.init_array` `.fini_array` `.dynamic` `.got` |

**关键：它是动态链接的。** 这一点很重要 —— 用 `-static` 编出来的同类工具
在 MuMu 的 houdini 翻译层下会启动即 SIGSEGV（实测）。

---

## 二、导入符号（39 个，按用途分组）

### 2.1 进程操控（注入核心）
```
ptrace          ← 附加/读写寄存器/读写内存
waitpid         ← 等目标进程状态变化
mmap            ← 在目标进程里分配内存（通过远程调用）
```

### 2.2 动态加载
```
dlopen          ← 在目标进程里加载 .so
dlerror         ← 取错误信息
```

### 2.3 文件与目录（找进程）
```
fopen / fclose / fgets / fflush
opendir / readdir / closedir
access          ← 检查文件是否存在/可读
```

### 2.4 字符串与内存
```
memcpy / memset
strcmp / strncmp / strlen / strstr
sprintf / snprintf / sscanf / atoi
```

### 2.5 网络（备用注入路径）
```
socket / connect    ← 连 /dev/socket/zygote 的备用通道
```

### 2.6 libc 基础设施
```
__libc_init / __cxa_atexit / __errno / __sF
__stack_chk_fail
perror / printf / puts / putchar
```

### 2.7 其它
```
system          ← 执行 shell（用来跑 su）
sleep
```

---

## 三、命令行接口

从字符串还原，**三种写法都吃**：

```
%s -pid 8678 -lib /storage/emulated/0/libfrida64.so
%s -pkg com.amy.virtual -lib /storage/emulated/0/libfrida64.so
%s -pkg=com.amy.virtual -lib=/storage/emulated/0/libfrida64.so
```

即：
- `-pid <N>` 或 `-pid=<N>` —— 直接指定目标 PID
- `-pkg <包名>` 或 `-pkg=<包名>` —— 按包名找进程
- `-lib <路径>` 或 `-lib=<路径>` —— 要注入的 .so

提示字符串：
```
%s -pid <
%s -pkg <
>       : 
> -lib <SO
ID>     : 
ID> -lib <SO
```

（这些是 usage 输出的片段拼接）

---

## 四、注入流程（从字符串与导入符号还原）

```
1. 执行 su -c setenforce 0
     字符串： "su -c setenforce 0"
     作用：   把整机 SELinux 关成宽容模式（全局副作用）

2. 定位目标进程
     - 若给了 -pid：直接用
     - 若给了 -pkg：读 /proc/<pid>/cmdline 逐个比对
       字符串： "/proc/%d/cmdline"  "/proc/self/cmdline"
     打印：   "PID: %d"

3. 从 /proc/<pid>/maps 找模块基址
     字符串： "/proc/%d/maps"  "/proc/self/maps"
             "/libdl.so"  "libc.so"
             "%p-%*p %4s"        ← maps 行的解析格式
     失败信息： "[-] get local module base failed"
               "[-] get remote module base failed"
     成功：    "[+] DLOPEN_FUN = %p"
               "[+] MMAP_FUN = %p"
               "[+] DLERROR_FUN = %p"

4. PTRACE_ATTACH
     成功： "[+] PTRACE_ATTACH OK"
     失败： "[-] PTRACE_ATTACH FAILED:[%s]"

5. PTRACE_GETREGS 保存现场
     成功： "[+] PTRACE_GETREGS OK"
     失败： "[-] PTRACE_GETREGS FAILED:[%s]"

6. 远程 mmap 一块内存
     成功： "[+] MMAP_RESULT = %p"
     失败： "[-] REMOTE_CALL_MMAP FAILED:[%s]"

7. 远程写内存（把 .so 路径塞进去）
     成功： "[+] REMOTE_WRITE OK"
     失败： "[-] REMOTE_WRITE FAILED:[%s]"

8. 远程调用 dlopen
     成功： "[+] DLOPEN_RESULT = %p"
     失败： "[-] REMOTE_CALL_DLERROR FAILED"
           "[-] GET_DLERROR_FUN FAILED"
           "[-] GET_DLOPEN_FUN FAILED"
           "[-] SO"      ← 路径相关错误
           "[-] "        ← 通用错误前缀

9. 读回 dlerror（失败时）
     "[+] DLERROR_RESULT = %s"

10. (推测) 恢复寄存器 + DETACH
```

**日志前缀规律**：`[+]` 成功、`[-]` 失败、`[*]` 进度。这套日志风格在
`PTRACE_INJECT` 这个名字上也能看到（应该是程序自己的标识字符串）。

---

## 五、备用注入路径

字符串里有一条 zygote 相关：

```
/dev/socket/zygote
zygote64
```

以及 `.rodata` 里的 `socket` / `connect` 导入。

这说明它有一个**通过 zygote socket 的备用注入路径** ——
不走 ptrace，而是连到 zygote 的 unix socket 让 zygote fork 出进程。
这条路径影响面更大（动的是 zygote），我的复刻里**没有实现**。

---

## 六、全部 112 条字符串

### 6.1 用法与提示
```
%s -pid 8678 -lib /storage/emulated/0/libfrida64.so
%s -pkg com.amy.virtual -lib /storage/emulated/0/libfrida64.so
%s -pkg=com.amy.virtual -lib=/storage/emulated/0/libfrida64.so
%s -pid <
%s -pkg <
-lib <SO
-pid <
-pkg <
-lib
-lib=
-pid
-pid=
-pkg
-pkg=
```

注意示例里用的是 **frida64**（`libfrida64.so`）—— 说明这个注入器是通用的，
原作者也用它注 frida。

### 6.2 日志
```
PTRACE_INJECT
[+] DLERROR_FUN = %p
[+] DLERROR_RESULT = %s
[+] DLOPEN_FUN = %p
[+] DLOPEN_RESULT = %p
[+] MMAP_FUN = %p
[+] MMAP_RESULT = %p
[+] PTRACE_ATTACH OK
[+] PTRACE_GETREGS OK
[+] REMOTE_WRITE OK
[-] GET_DLERROR_FUN FAILED
[-] GET_DLOPEN_FUN FAILED
[-] GET_MMAP_FUN FAILED
[-] PTRACE_ATTACH FAILED:[%s]
[-] PTRACE_GETREGS FAILED:[%s]
[-] REMOTE_CALL_DLERROR FAILED
[-] REMOTE_CALL_MMAP FAILED:[%s]
[-] REMOTE_WRITE FAILED:[%s]
[-] SO
[-] fgets errno:[%s]
[-] fopen:[%s], errno:[%s]
[-] get local module base failed
[-] get remote module base failed
PID: %d
[pid:%d] 
[%s] 
```

### 6.3 路径与文件
```
/proc
/proc/%d/cmdline
/proc/%d/maps
/proc/self/cmdline
/proc/self/maps
/dev/socket/zygote
/libdl.so
libc.so
libdl.so
liblog.so
libm.so
libstdc++.so
lib64
zygote64
```

### 6.4 其它
```
__FINI_ARRAY__
__INIT_ARRAY__
__PREINIT_ARRAY__
__bss_start
_edata
_end
LIBC
:%s
%p-%*p %4s
su -c setenforce 0
```

---

## 七、我的复刻（injector.c）与原件对照

源码：`restore/injector/src/injector.c`

| 项 | 原件 assets/64 | 我的复刻 | 说明 |
| --- | --- | --- | --- |
| 架构 | AArch64 | AArch64 + **x86_64 双版本** | 因为注入器架构必须匹配目标进程 |
| 链接方式 | 动态 | 动态（踩过坑后改的） | 静态版在 houdini 下崩溃 |
| 命令行 | 三种写法 | 三种写法 | 完全兼容 |
| `setenforce 0` | **执行** | **去掉** | 有意移除，不改整机状态 |
| zygote 备用路径 | 有 | 无 | 有意不做 |
| 远程 mmap/dlopen | 有 | 有 | 核心逻辑一致 |
| 寄存器保存/恢复 | 有 | 有 | 且正确处理 iovec |
| 错误处理 | 部分 | 更完整 | 加 dlerror 读取 |

### 7.1 复刻过程中发现的必修坑

1. **必须动态链接** —— `-static` 编出的 AArch64 二进制在 houdini 翻译层下
   启动即 SIGSEGV（崩在 `anon:Mem_0x20000000`）

2. **`process_vm_readv/writev` bionic 不导出** —— 要走
   `syscall(SYS_process_vm_readv, ...)`（号 270/271）

3. **`PTRACE_GETREGSET` 必须传 iovec** —— 传裸指针一定 `EINVAL`

4. **库路径不能硬编码** —— 翻译层下真实库是 `/system/lib64/arm64/libc.so`，
   必须从目标进程的 `/proc/<pid>/maps` 里读

5. **`dlopen` 在 libdl 里只是跳板** —— 真身在 linker64 的 `__loader_dlopen`
   （但即使找对了，在 houdini 下仍不可用，见下）

### 7.2 实测结论：本环境下 ptrace 注入**不可用**

| 操作 | 结果 |
| --- | --- |
| `PTRACE_ATTACH` | ✅ |
| `PTRACE_PEEKDATA` 读内存 | ✅ |
| `PTRACE_GETREGSET` | ❌ `EINVAL`（用 ARM 版注入器）／✅（用 x86_64 版） |
| 远程 mmap | ✅ |
| 远程**为 dlopen 建调用桩** | ✅ 桩能执行 |
| 远程 `dlopen`（libdl 跳板） | ❌ 返回垃圾值 |
| 远程 `__loader_dlopen`（linker 真身） | ❌ 要求 `dvmFindStaticFieldHier`（Dalvik 时代符号） |

原因：本机是 **aarch64 内核 / x86_64 userspace / houdini 翻译 ARM** 三层结构，
`dlopen` 在"被 ptrace 劫持、调用者帧不在 linker 已知命名空间"的情况下会失败。

**所以最终方案改成了「换 libmain.so」**（见 `docs/mod-reuse.md`），
完全绕开注入。

---

## 八、复刻的编译方法

```bash
# AArch64 版（给 ARM 游戏用）
$NDK/toolchains/llvm/prebuilt/windows-x86_64/bin/aarch64-linux-android21-clang \
    -O2 -o injector_arm64 injector.c

# x86_64 版（给被翻译的进程用 —— 本机实际需要这个）
$NDK/toolchains/llvm/prebuilt/windows-x86_64/bin/x86_64-linux-android21-clang \
    -O2 -o injector_x64 injector.c
```

**不要加 `-static`。**
