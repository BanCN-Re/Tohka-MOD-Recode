#!/usr/bin/env python3
"""把剩下的 `R$styleable` 顶层类转成嵌套 `R.styleable`。

`styleable` 与其它 R 子类不同：它的字段是 **int[]**（属性 ID 数组），
而且会引用 `R$attr` 里的单个属性。

转换后：
    package androidx.appcompat.resources;
    public final class R {
        public static final class styleable {
            public static final int[] Xxx = { attr.a, attr.b };
            ...
        }
    }

引用 `R$attr.xxx` 改成 `R.attr.xxx`（同包内可直接用 `attr.xxx`）。

用法:
    python tools/fix_styleable.py
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")


def main():
    dry = "--check" in sys.argv
    done = 0

    for dp, dn, fn in os.walk(JAVA):
        for f in fn:
            if f != "R$styleable.java":
                continue
            p = os.path.join(dp, f)
            txt = open(p, encoding="utf-8").read()

            m = re.search(r"package\s+([\w.]+)\s*;", txt)
            if not m:
                continue
            pkg = m.group(1)

            # 取出类体
            bm = re.search(r"public\s+final\s+class\s+R\$styleable\s*\{(.*)\}\s*$",
                           txt, re.S)
            if not bm:
                print("  [跳过] 结构不识别: %s" % p)
                continue
            body = bm.group(1)

            # 处理字段
            out_fields = []
            for line in body.splitlines():
                s = line.strip()
                if not s or s.startswith("//") or s.startswith("/*") \
                        or s.startswith("*") or s.startswith("private"):
                    continue
                # int[] 形式： public static int[] Name = {R$attr.a, ...};
                am = re.match(r"public\s+static\s+int\[\]\s+(\w+)\s*=\s*\{(.*?)\}\s*;",
                              s)
                if am:
                    name, items = am.group(1), am.group(2)
                    # R$attr.x -> attr.x
                    items = re.sub(r"R\$(\w+)\.", r"\1.", items)
                    items = items.replace("android.R$attr", "android.R.attr")
                    out_fields.append("        public static final int[] %s = {%s};"
                                      % (name, items.strip()))
                    continue
                # int 形式
                im = re.match(r"public\s+static\s+(?:final\s+)?int\s+(\w+)\s*=\s*(-?\d+)\s*;", s)
                if im:
                    out_fields.append("        public static final int %s = %s;"
                                      % (im.group(1), im.group(2)))
                    continue

            # 写成嵌套版本，追加到同包的 R.java
            rjava = os.path.join(dp, "R.java")
            lines = ["package %s;" % pkg, ""]
            if os.path.exists(rjava):
                old = open(rjava, encoding="utf-8").read()
                # 在最后一个 } 前插入 styleable 内部类
                idx = old.rstrip().rfind("}")
                block = ["    public static final class styleable {"]
                block += out_fields
                block.append("    }")
                new = old[:idx] + "\n".join(block) + "\n" + old[idx:]
            else:
                new = "\n".join([
                    "package %s;" % pkg, "",
                    "public final class R {",
                    "    public static final class styleable {",
                ] + out_fields + [
                    "    }",
                    "}", ""])

            if not dry:
                open(rjava, "w", encoding="utf-8").write(new)
                os.remove(p)
            print("  合并 %s -> R.java（%d 个字段）" % (p, len(out_fields)))
            done += 1

    print()
    print("%s %d 个 styleable" % ("将处理" if dry else "已处理", done))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
