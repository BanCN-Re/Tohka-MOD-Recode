#!/usr/bin/env python3
"""Extract and diff the Chinese (CJK) UI strings for the menu tree."""
import re, sys, collections, json
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_cjk import nonascii_runs
from np_menu import SO_ARK, SO_CHE

def cjk_map(path):
    got = nonascii_runs(path)
    m = {}
    for sec, off, t in got:
        if sec != ".rodata":
            continue
        if not any("\u4e00" <= c <= "\u9fff" for c in t):
            continue
        m.setdefault(t.strip(), off)
    return m

if __name__ == "__main__":
    a = cjk_map(SO_ARK); c = cjk_map(SO_CHE)
    print("ARK CJK uniq=%d  CHE CJK uniq=%d  shared=%d" % (len(a), len(c), len(set(a) & set(c))))
    print("ARK-only=%d  CHE-only=%d" % (len(set(a) - set(c)), len(set(c) - set(a))))
    print()
    mode = sys.argv[1] if len(sys.argv) > 1 else "ark"
    if mode == "ark":
        print("### ALL ARK CJK strings (by offset) ###")
        for t, o in sorted(a.items(), key=lambda kv: kv[1]):
            print("  0x%06X %s" % (o, t))
    elif mode == "che":
        print("### ALL CHE CJK strings (by offset) ###")
        for t, o in sorted(c.items(), key=lambda kv: kv[1]):
            print("  0x%06X %s" % (o, t))
    elif mode == "arkonly":
        print("### ARK-only CJK ###")
        for t in sorted(set(a) - set(c), key=lambda x: a[x]):
            print("  0x%06X %s" % (a[t], t))
    elif mode == "cheonly":
        print("### CHE-only CJK ###")
        for t in sorted(set(c) - set(a), key=lambda x: c[x]):
            print("  0x%06X %s" % (c[t], t))
    json.dump({"ark": a, "che": c}, open(r"D:\Work\MuMuP\ArkReCodeHack\restore\tools\cjk_map.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
