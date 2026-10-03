#!/usr/bin/env python3
"""Extract menu structure from payload .rodata using offsets + clustering."""
import re, struct, sys, collections, json

SO_ARK = r"D:\Work\MuMuP\ArkReCodeHack\work\assets\libArkRe.so"
SO_CHE = r"D:\Work\MuMuP\ArkReCodeHack\work\assets\libCherryTale.so"

def sections(d):
    shoff, = struct.unpack_from("<Q", d, 0x28)
    shentsize, shnum, shstrndx = struct.unpack_from("<HHH", d, 0x3A)
    secs = []
    for i in range(shnum):
        f = struct.unpack_from("<IIQQQQIIQQ", d, shoff + i * shentsize)
        secs.append(dict(name=f[0], type=f[1], addr=f[3], offset=f[4], size=f[5]))
    st = secs[shstrndx]
    def nm(n):
        s = d[st["offset"] + n:]
        return s[:s.index(b"\0")].decode("latin1")
    for s in secs:
        s["sname"] = nm(s["name"])
    return secs

def rodata_strings(path):
    d = open(path, "rb").read()
    ro = next(s for s in sections(d) if s["sname"] == ".rodata")
    rod = d[ro["offset"]:ro["offset"] + ro["size"]]
    return [(ro["offset"] + m.start(), m.group().decode("latin1"))
            for m in re.finditer(rb"[\x20-\x7e]{3,}", rod)]

# ---- menu label pattern sets ----
TITLE_CAPS = re.compile(r"^[A-Z][A-Z0-9 /&+_\-\.]{2,40}$")
CFGKEY     = re.compile(r"^[a-z][a-z0-9]*(_[a-z0-9]+){1,4}$")
HASHID     = re.compile(r"^##[a-z0-9_]+$")
WINDOWNAME = re.compile(r"^[A-Z][A-Za-z0-9 ]{2,32}$")

# known section titles to anchor on (from prior analysis, verified in strings)
ANCHORS = ["Tohka MOD", "AIMBOT", "ESP / WALLHACK", "GAME FEATURES",
           "OBJECT BROWSER", "AI ASSISTANT", "CARD KEY", "LOVE TALK / STORY",
           "SETTINGS", "ROLE", "DUMP", "Il2Cpp API", "Custom Camera"]

if __name__ == "__main__":
    which = sys.argv[1] if len(sys.argv) > 1 else "ark"
    path = SO_ARK if which == "ark" else SO_CHE
    strs = rodata_strings(path)
    print("### %s  .rodata strings=%d" % (path.split("\\")[-1], len(strs)))
    # locate anchors
    for a in ANCHORS:
        hits = [o for o, s in strs if s == a]
        print("  anchor %-22s -> %s" % (a, [hex(h) for h in hits] or "(absent)"))
