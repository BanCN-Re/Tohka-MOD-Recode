"""找 auth 状态变量被写成 1 的地方（授权成功的标志）。

已知：
  0x340F10  cmp w8,#1; cset w0,eq     -> 状态 == 1 才算「已授权」
  0x341518  mov w8,#2; stlr w8,[x9]   -> 写 2
  0x341640  mov w8,#3; stlr w8,[x9]   -> 写 3

状态变量：0x740568（VA）

本脚本：
  1. 反汇编 0x340C00 - 0x341700 整段，把所有 stlr/str 到该变量的地方标出来
  2. 找 mov w8,#1 紧跟写入的模式
  3. 找 0x340D30 那个引用点（第一个）在做什么
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-state-machine.md")

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

p("# 卡密授权状态机（0x740568）")
p()
p("状态变量 VA = `0x740568`")
p()
p("## 状态语义（从判断函数读出）")
p()
p("| 读取函数 | 逻辑 | 含义 |")
p("| --- | --- | --- |")
p("| `0x340EDC` | 直接返回状态值 | 原始读取 |")
p("| `0x340EEC` | `cmp #2; ccmp; cset w0,ne` | 状态 != 2 时返回 1 |")
p("| `0x340F10` | `cmp #1; cset w0,eq` | **状态 == 1 才算已授权** |")
p("| `0x34102C` | 读 | |")
p()
p("## 写入点（stlr）")
p()
p("| 地址 | 写入值 | 上下文 |")
p("| --- | --- | --- |")

START, END = 0x340C00, 0x341700
fo = v2f(START)
blob = d[fo:fo + (END - START)]
insns = list(md.disasm(blob, START))
writes = []
for idx, ins in enumerate(insns):
    if ins.mnemonic in ("stlr", "str") and "w" in ins.op_str.split(",")[0].strip():
        # 往前 6 条找 mov wX, #imm
        val = None
        for k in range(max(0, idx - 6), idx):
            prev = insns[k]
            if prev.mnemonic == "mov" and prev.op_str.startswith("w8, #"):
                val = prev.op_str
                break
            if prev.mnemonic == "mov" and "#" in prev.op_str and ", w" in prev.op_str:
                val = prev.op_str
                break
        writes.append((ins.address, val, ins.op_str))

for a, val, ops in writes:
    p("| `0x%X` | `%s` | `%s` |" % (a, val or "?", ops))
p()

p("## 完整反汇编（0x340C00 - 0x341700）")
p()
p("```asm")
for ins in insns:
    mark = ""
    if ins.mnemonic in ("stlr", "str") and "w8" in ins.op_str:
        mark = "   ; <== 写状态"
    elif ins.mnemonic == "ret":
        mark = "   ; <== 返回"
    elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
        mark = "   ; <分支>"
    p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
p("```")

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("写入点:")
for a, val, ops in writes:
    print("  0x%X  %s  (%s)" % (a, val, ops))
