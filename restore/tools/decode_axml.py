#!/usr/bin/env python3
"""把二进制 AXML 布局解码成文本 XML。

jadx/apktool 提取出的 res/layout/*.xml 是编译过的二进制 AXML（魔数 0x00080003），
aapt2 不接受这种格式作为输入 —— 必须还原成文本 XML。

同时把 @7F0E001C 这类资源 ID 引用还原成 @string/app_name 形式，
否则 aapt2 会因为找不到 ID 而报错。
"""
import os
import re
import sys

try:
    from loguru import logger
    logger.remove()
except Exception:
    pass

from androguard.core.axml import AXMLPrinter

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
RES = os.path.join(HOST, "src", "main", "res")

# 已知的资源 ID -> 符号名（从 aapt2 dump resources 拿到的自有资源）
ID_MAP = {
    0x7F0E001C: "@string/app_name",
    0x7F070093: "@drawable/ic_launcher",
    0x7F0700E5: "@drawable/srzx",
    0x7F0700E6: "@drawable/sx",
    0x7F070082: "@drawable/button_style",
    0x7F070083: "@drawable/button_style_gold",
    0x7F080058: "@id/btn_bilibili",
    0x7F080059: "@id/btn_buy",
    0x7F08005A: "@id/btn_qq_group",
    0x7F0F000B: "@style/AppTheme",
    # 常用的 Android 系统资源，android: 命名空间下的不用换
    0x01030012: "@android:style/AlertDialog",
    0x010100F2: "@android:layout/alert_dialog_material",
}


def fix_refs(xml: str) -> str:
    """把 @7Fxxxxxx 换成符号名；未知的保留但加注释"""
    def repl(m):
        v = int(m.group(1), 16)
        if v in ID_MAP:
            return ID_MAP[v]
        return m.group(0)
    return re.sub(r"@(7[fF][0-9a-fA-F]{6})", repl, xml)


def main():
    n = 0
    for sub in ("layout", "xml", "drawable", "color", "anim", "animator",
                "interpolator", "values"):
        d = os.path.join(RES, sub)
        if not os.path.isdir(d):
            continue
        for f in sorted(os.listdir(d)):
            if not f.endswith(".xml"):
                continue
            p = os.path.join(d, f)
            raw = open(p, "rb").read()
            if len(raw) < 4 or raw[:2] != b"\x03\x00":
                continue                      # 已经是文本
            try:
                pr = AXMLPrinter(raw)
                xml = pr.get_xml()
                if isinstance(xml, bytes):
                    xml = xml.decode("utf-8", errors="replace")
                xml = fix_refs(xml)
                open(p, "w", encoding="utf-8").write(xml)
                n += 1
                print("  解码 %-40s %6d -> %6d B" % (sub + "/" + f, len(raw), len(xml)))
            except Exception as e:
                print("  失败 %-40s %r" % (sub + "/" + f, e))
    print()
    print("共解码 %d 个二进制 XML" % n)


if __name__ == "__main__":
    main()
