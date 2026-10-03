"""找 ModAuth 的 enabled 标志。

日志：ModAuth: [+] 已加载卡密配置: enabled=1
      ModAuth: [+] 卡密模块初始化完成, ..., enabled=1

而 AuthConfig.txt 里没有 enabled 键（注释说 host/appid/appkey/rc4key 由程序内置）。

思路：找打印 "已加载卡密配置" / "卡密模块初始化完成" 的代码，
     它附近的 enabled 判断就是我们要改的地方。

字符串（UTF-8）：
  已加载卡密配置            e5 b7 b2 e5 8a a0 e8 bd bd e5 8d a1 e5 af 86 e9 85 8d e7 bd ae
  卡密模块初始化完成        e5 8d a1 e5 af 86 e6 a8 a1 e5 9d 97 e5 88 9d e5 a7 8b e5 8c 96 e5 ae 8c e6 88 90
  enabled
"""
import os
import re
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-enabled.md")

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

def f2v(fo):
    for s in secs:
        if s["type"] == 1 and s["offset"] <= fo < s["offset"] + s["size"]:
            return s["addr"] + (fo - s["offset"])
    return None

lines = []
def p(s=""):
    lines.append(str(s))

p("# ModAuth enabled 标志定位")
p()

# 1) 找中文字符串
CN = {
    "已加载卡密配置": "已加载卡密配置".encode("utf-8"),
    "卡密模块初始化完成": "卡密模块初始化完成".encode("utf-8"),
    "卡密验证": "卡密验证".encode("utf-8"),
    "验证中": "验证中".encode("utf-8"),
    "请稍候": "请稍候".encode("utf-8"),
    "卡密无效": "卡密无效".encode("utf-8"),
    "验证成功": "验证成功".encode("utf-8"),
    "验证失败": "验证失败".encode("utf-8"),
}
p("## 1. 中文字符串位置")
p()
p("| 字符串 | 找到 | 虚拟地址 |")
p("| --- | --- | --- |")
strva = {}
for k, b in CN.items():
    m = re.search(re.escape(b), d)
    if m:
        va = f2v(m.start())
        strva[k] = va
        p("| %s | 是 | 0x%X |" % (k, va))
    else:
        p("| %s | 否 | |" % k)
p()

# 2) 找引用
md = Cs(CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN)
text = next(s for s in secs if s["sname"] == ".text")
base_off, base_va, size = text["offset"], text["addr"], text["size"]

def refs_to(target_va, limit=8):
    tgt_page = target_va & ~0xFFF
    tgt_off = target_va & 0xFFF
    hits = []
    for off in range(base_off, base_off + size - 8, 4):
        ins1, = struct.unpack_from("<I", d, off)
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
        ins2, = struct.unpack_from("<I", d, off + 4)
        if (ins2 & 0xFFC00000) == 0x91000000:
            rn = (ins2 >> 5) & 0x1F
            imm12 = (ins2 >> 10) & 0xFFF
            if rn == rd and imm12 == tgt_off:
                hits.append(pc_va)
                if len(hits) >= limit:
                    return hits
    return hits

p("## 2. 引用点")
p()
refmap = {}
for k, va in strva.items():
    h = refs_to(va)
    refmap[k] = h
    p("- %s (0x%X)：%s" % (k, va, " ".join("`0x%X`" % x for x in h) or "无"))
p()

# 3) 反汇编「已加载卡密配置」附近（这里会读 enabled）
if "已加载卡密配置" in refmap and refmap["已加载卡密配置"]:
    for va in refmap["已加载卡密配置"][:2]:
        p("## 3. 「已加载卡密配置」附近反汇编 @ 0x%X" % va)
        p()
        p("```asm")
        START = va - 0x180
        fo = v2f(START)
        for ins in md.disasm(d[fo:fo + 0x300], START):
            mark = ""
            if ins.address == va:
                mark = "   ; <== 打印「已加载卡密配置」"
            elif ins.mnemonic == "ret":
                mark = "   ; <== 返回"
            elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
                mark = "   ; <分支>"
            p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
        p("```")
        p()

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
