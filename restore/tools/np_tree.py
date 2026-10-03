#!/usr/bin/env python3
"""Reconstruct the ImGui menu tree from .rodata layout + label/key heuristics."""
import re, sys, collections
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_menu import rodata_strings, SO_ARK, SO_CHE

# UI label shapes seen in ImGui payloads
PATS = [
    ("section",  re.compile(r"^[A-Z][A-Z0-9 /&+\-\.]{2,40}$")),
    ("label",    re.compile(r"^[A-Z][A-Za-z0-9]*( [A-Za-z0-9][A-Za-z0-9\-\.%/\(\)]*){0,3}$")),
    ("cfgkey",   re.compile(r"^[a-z][a-z0-9]*(_[a-z0-9]+){1,4}$")),
    ("hashid",   re.compile(r"^##[a-z0-9_]+$")),
    ("fmt",      re.compile(r"%[-0-9\.]*[dsfuxXgp]")),
]

# subsystem prefixes -> menu grouping evidence
PREFIX_GROUP = [
    ("esp",       "ESP / WALLHACK"),
    ("aim",       "AIMBOT"),
    ("gfeat",     "GAME FEATURES"),
    ("role",      "ROLE / 角色"),
    ("object_",   "OBJECT BROWSER"),
    ("lovetalk",  "LOVE TALK / STORY"),
    ("auth",      "CARD KEY"),
    ("kb",        "CARD KEY (键盘)"),
    ("cardKey",   "CARD KEY"),
    ("icon",      "FLOATING ICON"),
    ("il2cpp",    "Il2Cpp API / MCP"),
    ("mcp",       "Il2CppMCP"),
    ("rd_",       "ROLEDATA (CHE)"),
]

def classify_label(s):
    for name, rx in PATS:
        if rx.match(s):
            return name
    return None

def group_of(s):
    for pre, g in PREFIX_GROUP:
        if s.startswith(pre):
            return g
    return None

if __name__ == "__main__":
    which = sys.argv[1] if len(sys.argv) > 1 else "ark"
    path = SO_ARK if which == "ark" else SO_CHE
    strs = rodata_strings(path)
    # restrict to the menu hot band: between Tohka MOD/title anchor and Il2Cpp API tail
    band = [(o, s) for o, s in strs if 0x61000 <= o <= 0x74000]
    groups = collections.defaultdict(list)
    for o, s in band:
        g = group_of(s)
        if not g:
            continue
        k = classify_label(s)
        if k in ("cfgkey", "hashid", "label", "section"):
            groups[g].append((o, k, s))
    for g in sorted(groups):
        items = groups[g]
        print("### %s  (%d)" % (g, len(items)))
        for o, k, s in sorted(items):
            print("   0x%06X [%-7s] %r" % (o, k, s))
        print()
