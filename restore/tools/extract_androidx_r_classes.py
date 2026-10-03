#!/usr/bin/env python3
"""从原 APK 的 dex 里提取 AndroidX 库自己的 R 类（R$string 等）。

## 背景

AndroidX 的每个库都有自己的 R 类（`androidx.startup.R$string`），
它们由 AAR 里的 R.txt 在编译期生成，值是**该库自己资源 ID 的别名**
（最终会被 aapt2 替换成应用包里的真实 ID）。

原 APK 的 dex 里就有这些类（已链接过，值是最终数值）。
把它们提取出来编译进我们的 APK，就能解决：

    NoClassDefFoundError: Failed resolution of: Landroidx/startup/R$string;

## 做法

用 androguard 读原 APK 的 dex，找所有 `<pkg>/R$<Type>` 与 `<pkg>/R` 类，
读出它们的 static final int 字段值，生成等价的 Java 源码。

用法:
    python tools/extract_androidx_r_classes.py
"""
import os
import re
import struct
import sys
import zipfile
from collections import defaultdict

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass

from androguard.core.dex import DEX

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
PROJ = os.path.dirname(ROOT)
APK = None
for c in ("Riruriru Mod_1.1.2.apk.1", "Riruriru Mod_1.1.2.apk"):
    p = os.path.join(PROJ, c)
    if os.path.exists(p):
        APK = p
        break

OUT = os.path.join(ROOT, "host-apk", "build-manual", "gen-androidx")

R_CLS = re.compile(r"^L([\w/]+)/R(\$[\w$]+)?;$")


def main():
    if not APK:
        print("找不到原 APK")
        return 1
    os.makedirs(OUT, exist_ok=True)

    # pkg -> {type: {field: value}}
    data = defaultdict(lambda: defaultdict(dict))
    ncls = 0

    z = zipfile.ZipFile(APK)
    for dn in sorted(n for n in z.namelist() if n.endswith(".dex")):
        d = DEX(z.read(dn))
        for c in d.get_classes():
            name = c.get_name()
            m = R_CLS.match(name)
            if not m:
                continue
            pkg = m.group(1).replace("/", ".")
            typ = (m.group(2) or "").lstrip("$")     # "" 或 "String" 等
            # 收集 static final int 字段
            vals = {}
            try:
                for f in c.get_fields():
                    try:
                        acc = f.get_access_flags_string() or ""
                    except Exception:
                        acc = ""
                    if "static" not in acc:
                        continue
                    # 从 <clinit> 或字段初始值里取 —— androguard 不直接给，
                    # 改从 EncodedField 的初始值读
                    try:
                        init = f.get_init_value()
                        if init is not None:
                            v = init.get_value() if hasattr(init, "get_value") else init
                            if isinstance(v, int):
                                vals[f.get_name()] = v
                    except Exception:
                        pass
            except Exception:
                pass

            if vals:
                data[pkg][typ].update(vals)
                ncls += 1

    print("从原 APK 提取到 %d 个 R 类（带初始值的字段）" % ncls)
    if not ncls:
        print()
        print("提示：androguard 可能读不到字段初始值。")
        print("改用另一条路：从原 APK 的 dex 直接反编译 R 类（jadx 已产出）。")
        return 1

    # 写 Java
    nf = 0
    for pkg, types in data.items():
        pkgdir = os.path.join(OUT, pkg.replace(".", os.sep))
        os.makedirs(pkgdir, exist_ok=True)
        with open(os.path.join(pkgdir, "R.java"), "w", encoding="utf-8") as f:
            f.write("package %s;\n\n" % pkg)
            f.write("public final class R {\n")
            for t, fields in sorted(types.items()):
                if not t:
                    continue
                f.write("    public static final class %s {\n" % t)
                for k, v in sorted(fields.items()):
                    f.write("        public static final int %s = %d;\n" % (k, v))
                    nf += 1
                f.write("    }\n")
            f.write("}\n")
    print("写出 %d 个文件，%d 个字段 -> %s" % (len(data), nf, OUT))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
