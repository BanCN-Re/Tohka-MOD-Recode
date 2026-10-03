"""把两个 payload 里内嵌的 injected.dex 提取出来。

payload 的 .so 里内嵌了一个 DEX（魔数 dex\\n035），文件名 injected.dex。
原 mod 用 DexClassLoader 把它加载进游戏进程，里面的
com.example.imgui.GLES3JNIView 负责建 GL 表面、回调 native 画 ImGui。

我们从 .so 里定位 DEX 的起点和长度（用 DEX 头的 file_size 字段），提取出来。
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
ASSETS = os.path.join(ROOT, "work", "assets")
OUT = os.path.join(ROOT, "build", "dex")
os.makedirs(OUT, exist_ok=True)

DEX_MAGIC = b"dex\n035\x00"


def carve_dex(path):
    d = open(path, "rb").read()
    off = d.find(DEX_MAGIC)
    if off < 0:
        return None, None, "找不到 DEX 魔数"
    # DEX 头：magic(8) checksum(4) signature(20) file_size(4) header_size(4) ...
    file_size = struct.unpack_from("<I", d, off + 32)[0]
    if not (0x70 <= file_size <= len(d) - off):
        return off, None, "file_size 不合理: %d" % file_size
    return off, d[off:off + file_size], None


for name in ("libArkRe.so", "libCherryTale.so"):
    p = os.path.join(ASSETS, name)
    if not os.path.exists(p):
        print("MISS", p)
        continue
    off, blob, err = carve_dex(p)
    print("=" * 70)
    print(name)
    print("=" * 70)
    if err:
        print("  失败:", err, "(偏移 %s)" % (hex(off) if off else "无"))
        continue
    out = os.path.join(OUT, name.replace(".so", "") + ".injected.dex")
    open(out, "wb").write(blob)
    print("  DEX 偏移 = 0x%X" % off)
    print("  DEX 大小 = %d 字节" % len(blob))
    print("  已写出   = %s" % out)
    # DEX 头信息
    ver = blob[4:7].decode()
    string_ids_size = struct.unpack_from("<I", blob, 56)[0]
    class_defs_size = struct.unpack_from("<I", blob, 96)[0]
    print("  版本=%s  string_ids=%d  class_defs=%d" % (ver, string_ids_size, class_defs_size))
