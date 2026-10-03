"""从原 APK 的 dex 里提取 AndroidX / Material 的类，打成 jar 作为编译依赖。

思路：
  原 APK 的 classes*.dex 里已经包含了全部 AndroidX 类的字节码。
  用 androguard 把它们导出成 .class 文件（或直接用 d8 的反向），
  打包成 deps.jar，供 javac 编译我们还原的源码时使用。

实际上更简单的路子：直接用 dex2jar 思路 —— 但这里没有。
所以退一步：用 androguard 读 dex，把每个 AndroidX 类的方法签名导出成
**stub 源码**（只有签名、空实现），再编译成 jar。
这样 javac 能通过，且不涉及任何实现（我们也不需要它们的实现）。

产出: restore/host-apk/_deps/stubs.jar
"""
import os
import re
import zipfile
from collections import defaultdict

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass

from androguard.core.dex import DEX

HERE = os.path.dirname(os.path.abspath(__file__))
R = os.path.dirname(HERE)
ROOT = os.path.dirname(R)
APK = os.path.join(ROOT, "Riruriru Mod_1.1.2.apk.1")
OUT = os.path.join(R, "host-apk", "_deps")

# 需要打桩的包前缀（我们源码 import 了它们）
WANT = ("Landroidx/", "Lcom/google/android/material/", "Lcom/google/android/",
        "Lkotlin/", "Lkotlinx/", "Lokhttp3/", "Lokio/", "Lorg/jetbrains/",
        "Lorg/intellij/", "Lirene/")


def jtype(d):
    PRIM = {"V": "void", "Z": "boolean", "B": "byte", "S": "short",
            "C": "char", "I": "int", "J": "long", "F": "float", "D": "double"}
    if not d:
        return "void"
    if d in PRIM:
        return PRIM[d]
    if d.startswith("["):
        return jtype(d[1:]) + "[]"
    if d.startswith("L") and d.endswith(";"):
        return d[1:-1].replace("/", ".").replace("$", ".")
    return "Object"


def main():
    os.makedirs(OUT, exist_ok=True)
    z = zipfile.ZipFile(APK)
    dex_names = sorted(n for n in z.namelist() if n.endswith(".dex"))

    # 收集需要的类
    classes = {}          # 类名 -> (父类, [(方法名, 描述符, 静态?), (字段名,类型,静态?)])
    for dn in dex_names:
        d = DEX(z.read(dn))
        for c in d.get_classes():
            cn = c.get_name()
            if not any(cn.startswith(p) for p in WANT):
                continue
            if cn in classes:
                continue
            try:
                sup = c.get_superclassname() or "Ljava/lang/Object;"
            except Exception:
                sup = "Ljava/lang/Object;"
            ms = []
            for m in c.get_methods():
                try:
                    ms.append((m.get_name(), m.get_descriptor(),
                               "static" in (m.get_access_flags_string() or "")))
                except Exception:
                    pass
            fs = []
            for f in c.get_fields():
                try:
                    fs.append((f.get_name(), f.get_descriptor(),
                               "static" in (f.get_access_flags_string() or "")))
                except Exception:
                    pass
            classes[cn] = (sup, ms, fs)

    print("收集到 %d 个需要打桩的类" % len(classes))

    # 生成 stub 源码
    srcdir = os.path.join(OUT, "stub-src")
    nfile = 0
    for cn, (sup, ms, fs) in classes.items():
        if cn.startswith("Lirene/"):
            continue                     # irene 是我们自己的代码，不打桩
        fq = cn[1:-1].replace("/", ".")
        pkg, _, simple = fq.rpartition(".")
        sup_j = jtype(sup)
        sup_simple = sup_j.rsplit(".", 1)[-1] if "." in sup_j else sup_j
        lines = ["package %s;" % pkg if pkg else "", ""]
        lines.append("public class %s {" % re.sub(r"[^0-9A-Za-z_$]", "_", simple.replace("$", "_")))
        seen = set()
        for name, desc, is_static in ms:
            if name in ("<init>", "<clinit>"):
                continue
            if name in seen:
                continue
            seen.add(name)
            m = re.match(r"^\((.*)\)(.*)$", desc)
            if not m:
                continue
            ps, rt = m.group(1), m.group(2)
            args, i = [], 0
            while i < len(ps):
                ch = ps[i]
                if ch == "[":
                    j = i
                    while j < len(ps) and ps[j] == "[":
                        j += 1
                    if j < len(ps) and ps[j] == "L":
                        j = ps.index(";", j)
                    args.append(jtype(ps[i:j + 1]))
                    i = j + 1
                elif ch == "L":
                    j = ps.index(";", i)
                    args.append(jtype(ps[i:j + 1]))
                    i = j + 1
                elif ch in "ZBSCIJFD":
                    args.append(jtype(ch))
                    i += 1
                else:
                    i += 1
            ret = jtype(rt)
            st = "static " if is_static else ""
            sig = ", ".join("%s a%d" % (t, k) for k, t in enumerate(args))
            body = "" if ret == "void" else _default(ret)
            # 方法名要做合法化：lambda$... 里的 '-' 不是合法 Java 标识符
            safe = re.sub(r"[^0-9A-Za-z_$]", "_", name)
            lines.append("    public %s%s %s(%s) { %s }" %
                         (st, ret, safe, sig, body))
        lines.append("}")
        d2 = os.path.join(srcdir, pkg.replace(".", os.sep)) if pkg else srcdir
        os.makedirs(d2, exist_ok=True)
        with open(os.path.join(d2, simple.replace("$", "_") + ".java"),
                  "w", encoding="utf-8") as fh:
            fh.write("\n".join(lines) + "\n")
        nfile += 1

    print("生成 stub 源码 %d 个 -> %s" % (nfile, srcdir))


def _default(t):
    if t in ("int", "short", "byte", "char"):
        return "return 0;"
    if t == "long":
        return "return 0L;"
    if t == "float":
        return "return 0f;"
    if t == "double":
        return "return 0d;"
    if t == "boolean":
        return "return false;"
    return "return null;"


if __name__ == "__main__":
    main()
