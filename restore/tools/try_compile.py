#!/usr/bin/env python3
"""快速编译检查：绕开 Gradle，直接用 javac 编译还原的源码。

为什么需要它：
  Gradle 会把 javac 的错误信息截断到约 100 字符，看不清具体原因。
  直接跑 javac 能拿到完整消息，而且迭代快（秒级 vs 分钟级）。

classpath 来源：Gradle 已经下载/解压好的依赖
  ~/.gradle/caches/transforms-*/**/classes.jar   （AAR 解出来的）
  ~/.gradle/caches/modules-2/**/*.jar            （普通 jar）

用法:
  python tools/try_compile.py            # 编译并列出错误
  python tools/try_compile.py --quiet    # 只报数量
"""
import os
import re
import subprocess
import sys
import glob
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
HOST = os.path.join(ROOT, "host-apk")
JAVA_SRC = os.path.join(HOST, "src", "main", "java")
DEPS = os.path.join(HOST, "_deps")
OUT = os.path.join(HOST, "_javac-out")

SDK = os.environ.get("ANDROID_HOME") or os.path.join(
    os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk")
ANDROID_JAR = os.path.join(SDK, "platforms", "android-35", "android.jar")
JAVAC = r"C:\Program Files\Java\jdk-17\bin\javac.exe"
GRADLE_HOME = os.path.join(os.environ.get("USERPROFILE", ""), ".gradle")


def find_dep_jars():
    jars = []
    # 1) transforms 缓存里的 classes.jar（AAR 解出来的）
    for pat in ("caches/transforms-*/**/classes.jar",
                "caches/transforms-*/**/jars/classes.jar",
                "caches/*/transforms/**/classes.jar"):
        jars += glob.glob(os.path.join(GRADLE_HOME, pat), recursive=True)
    # 2) modules-2 里的普通 jar
    for p in glob.glob(os.path.join(GRADLE_HOME, "caches", "modules-2",
                                    "files-2.1", "**", "*.jar"), recursive=True):
        low = p.lower()
        if "sources" in low or "javadoc" in low:
            continue
        jars.append(p)
    # 去重
    seen, out = set(), []
    for j in jars:
        base = os.path.basename(j)
        if base in seen:
            continue
        seen.add(base)
        out.append(j)
    return out


def main():
    quiet = "--quiet" in sys.argv
    os.makedirs(OUT, exist_ok=True)

    deps = find_dep_jars()
    cp = os.pathsep.join([ANDROID_JAR] + deps)

    files = []
    for dp, dn, fn in os.walk(JAVA_SRC):
        for f in fn:
            if f.endswith(".java"):
                files.append(os.path.join(dp, f).replace("\\", "/"))

    if not quiet:
        print("android.jar : %s" % ("有" if os.path.exists(ANDROID_JAR) else "缺失"))
        print("依赖 jar    : %d 个" % len(deps))
        print("源文件      : %d 个" % len(files))

    os.makedirs(DEPS, exist_ok=True)

    # 命令行会超长（288 个 jar），所以 classpath 与源文件都走 argfile
    cpfile = os.path.join(DEPS, "cp.args")
    with open(cpfile, "w", encoding="utf-8", newline="\n") as f:
        f.write('-cp "%s"\n' % cp)

    argfile = os.path.join(DEPS, "compile.args")
    with open(argfile, "w", encoding="utf-8", newline="\n") as f:
        for p in files:
            f.write('"%s"\n' % p)

    # javac 的 @argfile 里 -cp 与源文件要分开写；直接调会把源文件当参数吞掉。
    # 这里全部塞进同一个 argfile，最稳。
    allargs = os.path.join(DEPS, "all.args")
    with open(allargs, "w", encoding="utf-8", newline="\n") as f:
        f.write('-encoding UTF-8\n')
        f.write('-nowarn\n-proc:none\n')
        f.write('-Xmaxerrs 2000\n')
        f.write('-d "%s"\n' % OUT.replace("\\", "/"))
        f.write('-cp "%s"\n' % cp.replace("\\", "/"))
        for p in files:
            f.write('"%s"\n' % p.replace("\\", "/"))

    cmd = [JAVAC, "@" + allargs]
    r = subprocess.run(cmd, capture_output=True, text=True,
                       encoding="utf-8", errors="replace")

    out = (r.stdout or "") + (r.stderr or "")

    # 校验产物 —— 只看退出码会被 javac 的静默跳过骗到
    ncls = 0
    if os.path.isdir(OUT):
        for dp, dn, fn in os.walk(OUT):
            ncls += sum(1 for x in fn if x.endswith(".class"))
    print("生成 class : %d 个" % ncls)
    if ncls == 0 and "error" not in out.lower():
        print("!! 没有任何 class 产出，编译实际没执行 —— 检查 argfile")

    # 解析错误
    errs = []
    for m in re.finditer(r"([A-Za-z0-9_$]+\.java):(\d+): error: (.+)", out):
        errs.append((m.group(1), int(m.group(2)), m.group(3).strip()))

    print()
    if not errs:
        print("*** 编译通过，0 错误 ***")
        return 0

    print("错误 %d 条" % len(errs))
    by_file = Counter(e[0] for e in errs)
    print()
    print("按文件:")
    for f, c in by_file.most_common():
        print("  %-40s %4d" % (f, c))

    # 归一化错误消息便于归类
    norm = Counter()
    for _, _, msg in errs:
        k = re.sub(r"'[^']*'", "'X'", msg)
        k = re.sub(r'"[^"]*"', '"X"', k)
        norm[k] += 1
    print()
    print("按类型 TOP 20:")
    for k, c in norm.most_common(20):
        print("  %4d  %s" % (c, k[:110]))

    if not quiet:
        print()
        print("前 40 条明细:")
        for f, ln, msg in errs[:40]:
            print("  %s:%d" % (f, ln))
            print("      %s" % msg[:160])

    # 把完整输出落盘
    with open(os.path.join(ROOT, "_javac_full.txt"), "w",
              encoding="utf-8") as fh:
        fh.write(out)
    print()
    print("完整输出 -> _javac_full.txt")
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
