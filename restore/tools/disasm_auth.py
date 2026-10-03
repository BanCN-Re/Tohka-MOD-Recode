"""反汇编卡密验证区域的代码，找出验证通过/失败的判断分支。

已知引用点（虚拟地址，都是 ADRP+ADD 指向字符串的位置）：
  0x247954  ##authwindow
  0x247B0C  authcard
  0x247B88  ##cardkey
  0x247BC8  cardKeyText
  0x247C20  authime
  0x247C48  cardKeyText
  0x247C8C  authkb
  0x247CD4  authverify      <== 验证按钮
  0x2484F4  authclear
  0x33D0CC  AuthConfig.txt
  0x33D87C  AuthConfig.txt
  0x33D8D8  # 卡密
  0x33D900  kill_on_expire
  0x33D950  timeout_sec
  0x33D9C8  notice:
  0x25680C  [Auth]
  0x33B2E4..  ModAuth (20 处)

做法：用 capstone 反汇编 0x247000 - 0x248600 这一整段（auth UI），
把每个分支指令和它附近的字符串引用标出来。
"""
import os
import struct

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
SO = os.path.join(ROOT, "work", "assets", "libArkRe.so")
OUT = os.path.join(ROOT, "docs", "auth-disasm.md")

try:
    from capstone import Cs, CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN
except ImportError:
    print("需要 capstone: pip install capstone")
    raise

d = open(SO, "rb").read()

# 段表
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

def v2f(va):
    for s in secs:
        if s["type"] == 1 and s["addr"] <= va < s["addr"] + s["size"]:
            return s["offset"] + (va - s["addr"])
    return None

def f2v(fo):
    for s in secs:
        if s["type"] == 1 and s["offset"] <= fo < s["offset"] + s["size"]:
            return s["addr"] + (fo - s["offset"])
    return None

# 收集所有已知字符串引用点（用于在反汇编里标注）
KNOWN = {
    0x247954: "##authwindow",
    0x247B0C: "authcard",
    0x247B88: "##cardkey",
    0x247BC8: "cardKeyText",
    0x247C20: "authime",
    0x247C48: "cardKeyText",
    0x247C8C: "authkb",
    0x247CD4: "authverify",
    0x2484F4: "authclear",
    0x33D0CC: "AuthConfig.txt",
    0x33D87C: "AuthConfig.txt",
    0x33D8D8: "# 卡密",
    0x33D900: "kill_on_expire",
    0x33D950: "timeout_sec",
    0x33D9C8: "notice:",
    0x25680C: "[Auth]",
}

md = Cs(CS_ARCH_ARM64, CS_MODE_LITTLE_ENDIAN)

def disasm_range(start_va, end_va, title, max_lines=4000):
    out = []
    out.append("### %s" % title)
    out.append("")
    out.append("范围：`0x%X` - `0x%X`" % (start_va, end_va))
    out.append("")
    out.append("```asm")
    fo = v2f(start_va)
    if fo is None:
        out.append("(范围不在任何段内)")
        out.append("```")
        out.append("")
        return out
    blob = d[fo:fo + (end_va - start_va)]
    n = 0
    for ins in md.disasm(blob, start_va):
        marker = ""
        if ins.address in KNOWN:
            marker = "   ; ===> 引用 %s" % KNOWN[ins.address]
        # 标出分支指令
        elif ins.mnemonic in ("b", "bl", "cbz", "cbnz", "tbz", "tbnz", "ret") or \
             ins.mnemonic.startswith("b."):
            marker = "   ; <分支>"
        out.append("0x%08X  %-8s %s%s" % (ins.address, ins.mnemonic, ins.op_str, marker))
        n += 1
        if n >= max_lines:
            out.append("... (截断，共超过 %d 条)" % max_lines)
            break
    out.append("```")
    out.append("")
    return out

lines = []
def p(s=""):
    lines.append(str(s))

p("# 卡密验证区域反汇编")
p()
p("源文件：`libArkRe.so`（AArch64）")
p()
p("## 已知的字符串引用点")
p()
p("| 虚拟地址 | 字符串 |")
p("| --- | --- |")
for va, name in sorted(KNOWN.items()):
    p("| `0x%X` | `%s` |" % (va, name))
p()

# 主区域：卡密 UI
lines += disasm_range(0x247900, 0x248600, "卡密 UI 区域（0x247900-0x248600）")

# 配置解析区域
lines += disasm_range(0x33D000, 0x33DA80, "AuthConfig 解析区域（0x33D000-0x33DA80）")

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("lines:", len(lines))
