"""定位 payload 里卡密验证的逻辑。

从之前提取的字符串线索：
  AuthConfig.txt / authverify / authcard / authkb / authime / authclear
  cardKeyText / ##cardkey / ##authwindow / ModAuth
  '[Auth] ' / ' Android ID ' / authime

思路：
  1. 找这些字符串在文件里的偏移
  2. 找引用这些字符串的代码（x86_64 是 RIP 相对 lea，AArch64 是 ADRP+ADD）
  3. 把附近的函数反汇编出来，找判断分支
"""
import os
import re
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-analysis.md")

lines = []
def p(s=""):
    lines.append(str(s))

d = open(SO, "rb").read()
p("# 卡密验证逻辑分析（libArkRe.so）")
p()
p("文件：`%s`（%d 字节，AArch64）" % (os.path.basename(SO), len(d)))
p()

# 段表
shoff, = struct.unpack_from("<Q", d, 0x28)
shentsize, shnum, shstrndx = struct.unpack_from("<HHH", d, 0x3A)
secs = []
for i in range(shnum):
    f = struct.unpack_from("<IIQQQQIIQQ", d, shoff + i * shentsize)
    secs.append(dict(name=f[0], type=f[1], flags=f[2], addr=f[3],
                     offset=f[4], size=f[5]))
st = secs[shstrndx]
def nm(n):
    s = d[st["offset"] + n:]
    return s[:s.index(b"\0")].decode("latin1")
for s in secs:
    s["sname"] = nm(s["name"])

def file_to_vaddr(off):
    for s in secs:
        if s["type"] == 1 and s["offset"] <= off < s["offset"] + s["size"]:
            return s["addr"] + (off - s["offset"]), s["sname"]
    return None, None

def vaddr_to_file(va):
    for s in secs:
        if s["type"] == 1 and s["addr"] <= va < s["addr"] + s["size"]:
            return s["offset"] + (va - s["addr"])
    return None

# 1) 找关键字符串
KEYS = [
    b"AuthConfig.txt", b"authverify", b"authcard", b"authkb",
    b"authime", b"authclear", b"cardKeyText", b"##cardkey",
    b"##authwindow", b"ModAuth", b"# \xe5\x8d\xa1\xe5\xaf\x86",
    b"kill_on_expire", b"timeout_sec", b"notice:",
    b"[Auth] ", b" Android ID ",
]

p("## 1. 关键字符串在文件里的位置")
p()
p("| 字符串 | 文件偏移 | 所在段 | 虚拟地址 |")
p("| --- | --- | --- | --- |")
strlocs = {}
for k in KEYS:
    idxs = [m.start() for m in re.finditer(re.escape(k), d)]
    if not idxs:
        p("| `%s` | 未找到 | | |" % k.decode("latin1", "replace"))
        continue
    for i in idxs[:3]:
        va, sec = file_to_vaddr(i)
        p("| `%s` | 0x%X | %s | 0x%X |" % (k.decode("latin1", "replace"), i, sec, va))
        strlocs.setdefault(k, []).append((i, va, sec))
p()

# 2) 找引用了这些地址的指令
# AArch64 引用一个地址通常是:
#   ADRP Xd, page        (0x90000000 mask)
#   ADD  Xd, Xd, #imm12  (0x91000000 mask)
# 或者 LDR 字面量
def find_adrp_add_refs(target_va, max_results=20):
    """扫描 .text，找 ADRP+ADD 组合指向 target_va 的位置"""
    text = next(s for s in secs if s["sname"] == ".text")
    base_off, base_va, size = text["offset"], text["addr"], text["size"]
    tgt_page = target_va & ~0xFFF
    tgt_off = target_va & 0xFFF
    hits = []
    for off in range(base_off, base_off + size - 8, 4):
        ins1, = struct.unpack_from("<I", d, off)
        # ADRP: bit31=1, bits[28:24]=10000
        if (ins1 & 0x9F000000) != 0x90000000:
            continue
        rd = ins1 & 0x1F
        immlo = (ins1 >> 29) & 0x3
        immhi = (ins1 >> 5) & 0x7FFFF
        imm = (immhi << 2) | immlo
        if imm & (1 << 20):
            imm -= (1 << 21)
        pc_va = base_va + (off - base_off)
        page = (pc_va & ~0xFFF) + (imm << 12)
        if page != tgt_page:
            continue
        # 下一条应该是 ADD Xd, Xd, #imm12  或 LDR
        ins2, = struct.unpack_from("<I", d, off + 4)
        if (ins2 & 0xFFC00000) == 0x91000000:      # ADD (64-bit, no shift)
            rn = (ins2 >> 5) & 0x1F
            imm12 = (ins2 >> 10) & 0xFFF
            if rn == rd and imm12 == tgt_off:
                hits.append(pc_va)
                if len(hits) >= max_results:
                    break
    return hits

p("## 2. 引用这些字符串的代码位置")
p()
refs = {}
for k, locs in strlocs.items():
    for (fo, va, sec) in locs[:1]:
        h = find_adrp_add_refs(va)
        if h:
            refs[k] = h
            p("- `%s` (0x%X) 被 %d 处引用：" % (k.decode("latin1", "replace"), va, len(h)))
            for x in h[:8]:
                p("  - `0x%X`" % x)
        else:
            p("- `%s` (0x%X)：没找到 ADRP+ADD 引用（可能用 GOT 间接寻址）"
              % (k.decode("latin1", "replace"), va))
p()

# 3) 把这些引用位置所在函数范围找出来
p("## 3. 引用点附近的字节（供反汇编）")
p()
for k, hits in refs.items():
    for h in hits[:3]:
        fo = vaddr_to_file(h)
        if fo is None:
            continue
        p("### `%s` @ 0x%X（文件偏移 0x%X）" % (k.decode("latin1", "replace"), h, fo))
        p()
        p("```")
        lo = max(0, fo - 64)
        blob = d[lo:fo + 96]
        # 简单按 4 字节列出
        for i in range(0, len(blob) - 3, 4):
            ins, = struct.unpack_from("<I", blob, i)
            va = h - (fo - lo) + i
            mark = "  <== 引用点" if va == h else ""
            p("0x%08X  %08X%s" % (va, ins, mark))
        p("```")
        p()

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("lines:", len(lines))
print()
print("引用统计:")
for k, v in refs.items():
    print("  %-18s %d 处" % (k.decode("latin1","replace"), len(v)))
