"""看 0x340B38 —— 验证结果写入后调用的状态处理函数。

调用链：
  0x340364  bl #0x500be0        把响应转成数字 x26
  0x340380  cmp x26, #1
  0x340388  cset w9, lt         w9 = (x26 < 1)  → 1 表示失败
  0x340394  strb w9, [x19, #1]  存失败标志
  0x3403A0  bl #0x340b38        交给状态处理

0x340B38 很可能就是「按结果设置 0x740568」的地方 ——
也就是把 2（成功）/ 3（失败）写进去的函数。
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-state-setter.md")

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

p("# 状态设置函数")
p()
p("## 0x340B38（验证结果处理）")
p()
p("```asm")
START, SIZE = 0x340B38, 0x300
fo = v2f(START)
for ins in md.disasm(d[fo:fo + SIZE], START):
    mark = ""
    if ins.mnemonic == "ret":
        mark = "   ; <== 返回"
    elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
        mark = "   ; <分支>"
    p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
p("```")
p()

p("## 0x340C74（0x341510 处被调用，紧邻写状态=2）")
p()
p("```asm")
START, SIZE = 0x340C74, 0x200
fo = v2f(START)
for ins in md.disasm(d[fo:fo + SIZE], START):
    mark = ""
    if ins.mnemonic == "ret":
        mark = "   ; <== 返回"
    elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
        mark = "   ; <分支>"
    p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
p("```")

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
