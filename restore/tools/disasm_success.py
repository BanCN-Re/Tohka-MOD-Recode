"""分析「验证成功」路径与把状态推回验证界面的复查逻辑。

线索：
  验证成功 @ 0x3403E8
  状态变量 0x740568：  1 = 验证中/已提交   2 = 验证通过   3 = 失败
  0x341518 写 2（验证通过）
  0x341640 写 3（失败）

现象：写 2 时功能列表出现一瞬间，又被推回验证界面
     → 有个周期性任务在复查（可能向服务器确认，失败后写 3 或改回 1）

本脚本反汇编 0x340300-0x340600（验证成功/失败处理）与
0x341400-0x341700（写状态的函数），找周期复查的入口。
"""
import os
import re
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-success-path.md")

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

p("# 验证成功路径与复查逻辑")
p()

def dump(title, start, size, marks=None):
    p("### %s" % title)
    p()
    p("```asm")
    fo = v2f(start)
    if fo is None:
        p("(不在段内)")
    else:
        for ins in md.disasm(d[fo:fo + size], start):
            mark = ""
            if marks and ins.address in marks:
                mark = "   ; %s" % marks[ins.address]
            elif ins.mnemonic == "ret":
                mark = "   ; <== 返回"
            elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
                mark = "   ; <分支>"
            p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
    p("```")
    p()

# 验证成功引用点
dump("验证成功路径 @ 0x3403E8 附近", 0x3402E0, 0x200,
     {0x3403E8: "打印「验证成功」"})

# 写状态的函数（0x341514 写2 / 0x34163C 写3）
dump("写状态的函数 @ 0x3413A0 起", 0x3413A0, 0x320,
     {0x341518: "写 2", 0x341640: "写 3"})

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("lines:", len(lines))
