#!/usr/bin/env python3
"""从二进制 AXML 里删除指定属性（不重新编译 manifest）。

## 为什么需要

重建版要去掉 `android:sharedUserId` —— 原版用它跟游戏共享 UID，
但重建版走 root 不需要，留着会导致签名不匹配装不上。

重新编译 manifest 需要完整的 AndroidX 资源（2000+ flat），
aapt2 link 的参数会超 Windows 命令行上限，所以直接改二进制。

## AXML 属性名怎么定位（关键）

属性在 START_TAG 里存的是**字符串池索引**，不是资源 ID：

```
attributes[i]: ns(4) name(4) rawValue(4) typedValue(8)
                     ↑
              字符串池索引
```

要判断它是不是 `android:sharedUserId`，需要经过 **ResourceMap** chunk：

```
ResourceMap chunk (0x0180):
  它的第 k 个 uint32 = 字符串池里第 k 个字符串对应的资源 ID
也就是：  resourceId = resourceMap[ nameIndex ]
```

并且该属性的 namespace 必须是 `http://schemas.android.com/apk/res/android`。

## 用法
    python tools/strip_axml_attr.py <in.apk> <out.apk> [resId]
    默认 resId = 0x0101000b (android:sharedUserId)
"""
import os
import struct
import sys
import zipfile

RES_STRING_POOL = 0x0001
RES_XML_START_TAG = 0x0102
RES_XML_RESOURCE_MAP = 0x0180

DEFAULT_ID = 0x0101000B          # android:sharedUserId


def parse_chunks(data):
    """把 AXML 拆成 [(type, headerSize, chunk_bytes, offset)]"""
    out = []
    pos = 8
    while pos + 8 <= len(data):
        ct, chs, csz = struct.unpack_from("<HHI", data, pos)
        if csz <= 0 or pos + csz > len(data):
            break
        out.append((ct, chs, data[pos:pos + csz], pos))
        pos += csz
    return out


def resource_map(data):
    """返回 ResourceMap 的 list（下标 = 字符串池索引）"""
    for ct, chs, chunk, off in parse_chunks(data):
        if ct == RES_XML_RESOURCE_MAP:
            n = (len(chunk) - 8) // 4
            return list(struct.unpack_from("<%dI" % n, chunk, 8))
    return []


def strip_attr(data: bytes, target_id: int):
    chunks = parse_chunks(data)
    rmap = resource_map(data)

    out = bytearray(data[:8])
    removed = 0
    details = []

    for ct, chs, chunk, off in chunks:
        if ct != RES_XML_START_TAG or len(chunk) < 36:
            out += chunk
            continue

        attr_start, attr_size, attr_count = struct.unpack_from("<HHH", chunk, 24)
        keep = []
        for i in range(attr_count):
            aoff = attr_start + i * attr_size
            if aoff + attr_size > len(chunk):
                keep.append(chunk[aoff:aoff + attr_size])
                continue
            ans, aname, raw = struct.unpack_from("<III", chunk, aoff)
            # 通过 resourceMap 把字符串索引翻成资源 ID
            rid = rmap[aname] if 0 <= aname < len(rmap) else 0
            if rid == target_id:
                removed += 1
                details.append("  删除属性: nameIndex=%d rid=0x%08x" % (aname, rid))
                continue
            keep.append(chunk[aoff:aoff + attr_size])

        if len(keep) != attr_count:
            tail_off = attr_start + attr_count * attr_size
            tail = chunk[tail_off:]
            head = bytearray(chunk[:attr_start])
            newbody = b"".join(keep) + tail
            struct.pack_into("<I", head, 4, attr_start + len(newbody))
            struct.pack_into("<H", head, 28, len(keep))
            chunk = bytes(head) + newbody

        out += chunk

    struct.pack_into("<I", out, 4, len(out))
    return bytes(out), removed, details


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        return 2
    src, dst = sys.argv[1], sys.argv[2]
    rid = int(sys.argv[3], 16) if len(sys.argv) > 3 else DEFAULT_ID

    print("源 APK : %s" % src)
    print("目标   : %s" % dst)
    print("删除属性资源 ID: 0x%08x" % rid)

    with zipfile.ZipFile(src) as zin:
        raw = zin.read("AndroidManifest.xml")
        new, n, det = strip_attr(raw, rid)
        print("原 manifest: %d B -> 新: %d B" % (len(raw), len(new)))
        print("删除 %d 个属性" % n)
        for d in det:
            print(d)

        with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
            for e in zin.infolist():
                if e.is_dir():
                    continue
                nm = e.filename
                if nm == "AndroidManifest.xml":
                    zout.writestr(nm, new)
                elif nm.upper().startswith("META-INF/") and \
                        nm.upper().endswith((".RSA", ".DSA", ".EC", ".SF", "MANIFEST.MF")):
                    continue              # 旧签名丢弃
                else:
                    zout.writestr(e, zin.read(nm))
    print("完成:", dst)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
