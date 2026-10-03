"""反汇编卡密有效性判断函数 0x340f10，找它的返回逻辑。

调用点：
  0x247CEC  bl #0x340f10      ; 检查卡密
  0x247CF0  tbnz w0, #0, #0x2484ec   ; 非 0（无效）则跳走

所以 0x340f10 返回 0 = 有效，非 0 = 无效。
本脚本把 0x340f10 整个函数反汇编出来，找所有 ret 与它前面的 w0 赋值。
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-check-fn.md")

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

# 反汇编 0x340f10 起 0x600 字节
START = 0x340F10
SIZE = 0x600
fo = v2f(START)
blob = d[fo:fo + SIZE]

lines = []
def p(s=""):
    lines.append(str(s))

p("# 卡密有效性判断函数 0x340F10")
p()
p("调用点 `0x247CEC`：`bl #0x340f10`，随后 `tbnz w0, #0, #0x2484ec`")
p("→ **返回 0 表示卡密有效（继续进入），非 0 表示无效（跳走）**")
p()
p("```asm")
rets = []
for ins in md.disasm(blob, START):
    mark = ""
    if ins.mnemonic == "ret":
        mark = "   ; <== 返回"
        rets.append(ins.address)
    elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
        mark = "   ; <分支>"
    p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
p("```")
p()
p("## ret 位置汇总")
p()
for r in rets:
    p("- `0x%X`" % r)

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("ret 位置:", [hex(r) for r in rets])
