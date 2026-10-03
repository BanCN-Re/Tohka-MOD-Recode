#!/usr/bin/env python3
"""核验 GitHub 远端仓库内容（用 gh CLI 取数据，python 解析）。"""
import json
import subprocess
import sys

REPO = "BanCN-Re/Tohka-MOD-Recode"


def gh(*args):
    r = subprocess.run(["gh"] + list(args), capture_output=True, text=True,
                       encoding="utf-8", errors="replace")
    if r.returncode != 0:
        return None
    try:
        return json.loads(r.stdout)
    except Exception:
        return r.stdout


def main():
    print("=== 仓库信息 ===")
    info = gh("repo", "view", REPO, "--json",
              "name,description,visibility,defaultBranchRef,diskUsage,pushedAt")
    if info:
        print("  名称:   %s" % info["name"])
        print("  描述:   %s" % info.get("description"))
        print("  可见性: %s" % info["visibility"])
        print("  分支:   %s" % info["defaultBranchRef"]["name"])
        print("  体积:   %s KB" % info.get("diskUsage"))
        print("  推送于: %s" % info.get("pushedAt"))

    print()
    print("=== 根目录 ===")
    root = gh("api", "repos/%s/contents" % REPO)
    if isinstance(root, list):
        for f in root:
            print("  %-8s %-20s %s" % (f["type"], f["name"], f.get("size", "")))

    for sub in ("restore", "restore/docs", "restore/host-apk",
                "restore/host-apk/src/main/java/com/Riruriru/Sx"):
        print()
        print("=== %s ===" % sub)
        d = gh("api", "repos/%s/contents/%s" % (REPO, sub))
        if isinstance(d, list):
            for f in d[:30]:
                print("  %-8s %s" % (f["type"], f["name"]))
            if len(d) > 30:
                print("  ... 还有 %d 项" % (len(d) - 30))
        else:
            print("  (取不到)")

    print()
    print("=== 提交历史 ===")
    cs = gh("api", "repos/%s/commits" % REPO)
    if isinstance(cs, list):
        for c in cs:
            msg = c["commit"]["message"].split("\n")[0]
            print("  %s  %s" % (c["sha"][:7], msg[:80]))

    print()
    print("=== 文件统计（git tree 递归）===")
    t = gh("api", "repos/%s/git/trees/main?recursive=1" % REPO)
    if isinstance(t, dict):
        blobs = [x for x in t["tree"] if x["type"] == "blob"]
        total = sum(x.get("size", 0) for x in blobs)
        print("  文件 %d 个，合计 %.1f KB" % (len(blobs), total / 1024))

        print()
        print("=== 大件检查 ===")
        big = [x for x in blobs if x.get("size", 0) > 500 * 1024]
        if big:
            for x in big:
                print("  [BIG] %s  %.0f KB" % (x["path"], x["size"] / 1024))
        else:
            print("  [OK] 无超过 500 KB 的文件")

        bad = [x for x in blobs if x["path"].endswith((".so", ".apk", ".dex"))]
        if bad:
            for x in bad:
                print("  [BAD] %s" % x["path"])
        else:
            print("  [OK] 无 .so / .apk / .dex")

        # 按目录统计
        print()
        print("=== 目录分布 ===")
        from collections import Counter
        c = Counter()
        for x in blobs:
            p = x["path"]
            c[p.split("/")[0] if "/" in p else "(root)"] += 1
        for k, v in c.most_common():
            print("  %-20s %d" % (k, v))


if __name__ == "__main__":
    main()
