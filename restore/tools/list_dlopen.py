"""列出设备 libdl.so / libc.so 的导出符号，找 dlopen 的正确入口。"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
LIBS = os.path.join(os.path.dirname(HERE), "work", "libs")

WANT = ("dlopen", "dlerror", "android_dlopen_ext", "__loader_dlopen",
        "dlsym", "dlvsym", "__loader_android_dlopen_ext")

def shdrs(d):
    shoff, = struct.unpack_from("<Q", d, 0x28)
    shentsize, shnum, shstrndx = struct.unpack_from("<HHH", d, 0x3A)
    secs = []
    for i in range(shnum):
        f = struct.unpack_from("<IIQQQQIIQQ", d, shoff + i * shentsize)
        secs.append(dict(name=f[0], type=f[1], addr=f[3], offset=f[4], size=f[5], link=f[6]))
    st = secs[shstrndx]
    def nm(n):
        s = d[st["offset"] + n:]
        return s[:s.index(b"\0")].decode("latin1")
    for s in secs:
        s["sname"] = nm(s["name"])
    return secs

for fn in ("libdl_real.so", "libc_real.so"):
    p = os.path.join(LIBS, fn)
    if not os.path.exists(p):
        print("MISS", p)
        continue
    d = open(p, "rb").read()
    secs = shdrs(d)
    print("=" * 74)
    print("%s  (%d bytes)" % (fn, len(d)))
    print("=" * 74)
    for s in secs:
        if s["sname"] not in (".dynsym",):
            continue
        strt = secs[s["link"]]
        n = s["size"] // 24
        for i in range(n):
            off = s["offset"] + i * 24
            nameoff, info, other, shndx, value, size = struct.unpack_from("<IBBHQQ", d, off)
            if not nameoff:
                continue
            st = d[strt["offset"] + nameoff:]
            name = st[:st.index(b"\0")].decode("latin1")
            if name in WANT or name.startswith("__loader"):
                print("   %-32s value=0x%-8X size=%-6d shndx=%d bind=%d type=%d"
                      % (name, value, size, shndx, info >> 4, info & 0xF))
    print()
