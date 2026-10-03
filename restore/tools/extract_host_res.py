"""提取 host APK 的全部资源，并解析 resources.arsc 里自有包用到的条目。

产出 restore/host-apk/res/ 下的原始资源文件，
以及 restore/host-apk/resources.txt 里自有资源的清单。
"""
import os
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
APK = os.path.join(ROOT, "Riruriru Mod_1.1.2.apk.1")
OUT = os.path.join(ROOT, "restore", "host-apk")

os.makedirs(OUT, exist_ok=True)

z = zipfile.ZipFile(APK)

# 1) 全部资源类文件解出来
res_dir = os.path.join(OUT, "res")
n = 0
entries = []
for info in z.infolist():
    name = info.filename
    if not (name.startswith("res/") or name.startswith("assets/")
            or name in ("resources.arsc", "AndroidManifest.xml")):
        continue
    dst = os.path.join(OUT, name.replace("/", os.sep))
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with open(dst, "wb") as f:
        f.write(z.read(name))
    entries.append("%-72s %10d" % (name, info.file_size))
    n += 1

print("解出资源文件 %d 个" % n)

# 2) 按类型统计
from collections import Counter
types = Counter()
for e in entries:
    nm = e.split()[0]
    if nm.startswith("res/"):
        ext = os.path.splitext(nm)[1] or "(noext)"
        types[ext] += 1
    elif nm.startswith("assets/"):
        types["assets"] += 1

print()
print("资源类型分布:")
for k, v in types.most_common():
    print("  %-10s %d" % (k, v))

# 3) 自有资源（R 类里引用的 id 名字）
# R$drawable / R$layout / R$string 等值可以从 classes2.dex 的 R 类里读
with open(os.path.join(OUT, "res-filelist.txt"), "w", encoding="utf-8") as f:
    f.write("\n".join(sorted(entries)) + "\n")

print()
print("清单: res-filelist.txt")
