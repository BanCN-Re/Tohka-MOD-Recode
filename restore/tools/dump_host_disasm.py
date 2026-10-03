"""输出 host APK 自有类的完整反汇编（smali 风格），供源码还原用。

androguard 4.x 的 get_source() 不可用，改用 get_bc().get_instructions()
自己按 smali 语法拼。这份文本是我们还原 Java 源码的依据。
"""
import os
import re
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
OUT = os.path.join(ROOT, "restore", "host-apk", "disasm")

OWN = re.compile(r"^L(com/Riruriru|com/example)/")
os.makedirs(OUT, exist_ok=True)


def cls_to_path(name):
    return name.strip("L;")


def main():
    z = zipfile.ZipFile(APK)
    dex_names = sorted(n for n in z.namelist() if n.endswith(".dex"))

    index = []
    ncls = 0
    for dn in dex_names:
        d = DEX(z.read(dn))
        for c in d.get_classes():
            cn = c.get_name()
            if not OWN.match(cn):
                continue
            rel = cls_to_path(cn)
            sp = os.path.join(OUT, rel.replace("/", os.sep) + ".smali")
            os.makedirs(os.path.dirname(sp), exist_ok=True)

            lines = []
            try:
                lines.append("### %s   (from %s)" % (rel, dn))
                try:
                    lines.append("### super: %s" % c.get_superclassname())
                except Exception:
                    pass
                try:
                    lines.append("### access: %s" % c.get_access_flags_string())
                except Exception:
                    pass
                lines.append("")

                # 字段
                try:
                    fields = list(c.get_fields())
                except Exception:
                    fields = []
                if fields:
                    lines.append("# ---------- 字段 ----------")
                    for f in fields:
                        try:
                            acc = f.get_access_flags_string()
                        except Exception:
                            acc = ""
                        try:
                            t = f.get_descriptor()
                        except Exception:
                            t = "?"
                        lines.append(".field %s %s %s" % (acc, f.get_name(), t))
                    lines.append("")

                # 方法
                for m in c.get_methods():
                    try:
                        acc = m.get_access_flags_string()
                    except Exception:
                        acc = ""
                    try:
                        desc = m.get_descriptor()
                    except Exception:
                        desc = "()V"
                    lines.append("# ---------- %s%s (%s) ----------" %
                                 (m.get_name(), desc, acc))
                    code = m.get_code()
                    if code is None:
                        lines.append("    (无字节码)")
                        lines.append("")
                        continue
                    try:
                        for ins in code.get_bc().get_instructions():
                            lines.append("    %-14s %s" %
                                         (ins.get_name(), ins.get_output()))
                    except Exception as e:
                        lines.append("    (反汇编失败: %r)" % e)
                    lines.append("")

            except Exception as e:
                lines.append("### 解析异常: %r" % e)

            with open(sp, "w", encoding="utf-8") as f:
                f.write("\n".join(lines) + "\n")
            ncls += 1
            index.append("%s\t%s\t%d 行" % (rel, dn, len(lines)))

    with open(os.path.join(OUT, "_index.txt"), "w", encoding="utf-8") as f:
        f.write("\n".join(sorted(index)) + "\n")

    print("反汇编 %d 个自有类" % ncls)
    print("输出:", OUT)


if __name__ == "__main__":
    main()
