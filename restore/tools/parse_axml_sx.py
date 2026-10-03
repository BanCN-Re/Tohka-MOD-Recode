"""Minimal AndroidManifest (AXML) reader for the Riruriru Mod host APK.

Only used to confirm component names, permissions and uses-sdk values for
docs/module-host-business.md.  Not part of the build.
"""
import struct
import sys

# Attribute resource IDs we care about (android namespace)
ATTRS = {
    0x01010000: "theme",
    0x01010001: "label",
    0x01010002: "icon",
    0x01010003: "name",
    0x01010006: "permission",
    0x01010007: "protectionLevel",
    0x0101000B: "sharedUserId",
    0x0101000E: "enabled",
    0x0101000F: "debuggable",
    0x01010010: "exported",
    0x01010018: "authorities",
    0x01010024: "value",
    0x0101020C: "minSdkVersion",
    0x0101021B: "versionCode",
    0x0101021C: "versionName",
    0x01010270: "targetSdkVersion",
    0x010102B7: "extractNativeLibs",
    0x01010535: "compileSdkVersion",
    0x01010572: "compileSdkVersionCodename",
    0x0101057A: "appComponentFactory",
    0x010104C7: "requestLegacyExternalStorage",
}

TYPES = {0x00: "NULL", 0x01: "REF", 0x02: "STR", 0x03: "INT", 0x04: "HEX",
         0x05: "BOOL", 0x06: "COLOR", 0x10: "FLOAT", 0x11: "DIM", 0x12: "FRAC"}


def read_strings(buf, off):
    _chunk_type, _hdr, _size = struct.unpack_from("<HHI", buf, off)
    count, _style_count, _flags, strings_start = struct.unpack_from("<IIII", buf, off + 8)
    offsets = struct.unpack_from("<%dI" % count, buf, off + 28)
    out = []
    for o in offsets:
        p = off + strings_start + o
        # u16 length (utf-16), possibly high-bit extended
        n = struct.unpack_from("<H", buf, p)[0]
        p += 2
        if n & 0x8000:
            n = ((n & 0x7FFF) << 16) | struct.unpack_from("<H", buf, p)[0]
            p += 2
        raw = buf[p:p + n * 2]
        out.append(raw.decode("utf-16-le", "replace"))
    return out


def walk(buf, off, strings, depth=0, out=None):
    if out is None:
        out = []
    while off < len(buf):
        ctype, hdr, csize = struct.unpack_from("<HHI", buf, off)
        if ctype == 0x0102:  # START_ELEMENT
            name_i = struct.unpack_from("<I", buf, off + 20)[0]
            attr_start, attr_size, attr_count = struct.unpack_from("<HHH", buf, off + 24)
            tag = strings[name_i]
            attrs = []
            for a in range(attr_count):
                ap = off + 16 + attr_start + a * attr_size
                a_ns, a_name, a_raw = struct.unpack_from("<iii", buf, ap)
                # typed value: size(2) res0(1) type(1) data(4)
                a_type = buf[ap + 15]
                a_data = struct.unpack_from("<I", buf, ap + 16)[0]
                key = ATTRS.get(a_name, "0x%08x" % (a_name & 0xFFFFFFFF))
                if a_type == 0x03 and a_data > 0x7FFFFFFF:
                    val = str(a_data - 0x100000000)
                elif a_type == 0x03:
                    val = str(a_data)
                elif a_type == 0x05:
                    val = "true" if a_data else "false"
                elif a_type == 0x02:
                    val = strings[a_data]
                elif a_type == 0x01:
                    val = "<ref 0x%08x>" % a_data
                else:
                    val = "%s:%d" % (TYPES.get(a_type, a_type), a_data)
                attrs.append("%s=%s" % (key, val))
            out.append("%s<%s %s>" % ("  " * depth, tag, " ".join(attrs)))
            walk(buf, off + csize, strings, depth + 1, out)
            return out, off + csize
        if ctype == 0x0103:  # END_ELEMENT
            name_i = struct.unpack_from("<I", buf, off + 20)[0]
            out.append("%s</%s>" % ("  " * depth, strings[name_i]))
            return out, off + csize
        if csize == 0:
            break
        off += csize
    return out, off


def main(path):
    buf = open(path, "rb").read()
    strings = read_strings(buf, 8)
    print("string pool: %d entries" % len(strings))
    # find the first tag chunk (0x0102)
    off = 8 + struct.unpack_from("<I", buf, 12)[0]
    while off < len(buf) and struct.unpack_from("<H", buf, off)[0] != 0x0102:
        off += struct.unpack_from("<I", buf, off + 4)[0]
    lines, _ = walk(buf, off, strings)
    print("\n".join(lines))


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else
         r"D:\Work\MuMuP\ArkReCodeHack\restore\host-apk\AndroidManifest.xml")
