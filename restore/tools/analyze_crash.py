"""反汇编 linker64 里 dlopen 崩掉的位置，看它到底在干什么。"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
LINKER = os.path.join(os.path.dirname(HERE), "work", "libs", "linker64_real")

d = open(LINKER, "rb").read()
print("linker64 size:", len(d))

# 段表
shoff, = struct.unpack_from("<Q", d, 0x28)
shentsize, shnum, shstrndx = struct.unpack_from("<HHH", d, 0x3A)
secs = []
for i in range(shnum):
    f = struct.unpack_from("<IIQQQQIIQQ", d, shoff + i * shentsize)
    secs.append(dict(name=f[0], type=f[1], flags=f[2], addr=f[3],
                     offset=f[4], size=f[5], link=f[6]))
st = secs[shstrndx]
def nm(n):
    s = d[st["offset"] + n:]
    return s[:s.index(b"\0")].decode("latin1")
for s in secs:
    s["sname"] = nm(s["name"])

print("\n=== 段表（含 addr，用于 RVA->offset 换算）===")
for s in secs:
    if s["type"] == 1 and s["size"]:
        print("  %-16s addr=0x%-9X off=0x%-9X size=0x%-8X" %
              (s["sname"], s["addr"], s["offset"], s["size"]))

def rva2off(rva):
    for s in secs:
        if s["type"] == 1 and s["addr"] <= rva < s["addr"] + s["size"]:
            return s["offset"] + (rva - s["addr"])
    return None

BASE_IN_PROC = 0x7fb767f96000
crash = 0x7fb768077173
rva = crash - BASE_IN_PROC
off = rva2off(rva)
print("\n=== 崩溃点 ===")
print("  进程内地址 = 0x%X" % crash)
print("  RVA        = 0x%X" % rva)
print("  文件偏移   = %s" % (hex(off) if off else "不在任何段内"))

# 找哪个符号包含这个 RVA
print("\n=== 哪个导出符号覆盖该 RVA ===")
best = None
for s in secs:
    if s["sname"] != ".dynsym":
        continue
    strt = secs[s["link"]]
    n = s["size"] // 24
    for i in range(n):
        o = s["offset"] + i * 24
        nameoff, info, other, shndx, value, size = struct.unpack_from("<IBBHQQ", d, o)
        if not nameoff or shndx == 0 or not size:
            continue
        if value <= rva < value + size:
            if best is None or size < best[2]:
                stt = d[strt["offset"] + nameoff:]
                best = (stt[:stt.index(b"\0")].decode("latin1"), value, size)
if best:
    print("  %s  (RVA 0x%X, size %d, 偏移 +0x%X)" % (best[0], best[1], best[2], rva - best[1]))
else:
    print("  未匹配到带 size 的导出符号")

# 反汇编附近
if off:
    print("\n=== 崩溃点前后 48 字节 ===")
    lo = off - 32
    blob = d[lo:off + 32]
    print("  0x%X: %s" % (lo, blob.hex(" ")))

    try:
        from capstone import Cs, CS_ARCH_X86, CS_MODE_64
        md = Cs(CS_ARCH_X86, CS_MODE_64)
        print("\n=== 反汇编（从崩溃点前 32 字节开始）===")
        for ins in md.disasm(blob, lo):
            mark = "   <== 崩溃点" if ins.address == off else ""
            print("  0x%-8X  %-24s %s%s" %
                  (ins.address, ins.mnemonic + " " + ins.op_str, "", mark))
    except ImportError:
        print("  (capstone 不可用)")
