#!/usr/bin/env python3
"""把 jadx 生成的顶层 `R$Type` 类合并成规范的嵌套 `R` 类。

## 问题

jadx 反编译 AndroidX 的 R 类时，产出的是**顶层类**：

    package androidx.startup;
    public final class R$string {
        public static int androidx_startup = 2131623963;
    }

而 Java 源码里引用的是**嵌套**形式：

    androidx.startup.R.string.androidx_startup

两者在 Java 源码层面不等价（虽然编译成 class 后名字相同）。
我们的源码（AlGui 框架）用的是嵌套形式，所以必须转成：

    package androidx.startup;
    public final class R {
        public static final class string {
            public static final int androidx_startup = 2131623963;
        }
    }

## 做法

对每个包，收集它所有的 `R$Type.java`，合并到一个 `R.java`，
把每个 `$Type` 变成内部类。同时删掉那些 `R$Type.java`。

用法:
    python tools/merge_r_classes.py --check
    python tools/merge_r_classes.py
"""
import os
import re
import sys
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")

# 匹配顶层 R$Type 类
TOP_R = re.compile(
    r"package\s+([\w.]+)\s*;\s*"
    r"(?:/\*[^*]*\*/\s*)*"
    r"public\s+final\s+class\s+R\$(\w+)\s*\{(.*?)\n\}",
    re.S)
# 字段： public static [final] int name = value;
FIELD = re.compile(
    r"public\s+static\s+(?:final\s+)?int\s+(\w+)\s*=\s*(-?\d+)\s*;")


def main():
    dry = "--check" in sys.argv

    # pkg -> {Type: {field: value}}
    by_pkg = defaultdict(lambda: defaultdict(dict))
    files = []
    for dp, dn, fn in os.walk(JAVA):
        for f in fn:
            if not f.startswith("R$") or not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            txt = open(p, encoding="utf-8").read()
            m = TOP_R.search(txt)
            if not m:
                continue
            pkg, typ, body = m.group(1), m.group(2), m.group(3)
            for fm in FIELD.finditer(body):
                by_pkg[pkg][typ][fm.group(1)] = int(fm.group(2))
            files.append((p, pkg, typ))

    if not files:
        print("没找到顶层 R$Type 类")
        return 0

    print("找到 %d 个 R$Type 文件，涉及 %d 个包" % (len(files), len(by_pkg)))

    # 写合并后的 R.java
    written = 0
    for pkg, types in by_pkg.items():
        pkgdir = os.path.join(JAVA, pkg.replace(".", os.sep))
        os.makedirs(pkgdir, exist_ok=True)
        out = os.path.join(pkgdir, "R.java")
        lines = ["package %s;" % pkg, "",
                 "/* 由 tools/merge_r_classes.py 从 jadx 的顶层 R$Type 合并而来。",
                 " * 这些是 AndroidX/Material 的资源索引类，值来自原 APK 的 dex。",
                 " */",
                 "public final class R {"]
        for t in sorted(types):
            lines.append("    public static final class %s {" % t)
            for k, v in sorted(types[t].items()):
                lines.append("        public static final int %s = %d;" % (k, v))
            lines.append("    }")
        lines.append("}")
        if not dry:
            open(out, "w", encoding="utf-8").write("\n".join(lines) + "\n")
        written += 1

    # 删掉顶层文件
    removed = 0
    if not dry:
        for p, pkg, typ in files:
            try:
                os.remove(p)
                removed += 1
            except Exception:
                pass

    print("%s %d 个 R.java，删除 %d 个 R$Type.java"
          % ("将写" if dry else "已写", written, removed))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
