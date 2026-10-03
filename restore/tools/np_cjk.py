#!/usr/bin/env python3
"""Extract CJK / non-ASCII strings the ASCII-only dumper missed."""
import re, sys, collections, json
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_menu import sections, SO_ARK, SO_CHE

CJK = re.compile(
    rb"(?:[\xe4-\xe9][\x80-\xbf]{2}"          # CJK U+4E00-U+9FFF
    rb"|[\xef\xbc\xbd][\x80-\xbf]{2}"          # CJK punctuation / fullwidth
    rb"|[\xe3\x80][\x80-\xbf]{2}"              # CJK symbols & punctuation
    rb"|[\xe2\x80][\x80-\xbf][\xe2\x80][\x80-\xbf]"  # runs
    rb")+")
RUN = re.compile(
    rb"(?:[\x20-\x7e]|[\xe2-\xef][\x80-\xbf]{2}|[\xe3][\x80-\xbf]{2}|[\xf0-\xf4][\x80-\xbf]{3}){2,}"
)

def nonascii_runs(path, min_len=2):
    """Return printable runs (ASCII+CJK) that contain at least one multibyte char."""
    d = open(path, "rb").read()
    secs = sections(d)
    out = []
    for s in secs:
        raw = d[s["offset"]:s["offset"] + s["size"]]
        for m in RUN.finditer(raw):
            b = m.group()
            if all(0x20 <= c < 0x7f for c in b):
                continue  # pure ASCII, already covered
            try:
                t = b.decode("utf-8")
            except UnicodeDecodeError:
                continue
            if len(t.strip()) < min_len:
                continue
            out.append((s["sname"], s["offset"] + m.start(), t))
    return out

if __name__ == "__main__":
    res = {}
    for tag, path in (("ARK", SO_ARK), ("CHE", SO_CHE)):
        got = nonascii_runs(path)
        res[tag] = got
        print("==== %s : %d non-ASCII runs ====" % (tag, len(got)))
        bysec = collections.Counter(x[0] for x in got)
        print("  sections:", dict(bysec))
        seen = set()
        for sec, off, t in got:
            if t in seen:
                continue
            seen.add(t)
            print("   0x%06X [%s] %r" % (off, sec, t))
        print()
    json.dump({k: [(a, b, c) for a, b, c in v] for k, v in res.items()},
              open(r"D:\Work\MuMuP\ArkReCodeHack\restore\tools\cjk.json", "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)
