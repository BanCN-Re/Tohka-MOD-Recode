"""反汇编 0x4FC0B0（授权判定）和它的调用环境，确定最优 patch 点。

已知：
  0x340D50  bl #0x4fc0b0        ; 返回非 0 = 通过
  0x340D54  cmp w0, #0
  0x340D58  b.eq #0x340d70      ; ==0 跳过
  0x340D5C  ldar w8,[x20]
  0x340D60  cmp w8, #1
  0x340D64  b.eq #0x340e10
  0x340D68  mov w8, #1          ; 授权成功 -> 状态 = 1
  0x340D6C  stlr w8, [x20]

候选 patch：
  A. 0x340D58  b.eq -> b         （无条件走成功路径）
  B. 0x340F20  cset w0,eq -> mov w0,#1   （让「已授权」判断恒真）
  C. 0x4FC0B0 头部改成 mov w0,#1; ret    （让判定函数恒真）
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-patch-plan.md")

from capstone import Cs, CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN

d = open(SO, "rb").read()
shoff, = struct.unpack_from("<Q", d, 0x28)
shentsize, shnum, shstrndx = struct.unpack_from("<HHH", d, 0x3A)
secs = []
for i in range(shnum):
    f = struct.unpack_from("<IIQQQQIIQQ", d, shoff + i * shentsize)
    secs.append(dict(name=f[0], type=f[1], addr=f[3], offset=f[4], size=f[5]))
st = secs[shstrndx]
def nm(n):
    s = d[st["offset"] + n:]
    return s[:s.index(b"\0")].decode("latin1")
for s in secs:
    s["sname"] = nm(s["name"])

def v2f(va):
    for s in secs:
        if s["type"] == 1 and s["addr"] <= va < s["addr"] + s["size"]:
            return s["offset"] + (va - s["addr"])
    return None

md = Cs(CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN)

lines = []
def p(s=""):
    lines.append(str(s))

def dump(title, start, size):
    p("### %s" % title)
    p()
    p("```asm")
    fo = v2f(start)
    if fo is None:
        p("(不在段内)")
    else:
        for ins in md.disasm(d[fo:fo + size], start):
            mark = ""
            if ins.mnemonic == "ret":
                mark = "   ; <== 返回"
            elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
                mark = "   ; <分支>"
            p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
    p("```")
    p()

p("# 卡密验证 patch 方案")
p()
p("## 1. 授权判定函数 0x4FC0B0")
p()
dump("0x4FC0B0（前 0x200 字节）", 0x4FC0B0, 0x200)

p("## 2. 调用它的地方 0x340D18 起")
p()
dump("0x340D18 - 0x340E20", 0x340D18, 0x110)

p("## 3. 三个 patch 候选的字节对比")
p()
CANDIDATES = [
    ("A", 0x340D58, "b.eq #0x340d70", "b #0x340d70",
     "让「判定通过」后无条件写状态=1"),
    ("B", 0x340F20, "cset w0,eq", "mov w0,#1",
     "让 0x340F10「是否已授权」恒为真"),
    ("C", 0x4FC0B0, "(函数头)", "mov w0,#1; ret",
     "让 0x4FC0B0 这个判定函数恒返回 1"),
]
p("| 方案 | 地址 | 原指令 | 改为 | 效果 |")
p("| --- | --- | --- | --- | --- |")
for tag, va, old, new, eff in CANDIDATES:
    p("| %s | `0x%X` | `%s` | `%s` | %s |" % (tag, va, old, new, eff))
p()

p("## 4. 各方案的原字节与目标字节")
p()
for tag, va, old, new, eff in CANDIDATES:
    fo = v2f(va)
    orig = d[fo:fo + 8]
    p("### 方案 %s @ 0x%X" % (tag, va))
    p()
    p("- 原字节：`%s`" % orig.hex(" "))
    p("- 原指令：`%s`" % old)
    if tag == "A":
        p("- AArch64 编码：`b` 的 imm26 = (target - pc) >> 2")
        target = 0x340D70
        imm = ((target - va) >> 2) & 0x03FFFFFF
        neww = 0x14000000 | imm
        p("- 目标字节：`%s`（`b #0x%X`）" % (struct.pack("<I", neww).hex(" "), target))
    elif tag == "B":
        # mov w0, #1  = 0x52800020
        p("- 目标字节：`20 00 80 52`（`mov w0, #1`）")
    else:
        # mov w0,#1 ; ret  = 0x52800020, 0xd65f03c0
        p("- 目标字节：`20 00 80 52 c0 03 5f d6`（`mov w0,#1; ret`）")
    p()

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("lines:", len(lines))
