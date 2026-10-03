#!/usr/bin/env python3
"""Diff menu-bearing strings between the two payloads + reconstruct tree."""
import re, sys, collections, json
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_menu import rodata_strings, SO_ARK, SO_CHE

TITLE_CAPS = re.compile(r"^[A-Z][A-Z0-9 /&+_\-\.]{2,40}$")
CFGKEY     = re.compile(r"^[a-z][a-z0-9]*(_[a-z0-9]+){1,4}$")
HASHID     = re.compile(r"^##[a-z0-9_]+$")

def collect(path):
    strs = rodata_strings(path)
    caps, cfg, hsh = set(), set(), set()
    for o, s in strs:
        if TITLE_CAPS.match(s): caps.add(s)
        if CFGKEY.match(s):     cfg.add(s)
        if HASHID.match(s):     hsh.add(s)
    return strs, caps, cfg, hsh

if __name__ == "__main__":
    a_s, a_caps, a_cfg, a_hsh = collect(SO_ARK)
    c_s, c_caps, c_cfg, c_hsh = collect(SO_CHE)
    print("== TITLE_CAPS ==")
    print("ARK %d, CHE %d, shared %d" % (len(a_caps), len(c_caps), len(a_caps & c_caps)))
    print("\n-- ARK-only caps --")
    for s in sorted(a_caps - c_caps): print("   ", repr(s))
    print("\n-- CHE-only caps --")
    for s in sorted(c_caps - a_caps): print("   ", repr(s))
    print("\n== ##hash ids ==")
    print("ARK %d, CHE %d, shared %d" % (len(a_hsh), len(c_hsh), len(a_hsh & c_hsh)))
    print("  ARK-only:", sorted(a_hsh - c_hsh))
    print("  CHE-only:", sorted(c_hsh - a_hsh))
    print("  shared  :", sorted(a_hsh & c_hsh))
    print("\n== snake_case config keys (filtered) ==")
    interesting = lambda s: re.match(r"^(esp|aim|gfeat|role|object|lovetalk|auth|card|kb|mcp|dump|"
                                     r"mcp_|mod|cfg|config|scale|icon|alpha|self|aim)", s)
    ai = sorted(x for x in a_cfg if interesting(x))
    ci = sorted(x for x in c_cfg if interesting(x))
    print("ARK %d / CHE %d / shared %d" % (len(ai), len(ci), len(set(ai) & set(ci))))
    print("  ARK-only:", sorted(set(ai) - set(ci)))
    print("  CHE-only:", sorted(set(ci) - set(ai)))
