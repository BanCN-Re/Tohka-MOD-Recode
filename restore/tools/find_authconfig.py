"""找 AuthConfig.txt 的解析代码，看有没有 enabled 之类的开关能直接关掉卡密验证。

线索：
  ModAuth: [+] 已加载卡密配置: enabled=1
  ModAuth: [+] 卡密模块初始化完成, ..., enabled=1

已知 AuthConfig.txt 引用点：0x33D0CC / 0x33D87C
已知配置键引用：kill_on_expire(0x33D900) timeout_sec(0x33D950) notice(0x33D9C8)

本脚本：
  1. 找 "enabled" 字符串及其引用
  2. 反汇编 0x33D000-0x33DC00 整段配置解析
  3. 找所有被引用的配置键名
"""
import os
import re
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-config-parse.md")

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

# 1) 找配置键名
KEYS = [b"enabled", b"kill_on_expire", b"timeout_sec", b"notice", b"title",
        b"host", b"appid", b"appkey", b"rc4key", b"AuthConfig"]
p("# AuthConfig 解析分析")
p()
p("## 1. 配置键名出现位置")
p()
p("| 键 | 文件偏移 | 虚拟地址 |")
p("| --- | --- | --- |")
locs = {}
for k in KEYS:
    for m in re.finditer(re.escape(k), d):
        i = m.start()
        # 只收看起来像独立字符串的（前后是 \0 或非字母）
        before = d[i-1] if i > 0 else 0
        after_i = i + len(k)
        after = d[after_i] if after_i < len(d) else 0
        if (before == 0 or not chr(before).isalnum()) and (after == 0 or not chr(after).isalnum()):
            va = f2v(i)
            p("| `%s` | 0x%X | 0x%X |" % (k.decode(), i, va or 0))
            locs.setdefault(k, []).append((i, va))
p()

# 2) 找引用这些键的 ADRP+ADD
md = Cs(CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN)
text = next(s for s in secs if s["sname"] == ".text")
base_off, base_va, size = text["offset"], text["addr"], text["size"]

def refs_to(target_va, limit=10):
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

p("## 2. 各配置键的引用点")
p()
keyrefs = {}
for k, lst in locs.items():
    for (fo, va) in lst[:1]:
        if va is None:
            continue
        h = refs_to(va)
        keyrefs[k] = (va, h)
        p("- `%s` (0x%X)：%d 处引用 %s" %
          (k.decode(), va, len(h), " ".join("`0x%X`" % x for x in h[:6])))
p()

# 3) 反汇编配置解析段
p("## 3. 配置解析段反汇编（0x33D000 - 0x33DC00）")
p()
p("```asm")
START, END = 0x33D000, 0x33DC00
fo = v2f(START)
for ins in md.disasm(d[fo:fo + (END - START)], START):
    mark = ""
    for k, (va, h) in keyrefs.items():
        if ins.address in h:
            mark = "   ; ===> 引用键 %s" % k.decode()
            break
    if not mark and ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz") or ins.mnemonic.startswith("b."):
        mark = "   ; <分支>"
    p("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, mark))
p("```")

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print()
print("配置键引用:")
for k, (va, h) in keyrefs.items():
    print("  %-16s 0x%-8X %d 处" % (k.decode(), va, len(h)))
