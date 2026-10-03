"""找 0x740568 这个全局状态变量的所有读写点，确认它的语义。

0x340F10 读它：cmp w8,#1; cset w0,eq
调用点 0x247CEC 后是 tbnz w0,#0,跳走

需要搞清：
  A. 0x740568 什么时候被写成 1（= 授权成功）
  B. 调用点的 tbnz 到底是「1 就跳走」还是「1 就进入」
  C. 有没有别的写入点（比如验证成功后写 1）
"""
import os
import re
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-global-var.md")

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

text = next(s for s in secs if s["sname"] == ".text")
lines = []
def p(s=""):
    lines.append(str(s))

TARGET = 0x740568          # 全局状态变量
md = Cs(CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN)

p("# 全局状态变量 0x%X 的所有访问点" % TARGET)
p()
p("`0x340F10` 这个函数读它：`ldar w8,[x8]; cmp w8,#1; cset w0,eq`")
p()
p("下面扫描整个 .text，找所有 ADRP+ADD/LDR/STR 指向 0x%X 的指令。" % TARGET)
p()

tgt_page = TARGET & ~0xFFF
tgt_off = TARGET & 0xFFF
base_off, base_va, size = text["offset"], text["addr"], text["size"]

hits = []      # (地址, 说明, 后续指令)
for off in range(base_off, base_off + size - 16, 4):
    ins1, = struct.unpack_from("<I", d, off)
    if (ins1 & 0x9F000000) != 0x90000000:      # ADRP
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
    # 看后面最多 4 条指令，找 ADD/STR/LDR 且 imm12 == tgt_off
    seq = []
    found = False
    note = ""
    for k in range(1, 6):
        if off + k * 4 + 4 > len(d):
            break
        ins, = struct.unpack_from("<I", d, off + k * 4)
        # ADD imm  (0x91...)  或 LDR/STR (无偏移形式)
        if (ins & 0xFFC00000) == 0x91000000:      # ADD 64-bit
            rn = (ins >> 5) & 0x1F
            imm12 = (ins >> 10) & 0xFFF
            if rn == rd and imm12 == tgt_off:
                found = True
                note = "ADD x%d, x%d, #0x%X" % (rd, rd, imm12)
                # 再看下一条是读还是写
                ins2, = struct.unpack_from("<I", d, off + (k + 1) * 4)
                seq.append((pc_va + (k + 1) * 4, ins2))
                break
    if found:
        hits.append((pc_va, note, seq))

p("共找到 **%d** 处引用。" % len(hits))
p()
p("| 地址 | 指令 | 下一条 | 判断 |")
p("| --- | --- | --- | --- |")
for va, note, seq in hits:
    nxt = ""
    verdict = ""
    if seq:
        nva, ins2 = seq[0]
        # 解码下一条
        code = struct.pack("<I", ins2)
        for i in md.disasm(code, nva):
            nxt = "%s %s" % (i.mnemonic, i.op_str)
            # 判断读写
            if i.mnemonic.startswith("str") or i.mnemonic.startswith("stp"):
                verdict = "**写**"
            elif i.mnemonic.startswith("ldr") or i.mnemonic.startswith("ldar") or i.mnemonic.startswith("ldrb"):
                verdict = "读"
            elif i.mnemonic.startswith("stlr") or i.mnemonic.startswith("stlrb"):
                verdict = "**写(原子)**"
            break
    p("| `0x%X` | `%s` | `%s` | %s |" % (va, note, nxt, verdict))
p()

# 详细反汇编几个关键位置
p("## 关键位置的反汇编")
p()
for va, note, seq in hits[:8]:
    fo = None
    for s in secs:
        if s["type"] == 1 and s["addr"] <= va < s["addr"] + s["size"]:
            fo = s["offset"] + (va - s["addr"])
    if fo is None:
        continue
    p("### 0x%X（%s）" % (va, note))
    p()
    p("```asm")
    lo = max(0, fo - 40)
    blob = d[lo:fo + 120]
    for i in md.disasm(blob, va - (fo - lo)):
        mark = "   <== 引用 0x%X" % TARGET if i.address == va else ""
        p("0x%08X  %-8s %s%s" % (i.address, i.mnemonic, i.op_str, mark))
    p("```")
    p()

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("引用点数量:", len(hits))
for va, note, seq in hits:
    print("  0x%X  %s" % (va, note))
