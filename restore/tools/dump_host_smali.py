"""用 androguard 把 host APK 的全部自有类反汇编成 smali，并生成可读的 Java 伪码。

产出：
  restore/host-apk/smali/<类路径>.smali       每个类的完整 smali
  restore/host-apk/java/<类路径>.java         androguard 反编译的 Java（尽力而为）
  restore/host-apk/classes.txt                类清单
  restore/host-apk/methods.txt                方法清单（含签名）

只处理自有包（com.Riruriru / com.example），androidx/kotlin 等库跳过。
"""
import os
import re
import sys
import zipfile

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass

from androguard.core.dex import DEX

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
APK = os.path.join(ROOT, "Riruriru Mod_1.1.2.apk.1")
OUT = os.path.join(ROOT, "restore", "host-apk")

OWN = re.compile(r"^L(com/Riruriru|com/example)/")
SKIP_PREFIX = (
    "Landroidx/", "Lkotlin/", "Lkotlinx/", "Ljava/", "Ljavax/", "Landroid/",
    "Ldalvik/", "Lorg/", "Lcom/google/",
)

os.makedirs(os.path.join(OUT, "smali"), exist_ok=True)
os.makedirs(os.path.join(OUT, "java"), exist_ok=True)


def cls_to_path(name):
    # Lcom/Riruriru/Sx/MainActivity; -> com/Riruriru/Sx/MainActivity
    n = name.strip("L;")
    return n


def main():
    z = zipfile.ZipFile(APK)
    dex_names = sorted(n for n in z.namelist() if n.endswith(".dex"))
    print("DEX:", dex_names)

    classes_txt = []
    methods_txt = []
    total_smali = 0
    total_java = 0

    for dn in dex_names:
        raw = z.read(dn)
        d = DEX(raw)
        own = [c for c in d.get_classes() if OWN.match(c.get_name())]
        print("%s: %d 个类，其中自有 %d 个" % (dn, len(d.get_classes()), len(own)))

        for c in own:
            cname = c.get_name()
            rel = cls_to_path(cname)
            safe = rel.replace("/", os.sep)

            # --- smali ---
            try:
                src = c.get_source()
                if src:
                    sp = os.path.join(OUT, "smali", safe + ".smali")
                    os.makedirs(os.path.dirname(sp), exist_ok=True)
                    with open(sp, "w", encoding="utf-8") as f:
                        f.write(src)
                    total_smali += 1
            except Exception as e:
                print("  smali 失败 %s: %r" % (rel, e))

            # --- 类清单 ---
            try:
                sup = c.get_superclassname()
            except Exception:
                sup = "?"
            classes_txt.append("%s\tsuper=%s\tdex=%s" % (rel, sup, dn))

            # --- 方法清单 ---
            for m in c.get_methods():
                try:
                    desc = m.get_descriptor()
                except Exception:
                    desc = "?"
                try:
                    acc = m.get_access_flags_string()
                except Exception:
                    acc = "?"
                methods_txt.append("%s->%s%s\t%s" % (rel, m.get_name(), desc, acc))

    with open(os.path.join(OUT, "classes.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(sorted(classes_txt)) + "\n")
    with open(os.path.join(OUT, "methods.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(sorted(methods_txt)) + "\n")

    print()
    print("smali 写出: %d 个类" % total_smali)
    print("类清单: %d 条" % len(classes_txt))
    print("方法清单: %d 条" % len(methods_txt))
    print("输出目录:", OUT)


if __name__ == "__main__":
    main()
