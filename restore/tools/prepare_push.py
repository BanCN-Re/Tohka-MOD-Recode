#!/usr/bin/env python3
"""整理推送目录：把最新的还原成果、修复记录与构建产物收进 _push/。

与上次推送的差异：
  - host-apk/src/main/java 现在是可编译版本（含全部修复与 AndroidX R 类）
  - 新增 COMPILE-FIXES.md（编译错误的根因与修复记录）
  - 新增 tools/ 下的还原工具链（14 个脚本）
  - APK 不进仓库，走 Release（.gitignore 已排除）
"""
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
PUSH = os.path.join(ROOT, "_push")

# 要同步的文件/目录（相对 restore/）
FILES = [
    "README.md", "ARCHITECTURE.md", "VERIFY.md", "VERIFY-DIFF.md",
    "COMPILE-FIXES.md", "compile-errors.md",
]
DIRS = [
    "docs", "tools", "injector", "payload", "injected-dex",
]
TOP = {
    "PUSH-README.md": "README.md",
    "PUSH-LICENSE.md": "LICENSE",
    "PUSH-DISCLAIMER.md": "DISCLAIMER.md",
    "PUSH-CREDITS.md": "CREDITS.md",
}


def copy_tree(src, dst, skip=None):
    skip = skip or []
    n = 0
    for dp, dn, fn in os.walk(src):
        rel = os.path.relpath(dp, src)
        if any(s in rel for s in skip):
            continue
        for f in fn:
            if any(f.endswith(s) for s in (".pyc",)):
                continue
            s = os.path.join(dp, f)
            d = os.path.join(dst, rel, f) if rel != "." else os.path.join(dst, f)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(s, d)
            n += 1
    return n


def main():
    os.makedirs(PUSH, exist_ok=True)

    # 1) 顶层文档（改名）
    for src, dst in TOP.items():
        s = os.path.join(ROOT, src)
        if os.path.exists(s):
            shutil.copy2(s, os.path.join(PUSH, dst))
            print("  %-24s -> %s" % (src, dst))

    # 2) restore 下的文档
    for f in FILES:
        s = os.path.join(ROOT, f)
        if os.path.exists(s):
            d = os.path.join(PUSH, "restore", f)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(s, d)
            print("  restore/%s" % f)

    # 3) restore 下的目录
    for dname in DIRS:
        s = os.path.join(ROOT, dname)
        if not os.path.isdir(s):
            continue
        d = os.path.join(PUSH, "restore", dname)
        if os.path.isdir(d):
            shutil.rmtree(d)
        n = copy_tree(s, d, skip=["__pycache__", "_deps", "build-manual",
                                  "jadx", "jadx2", "jadx3", "_jadx",
                                  "_verify", "_syntax", "_quick-out"])
        print("  restore/%-14s %d 个文件" % (dname, n))

    # 4) 可编译工程（源码 + 资源 + manifest + gradle）
    host_src = os.path.join(ROOT, "host-apk", "src", "main")
    host_dst = os.path.join(PUSH, "restore", "host-apk")
    if os.path.isdir(host_src):
        for sub in ("java", "res"):
            s = os.path.join(host_src, sub)
            d = os.path.join(host_dst, "src", "main", sub)
            if os.path.isdir(d):
                shutil.rmtree(d)
            if os.path.isdir(s):
                n = copy_tree(s, d)
                print("  host-apk/src/main/%-6s %d 个文件" % (sub, n))
        mf = os.path.join(host_src, "AndroidManifest.xml")
        if os.path.exists(mf):
            d = os.path.join(host_dst, "src", "main", "AndroidManifest.xml")
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(mf, d)
            print("  host-apk/src/main/AndroidManifest.xml")
    for f in ("build.gradle", "settings.gradle", "gradle.properties"):
        s = os.path.join(ROOT, "host-apk", f)
        if os.path.exists(s):
            shutil.copy2(s, os.path.join(host_dst, f))
            print("  host-apk/%s" % f)

    # 5) 统计
    tot = sum(len(fn) for _, _, fn in os.walk(PUSH))
    size = sum(os.path.getsize(os.path.join(dp, f))
               for dp, _, fn in os.walk(PUSH) for f in fn)
    print()
    print("_push: %d 个文件, %.2f MB" % (tot, size / 1024 / 1024))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
