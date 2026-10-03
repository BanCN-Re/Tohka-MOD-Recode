#!/usr/bin/env python3
"""从 Gradle 构建日志里提取 javac 错误，归类并生成可读报告。

Gradle 输出是 GBK 编码，Python 按 UTF-8 读会乱码 —— 这里按实际编码尝试。
"""
import os
import re
import sys
from collections import Counter, defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
LOG = os.path.join(ROOT, "_gradle_build.log")
OUT = os.path.join(ROOT, "compile-errors.md")

RAW = open(LOG, "rb").read()


def decode(raw):
    """Gradle 在 Windows 上按控制台代码页输出，试几种编码"""
    for enc in ("utf-8", "gbk", "cp936", "latin1"):
        try:
            s = raw.decode(enc)
            # 看中文错误关键词能不能出来
            if "错误:" in s or "error:" in s:
                return s, enc
        except Exception:
            continue
    return raw.decode("utf-8", errors="replace"), "utf-8(replace)"


def main():
    text, enc = decode(RAW)
    print("日志编码: %s" % enc)

    lines = text.splitlines()

    # 收集形如  path.java:NNN: 错误: msg
    errs = []
    for i, ln in enumerate(lines):
        m = re.search(r"([A-Za-z0-9_$\\/.:]+\.java):(\d+):\s*(?:错误|error):\s*(.*)$", ln)
        if m:
            errs.append({
                "file": m.group(1).split("\\")[-1],
                "line": int(m.group(2)),
                "msg": m.group(3).strip(),
            })

    # 去重（同一位置可能重复打印）
    seen = set()
    uniq = []
    for e in errs:
        k = (e["file"], e["line"], e["msg"])
        if k in seen:
            continue
        seen.add(k)
        uniq.append(e)

    print("错误条目: %d（去重前 %d）" % (len(uniq), len(errs)))

    by_file = Counter(e["file"] for e in uniq)
    by_msg = Counter(e["msg"] for e in uniq)

    lines_out = []
    def p(s=""):
        lines_out.append(str(s))

    p("# javac 编译错误清单")
    p()
    p("来源：`_gradle_build.log`（编码 %s）" % enc)
    p()
    p("**共 %d 条错误**（去重后）" % len(uniq))
    p()
    p("## 按文件分布")
    p()
    p("| 文件 | 错误数 |")
    p("| --- | ---: |")
    for f, c in by_file.most_common():
        p("| `%s` | %d |" % (f, c))
    p()
    p("## 按错误类型分布")
    p()
    p("| 错误信息 | 次数 |")
    p("| --- | ---: |")
    for m, c in by_msg.most_common(30):
        p("| %s | %d |" % (m.replace("|", "\\|")[:120], c))
    p()
    p("## 全部错误（按文件分组）")
    p()
    grouped = defaultdict(list)
    for e in uniq:
        grouped[e["file"]].append(e)
    for f in sorted(grouped):
        p("### %s（%d 条）" % (f, len(grouped[f])))
        p()
        p("| 行 | 错误 |")
        p("| ---: | --- |")
        for e in grouped[f][:120]:
            p("| %d | %s |" % (e["line"], e["msg"].replace("|", "\\|")[:150]))
        if len(grouped[f]) > 120:
            p("| ... | 还有 %d 条 |" % (len(grouped[f]) - 120))
        p()

    open(OUT, "w", encoding="utf-8").write("\n".join(lines_out) + "\n")
    print("写出:", OUT)


if __name__ == "__main__":
    main()
