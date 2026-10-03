"""反汇编 injected.dex 里的 ImGui 类，搞清 setupImGuiView 的调用方式。"""
import os
import re
import sys

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass

from androguard.core.dex import DEX

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
DEXF = os.path.join(ROOT, "build", "dex", "libArkRe.injected.dex")
OUT = os.path.join(ROOT, "docs", "injected-dex.md")

lines = []
def p(s=""):
    lines.append(str(s))

d = DEX(open(DEXF, "rb").read())

p("# injected.dex 结构（从 payload 里提取）")
p()
p("两个 payload 内嵌的 DEX 完全相同（都是 16,136 字节、282 个字符串、9 个类）。")
p("它是 ImGui 覆盖层的 Java 侧：建 GLSurfaceView、挂到 Activity 上、把触摸事件转发给 native。")
p()

p("## 1. 类清单")
p()
for c in sorted(d.get_classes(), key=lambda x: x.get_name()):
    ms = sorted(set(m.get_name() for m in c.get_methods()))
    p("- `%s`" % c.get_name())
    p("  - 方法：%s" % ", ".join("`%s`" % m for m in ms))
p()

p("## 2. 关键方法的签名与反汇编")
p()
WANT = {
    "Lcom/example/imgui/ImGui;": ["setupImGuiView", "setupImGuiViewOnMainThread",
                                  "setupImGuiViewInternal", "addGamePointer",
                                  "removeGamePointer", "<clinit>", "<init>"],
    "Lcom/example/imgui/GLES3JNIView;": ["<init>", "onSurfaceCreated",
                                         "onSurfaceChanged", "onDrawFrame", "init"],
}
for c in d.get_classes():
    cn = c.get_name()
    if cn not in WANT:
        continue
    p("### %s" % cn)
    p()
    # 类的方法描述符
    for m in c.get_methods():
        mn = m.get_name()
        if mn not in WANT[cn]:
            continue
        p("#### `%s`" % mn)
        try:
            desc = m.get_descriptor()
        except Exception:
            desc = "?"
        p()
        p("描述符：`%s`" % desc)
        p()
        code = m.get_code()
        if code is None:
            p("（无字节码）")
            p()
            continue
        try:
            ins = list(code.get_bc().get_instructions())
        except Exception as e:
            p("反汇编失败：%r" % e)
            p()
            continue
        p("```")
        for i in ins[:80]:
            p("%-12s %s" % (i.get_name(), i.get_output()))
        if len(ins) > 80:
            p("... (共 %d 条指令)" % len(ins))
        p("```")
        p()

p("## 3. 全部字符串（去掉明显是库的）")
p()
vals = []
for s in d.get_strings():
    v = s.get_value() if hasattr(s, "get_value") else s
    if isinstance(v, str) and 2 <= len(v) <= 80:
        vals.append(v)
for v in sorted(set(vals)):
    p("- `%s`" % v)

open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("wrote", OUT)
print("lines:", len(lines))
