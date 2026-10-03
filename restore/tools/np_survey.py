#!/usr/bin/env python3
"""Survey the native payload string tables by functional module."""
import re, sys, collections, io

ARK = r"D:\Work\MuMuP\ArkReCodeHack\work\libArkRe-strings.txt"
CHE = r"D:\Work\MuMuP\ArkReCodeHack\work\libCherryTale-strings.txt"

def load(path):
    rows = []
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue
            if "\t" in line:
                sec, s = line.split("\t", 1)
            else:
                sec, s = "", line
            rows.append((sec, s))
    return rows

def uniq_strings(rows):
    out = []
    seen = set()
    for sec, s in rows:
        if s in seen:
            continue
        seen.add(s)
        out.append(s)
    return out

if __name__ == "__main__":
    ark = load(ARK)
    che = load(CHE)
    print("ARK rows=%d uniq=%d" % (len(ark), len(uniq_strings(ark))))
    print("CHE rows=%d uniq=%d" % (len(che), len(uniq_strings(che))))
    aset = set(s for _, s in ark)
    cset = set(s for _, s in che)
    print("shared uniq strings = %d" % len(aset & cset))
    print("ark-only = %d  che-only = %d" % (len(aset - cset), len(cset - aset)))
    # section breakdown
    for name, rows in (("ARK", ark), ("CHE", che)):
        c = collections.Counter(sec for sec, _ in rows)
        print(name, dict(c))
