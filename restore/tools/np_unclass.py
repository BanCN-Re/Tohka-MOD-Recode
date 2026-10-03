#!/usr/bin/env python3
"""Sample the unclassified strings to see what they are."""
import re, collections, random, sys
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_modules import load, uniq, MODULES, ARK, CHE

def stats(strings, label):
    un = [(sec, s) for sec, s in strings if not any(p(s) for _, p, _ in MODULES)]
    print("== %s unclassified: %d ==" % (label, len(un)))
    bysec = collections.Counter(sec for sec, _ in un)
    print("  by section:", dict(bysec))
    lens = [len(s) for _, s in un]
    print("  len: min=%d max=%d mean=%.1f" % (min(lens), max(lens), sum(lens)/len(lens)))
    # printable-ratio histogram
    def ratio(s):
        if not s: return 1.0
        return sum(1 for c in s if 32 <= ord(c) < 127)/len(s)
    buckets = collections.Counter()
    for _, s in un:
        r = ratio(s)
        buckets["all-print(1.0)" if r == 1.0 else
                "mostly(>0.9)" if r > 0.9 else
                "mixed(>0.5)" if r > 0.5 else
                "binary(<0.5)"] += 1
    print("  printability:", dict(buckets))
    # sample .data
    d = [s for sec, s in un if sec == ".data"]
    random.seed(7)
    print("  --- .data samples (repr, first 120 chars) ---")
    for s in random.sample(d, min(12, len(d))):
        print("   ", repr(s[:120]))
    r_ = [s for sec, s in un if sec == ".rodata"]
    print("  --- .rodata samples ---")
    for s in random.sample(r_, min(20, len(r_))):
        print("   ", repr(s[:120]))
    print()

if __name__ == "__main__":
    stats(uniq(load(ARK)), "ARK")
    stats(uniq(load(CHE)), "CHE")
