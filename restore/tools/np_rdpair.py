#!/usr/bin/env python3
"""Pair CHE rd_* config keys with their nearest Chinese UI label."""
import re, sys, bisect
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_cjk import nonascii_runs
from np_menu import SO_CHE, sections

d = open(SO_CHE, "rb").read()
ro = next(x for x in sections(d) if x["sname"] == ".rodata")
raw = d[ro["offset"]:ro["offset"] + ro["size"]]
base = ro["offset"]

# all CJK strings in rodata with offsets
cjk = sorted(((off, t) for sec, off, t in nonascii_runs(SO_CHE)
              if sec == ".rodata" and any("\u4e00" <= c <= "\u9fff" for c in t)),
             key=lambda x: x[0])
coffs = [o for o, _ in cjk]

keys = []
for m in re.finditer(rb"rd_[a-z0-9]+", raw):
    keys.append((base + m.start(), m.group().decode()))
keys = sorted(set(keys))

def nearest(off, window=0x180):
    i = bisect.bisect_left(coffs, off)
    best = None
    for j in (i - 1, i, i + 1, i + 2):
        if 0 <= j < len(cjk):
            o, t = cjk[j]
            dist = abs(o - off)
            if dist <= window and (best is None or dist < best[0]):
                best = (dist, t)
    return best[1] if best else "?"

print("### CHE rd_* key  <->  Chinese label ###")
print("%-24s %-8s %s" % ("key", "offset", "label"))
print("-" * 70)
for off, k in keys:
    print("%-24s 0x%06X %s" % (k, off, nearest(off)))
print()
print("total rd_* keys =", len(keys))

# also: roledata_enable + set_Total* + section headers
print()
print("### CHE roledata section headers / actions ###")
for off, t in cjk:
    if 0xEF000 <= off <= 0x100200 and re.search(
            r"(属性|抗性|攻击|防御|增幅|减免|穿透|恢复|速度|暴击|格挡|命中|闪避|生命|法力|伤害|基础|高级|元素|战斗)", t):
        print("  0x%06X %s" % (off, t))
