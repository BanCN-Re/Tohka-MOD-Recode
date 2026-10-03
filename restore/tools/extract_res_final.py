"""提取并整理 host APK 的关键资源，产出可用于重建的资源集。

重点：
  1. 自有资源（图标、背景图、布局、字符串、颜色、样式）
  2. AndroidManifest 解码
  3. resources.arsc 里自有包的条目
  4. 把二进制 XML 转成可读形式

产出 restore/host-apk/res-final/
"""
import os
import re
import shutil
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
APK = os.path.join(ROOT, "Riruriru Mod_1.1.2.apk.1")
OUT = os.path.join(ROOT, "restore", "host-apk", "res-final")

os.makedirs(OUT, exist_ok=True)


def main():
    z = zipfile.ZipFile(APK)

    # 1) assets（注入器与 payload）
    adir = os.path.join(OUT, "assets")
    os.makedirs(adir, exist_ok=True)
    assets = []
    for info in z.infolist():
        if info.filename.startswith("assets/") and not info.filename.endswith("/"):
            rel = info.filename[len("assets/"):]
            dst = os.path.join(adir, rel.replace("/", os.sep))
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            with open(dst, "wb") as f:
                f.write(z.read(info.filename))
            assets.append("%-24s %12d" % (rel, info.file_size))
    print("assets:")
    for a in assets:
        print("  " + a)

    # 2) 图片资源（自有 + 全部 png/jpg，便于找图标与背景）
    idir = os.path.join(OUT, "images")
    os.makedirs(idir, exist_ok=True)
    imgs = []
    for info in z.infolist():
        n = info.filename
        if not n.startswith("res/"):
            continue
        if not n.lower().endswith((".png", ".jpg", ".jpeg", ".webp", ".gif")):
            continue
        rel = n[len("res/"):]
        dst = os.path.join(idir, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(dst, "wb") as f:
            f.write(z.read(n))
        imgs.append((info.file_size, rel))
    print()
    print("图片 %d 个，最大的 25 个：" % len(imgs))
    for sz, rel in sorted(imgs, reverse=True)[:25]:
        print("  %-52s %10d" % (rel, sz))

    # 3) 布局 XML（二进制）
    ldir = os.path.join(OUT, "layout")
    os.makedirs(ldir, exist_ok=True)
    for info in z.infolist():
        n = info.filename
        if n.startswith("res/layout") and n.endswith(".xml"):
            rel = os.path.basename(n)
            with open(os.path.join(ldir, rel), "wb") as f:
                f.write(z.read(n))
    nlay = len(os.listdir(ldir))
    print()
    print("布局 XML: %d 个" % nlay)

    # 4) manifest
    with open(os.path.join(OUT, "AndroidManifest.xml"), "wb") as f:
        f.write(z.read("AndroidManifest.xml"))
    with open(os.path.join(OUT, "resources.arsc"), "wb") as f:
        f.write(z.read("resources.arsc"))
    print("AndroidManifest.xml + resources.arsc 已解出")

    # 5) 全部 res/xml、res/values
    for sub in ("xml",):
        d = os.path.join(OUT, sub)
        os.makedirs(d, exist_ok=True)
        for info in z.infolist():
            n = info.filename
            if n.startswith("res/%s/" % sub) and n.endswith(".xml"):
                with open(os.path.join(d, os.path.basename(n)), "wb") as f:
                    f.write(z.read(n))
        print("res/%s: %d 个" % (sub, len(os.listdir(d))))

    # 6) kotlin 元数据等杂项也留着
    print()
    print("输出目录:", OUT)


if __name__ == "__main__":
    main()
