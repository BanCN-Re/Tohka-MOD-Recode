#!/usr/bin/env python3
"""Final precise per-module counts for the functional matrix."""
import re, sys, collections, json
sys.path.insert(0, r"D:\Work\MuMuP\ArkReCodeHack\restore\tools")
from np_menu import rodata_strings, sections, SO_ARK, SO_CHE

def tables(path):
    strs = rodata_strings(path)
    S = set(s for o, s in strs)
    d = open(path, "rb").read()
    ro = next(x for x in sections(d) if x["sname"] == ".rodata")
    dna = next(x for x in sections(d) if x["sname"] == ".dynstr")
    dat = next(x for x in sections(d) if x["sname"] == ".data")
    return S, d, ro, dna, dat

def cnt_sec(path, name):
    d = open(path, "rb").read()
    s = next(x for x in sections(d) if x["sname"] == name)
    raw = d[s["offset"]:s["offset"] + s["size"]]
    return len(set(m.group() for m in re.finditer(rb"[\x20-\x7e]{3,}", raw))), s["size"]

# feature modules: exact prefix/substring based, mutually disjoint by priority
FEATURES = [
    ("ESP / 透视",        r"^esp"),
    ("AIMBOT / 自瞄",     r"^(aim|selfaim|aimbot)"),
    ("GAME FEATURES",     r"^gfeat_"),
    ("ROLE / 角色",       r"^(role_|roleFilter|roleOccupant)"),
    ("LOVE TALK / 剧情",  r"^lovetalk_"),
    ("OBJECT BROWSER",    r"^(object_|objectFilter|objectNameFilter|objects$)"),
    ("CARD KEY 授权 UI",  r"^(authkb|authcard|authclear|authime|authverify|authcopy|cardKeyText|authorization$|##authwindow|##cardkey)"),
    ("卡密键盘",          r"^kb(back|case|clear|digit|done)$"),
    ("FLOATING ICON",     r"^icon_"),
    ("Il2CppMCP 服务",    r"(^|/)(McpConfig|mcp_dump|mcp_stdio)"),
    ("ROLEDATA (CHE)",    r"^(rd_|roledata_enable)"),
]
# library modules: count by well-known symbol/string families
LIBS = [
    ("OpenSSL/BoringSSL", r"^(ASN1|AES|BIO|BN|CMS|CONF|CRYPTO|DES|DH|DSA|EC|ECDSA|ENGINE|ERR|EVP|HMAC|MD|OBJ|OCSP|OPENSSL|PEM|PKCS|RAND|RC|RIPEMD|RSA|SHA|SRP|SSL|TLS|TS|UI|X509|Camellia|ChaCha|Poly1305|CTLOG|CMAC|ASYNC|DIRECTORY|AUTHORITY|BASIC_CONSTRAINTS|CERTIFICATE|CRL)"),
    ("libc++ / STL",      r"^(_Z|NSt|St[0-9]|std::)"),
]

if __name__ == "__main__":
    out = {}
    for tag, path in (("ARK", SO_ARK), ("CHE", SO_CHE)):
        S, d, ro, dna, dat = tables(path)
        row = {}
        for name, pat in FEATURES:
            row[name] = sorted(x for x in S if re.match(pat, x))
        # il2cpp
        row["il2cpp API 名称"] = sorted(set(m for s in S for m in re.findall(r"il2cpp_[A-Za-z0-9_]+", s)))
        out[tag] = {"features": row, "total_uniq_rodata": len(S)}
        n_ro, sz_ro = cnt_sec(path, ".rodata")
        n_dy, sz_dy = cnt_sec(path, ".dynstr")
        n_da, sz_da = cnt_sec(path, ".data")
        out[tag]["sec"] = {".rodata": (n_ro, sz_ro), ".dynstr": (n_dy, sz_dy), ".data": (n_da, sz_da)}
    # print matrix
    print("%-24s %6s %6s" % ("module", "ARK", "CHE"))
    print("-" * 40)
    for name, _ in FEATURES:
        print("%-24s %6d %6d" % (name, len(out["ARK"]["features"][name]), len(out["CHE"]["features"][name])))
    print("%-24s %6d %6d" % ("il2cpp API 名称", len(out["ARK"]["features"]["il2cpp API 名称"]),
                             len(out["CHE"]["features"]["il2cpp API 名称"])))
    print()
    for tag in ("ARK", "CHE"):
        print(tag, "sections:", {k: v for k, v in out[tag]["sec"].items()})
    json.dump({t: {"features": {k: v for k, v in out[t]["features"].items()}} for t in out},
              open(r"D:\Work\MuMuP\ArkReCodeHack\restore\tools\matrix.json", "w", encoding="utf-8"),
              ensure_ascii=False, indent=1)
