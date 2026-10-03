#!/usr/bin/env python3
"""修复 `--no-inline-anonymous` 版本剩下的少量独立编译错误。

这些都不是结构性问题，逐个精确处理：

1. `MaterialCardViewHelper` 在 material 1.11 里不是 public
   → 它只用于取一个常量 DEFAULT_FADE_ANIM_DURATION（值 150）
   → 直接内联成字面量，删 import

2. `AlguiView` / `AlguiNotification` 枚举里手写的 `valueOf(String)`
   → 与编译器自动生成的冲突，删掉手写那个

3. `androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure`
   → constraintlayout 2.x 已移除该内部包。它只用了一个常量
   → 内联成字面量，删 import

4. `kotlin.jvm.internal.ByteCompanionObject`
   → 只在 ViewTool 里当数值边界用 → 内联字面量，删 import

5. `Process` 歧义：文件同时 import 了 android.os.Process 和用了 java.lang.Process
   → 给 java.lang.Process 加全限定名

用法: python tools/fix_remaining.py [--check]
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")

# MaterialCardViewHelper.DEFAULT_FADE_ANIM_DURATION 的实际值
# （material-components 源码里是 150）
MATERIAL_FADE = "150"
# androidx.constraintlayout ... BasicMeasure.EXACTLY 的值
BASIC_MEASURE_EXACTLY = "1073741824"      # View.MeasureSpec.EXACTLY


def fix_material(src, log):
    if "MaterialCardViewHelper" not in src:
        return src
    n = 0
    # 用常量替换用法
    src, k = re.subn(r"MaterialCardViewHelper\.DEFAULT_FADE_ANIM_DURATION",
                     MATERIAL_FADE, src)
    n += k
    # 删 import
    src, k = re.subn(
        r"import\s+com\.google\.android\.material\.card\.MaterialCardViewHelper;\s*\n",
        "", src)
    n += k
    if n:
        log.append("  MaterialCardViewHelper -> 常量 %s（%d 处）" % (MATERIAL_FADE, n))
    return src


def fix_enum_valueof(src, log):
    """删掉枚举里手写的 valueOf(String)"""
    total = 0
    while True:
        # 匹配 "public static X valueOf(String str) { ... }"
        m = re.search(
            r"[ \t]*public\s+static\s+([A-Za-z_$][\w$]*)\s+valueOf\s*\(\s*String\s+\w+\s*\)\s*\{",
            src)
        if not m:
            break
        # 找方法体结束
        bo = src.find("{", m.end() - 1)
        depth = 0
        i = bo
        n = len(src)
        while i < n:
            if src[i] == "{":
                depth += 1
            elif src[i] == "}":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        # 连带前面的注释行一起去掉
        start = m.start()
        line_start = src.rfind("\n", 0, start) + 1
        # 若上一行是注释（/* ... */ 或 //），把它也删掉
        prev_end = line_start
        prev_start = src.rfind("\n", 0, max(0, prev_end - 1)) + 1
        prev = src[prev_start:prev_end].strip()
        if prev.startswith("/*") or prev.startswith("//"):
            start = prev_start
        src = src[:start] + src[i + 1:]
        total += 1
    if total:
        log.append("  删除手写 valueOf(String) %d 个（与枚举自动生成冲突）" % total)
    return src


def fix_constraint_analyzer(src, log):
    if "constraintlayout.solver.widgets.analyzer" not in src:
        return src
    n = 0
    src, k = re.subn(r"BasicMeasure\.EXACTLY", BASIC_MEASURE_EXACTLY, src)
    n += k
    src, k = re.subn(
        r"import\s+androidx\.constraintlayout\.solver\.widgets\.analyzer\.[\w$.]+;\s*\n",
        "", src)
    n += k
    # 若有裸的 BasicMeasure 类型引用，替换掉
    src = re.sub(r"\bBasicMeasure\b", "Object", src)
    if n:
        log.append("  constraintlayout analyzer 包引用内联（%d 处）" % n)
    return src


def fix_kotlin_companion(src, log):
    if "ByteCompanionObject" not in src:
        return src
    n = 0
    # ByteCompanionObject.MAX_VALUE / MIN_VALUE
    src, k = re.subn(r"ByteCompanionObject\.MAX_VALUE", "127", src)
    n += k
    src, k = re.subn(r"ByteCompanionObject\.MIN_VALUE", "-128", src)
    n += k
    src, k = re.subn(
        r"import\s+kotlin\.jvm\.internal\.[\w$.]+;\s*\n", "", src)
    n += k
    if n:
        log.append("  kotlin ByteCompanionObject 内联（%d 处）" % n)
    return src


def fix_process(src, log):
    """Process 类型歧义：文件里 import android.os.Process 又用 java.lang.Process"""
    if "import android.os.Process;" not in src:
        return src
    if not re.search(r"\bProcess\s+\w+\s*=\s*\w+\.start\(\)", src):
        return src
    n = len(re.findall(r"\bProcess\s+(\w+)\s*=\s*(\w+)\.start\(\)", src))
    src = re.sub(r"\bProcess\s+(\w+)\s*=\s*(\w+)\.start\(\)",
                 r"java.lang.Process \1 = \2.start()", src)
    if n:
        log.append("  Process -> java.lang.Process（%d 处）" % n)
    return src


def fix_file(path, dry=False):
    src0 = open(path, encoding="utf-8").read()
    src = src0
    log = []
    src = fix_material(src, log)
    src = fix_enum_valueof(src, log)
    src = fix_constraint_analyzer(src, log)
    src = fix_kotlin_companion(src, log)
    src = fix_process(src, log)
    if src != src0 and not dry:
        open(path, "w", encoding="utf-8").write(src)
    return log


def main():
    dry = "--check" in sys.argv
    tf = 0
    for dp, dn, fn in os.walk(JAVA):
        for f in sorted(fn):
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            log = fix_file(p, dry)
            if log:
                tf += 1
                print("%s%s" % ("[dry] " if dry else "[fix] ",
                                os.path.relpath(p, JAVA)))
                for x in log:
                    print(x)
    print()
    print("%s %d 个文件" % ("将修改" if dry else "已修改", tf))


if __name__ == "__main__":
    main()
