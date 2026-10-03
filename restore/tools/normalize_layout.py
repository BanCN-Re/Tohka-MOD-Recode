#!/usr/bin/env python3
"""把反编译出的布局 XML 规范化成 aapt2 能编译的形式。

问题：androguard 的 AXMLPrinter 把属性值原样输出成数字（编译后的内部表示），
     而 aapt2 要的是源码写法。必须做映射：

  layout_width/height : -1 -> match_parent      -2 -> wrap_content
  orientation         : 1 -> vertical           0 -> horizontal
  gravity             : 0x11 -> center
  textStyle           : 1 -> bold               2 -> italic
  visibility          : 0 -> visible
  stateListAnimator   : @00000000 -> @null
  id                  : @7Fxxxxxx -> @+id/<name>（未知的造个名字）
  padding 等 dimension: "30.000000dip" -> "30dip"
  float               : "2.000000" -> "2"
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
RES = os.path.join(HOST, "src", "main", "res")

ENUMS = {
    "layout_width":      {"-1": "match_parent", "-2": "wrap_content"},
    "layout_height":     {"-1": "match_parent", "-2": "wrap_content"},
    "minWidth":          {"-1": "match_parent", "-2": "wrap_content"},
    "minHeight":         {"-1": "match_parent", "-2": "wrap_content"},
    "orientation":       {"0": "horizontal", "1": "vertical"},
    "visibility":        {"0": "visible", "1": "invisible", "2": "gone"},
    "textStyle":         {"0": "normal", "1": "bold", "2": "italic", "3": "bold|italic",
                          "0x00000000": "normal", "0x00000001": "bold",
                          "0x00000002": "italic", "0x00000003": "bold|italic"},
    "gravity": {
        "0x00000011": "center", "17": "center",
        "0x00000001": "center_horizontal", "1": "center_horizontal",
        "0x00000010": "center_vertical", "16": "center_vertical",
        "0x00000003": "left|center_vertical", "3": "left|center_vertical",
    },
    "scaleType":         {"0": "matrix", "1": "fitXY", "2": "fitStart",
                          "3": "fitCenter", "4": "fitEnd", "5": "center",
                          "6": "centerCrop", "7": "centerInside"},
    "inputType":         {"1": "text"},
    "ellipsize":         {"0": "none", "1": "start", "2": "middle", "3": "end", "4": "marquee"},
}

DIM_RE = re.compile(r'"(-?[0-9.]+)(dip|dp|sp|px|pt|mm|in)"')
FLOAT_ATTR = {"shadowDx", "shadowDy", "shadowRadius", "alpha", "rotation",
              "rotationX", "rotationY", "scaleX", "scaleY", "translationX",
              "translationY", "textSize"}


def fix_dim(m):
    v = float(m.group(1))
    unit = m.group(2)
    s = ("%g" % v)
    return '"%s%s"' % (s, unit)


def fix_float_value(m):
    return '"%g"' % float(m.group(1))


def main():
    n = 0
    made_ids = {}
    for sub in ("layout", "xml", "drawable", "color", "anim", "animator",
                "interpolator"):
        d = os.path.join(RES, sub)
        if not os.path.isdir(d):
            continue
        for f in sorted(os.listdir(d)):
            if not f.endswith(".xml"):
                continue
            p = os.path.join(d, f)
            txt = open(p, encoding="utf-8").read()
            if not txt.lstrip().startswith("<"):
                continue
            orig = txt

            # 1) 枚举值
            for attr, table in ENUMS.items():
                def rep(m, table=table):
                    v = m.group(1)
                    return '%s="%s"' % (attr, table.get(v, v))
                txt = re.sub(r'%s="([^"]*)"' % re.escape(attr), rep, txt)

            # 2) dimension：去掉多余的 .000000
            txt = DIM_RE.sub(fix_dim, txt)

            # 3) 浮点属性
            for a in FLOAT_ATTR:
                txt = re.sub(r'%s="(-?[0-9]+\.[0-9]+)"' % a,
                             lambda m: '%s="%g"' % (a, float(m.group(1))), txt)

            # 4) stateListAnimator @00000000 -> @null
            txt = txt.replace('stateListAnimator="@00000000"',
                              'stateListAnimator="@null"')

            # 5) id 引用：@7Fxxxxxx -> @+id/gen_xxxx
            def rep_id(m):
                hexv = m.group(1).upper()
                name = "gen_%s" % hexv.lower()
                made_ids[hexv] = name
                return "@+id/%s" % name
            txt = re.sub(r'@\+?id/@(7F[0-9A-Fa-f]{6})', rep_id, txt)
            txt = re.sub(r'@(7F08[0-9A-Fa-f]{4})', rep_id, txt)

            # 6) 其它未知 @7Fxxxxxx 引用：换成 @null 会丢语义，这里保留但报告
            leftovers = set(re.findall(r'@(7F[0-9A-Fa-f]{6})', txt))

            if txt != orig:
                open(p, "w", encoding="utf-8").write(txt)
                n += 1
                print("  规范化 %-40s" % (sub + "/" + f))
                if leftovers:
                    print("     仍有未映射引用: %s" % ", ".join(sorted(leftovers)))

    # 补 id 声明
    if made_ids:
        vdir = os.path.join(RES, "values")
        os.makedirs(vdir, exist_ok=True)
        p = os.path.join(vdir, "ids_generated.xml")
        with open(p, "w", encoding="utf-8") as fh:
            fh.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
            for hexv, name in sorted(made_ids.items()):
                fh.write('    <item name="%s" type="id"/>  <!-- was @%s -->\n'
                         % (name, hexv))
            fh.write('</resources>\n')
        print()
        print("补写 %d 个 id 声明 -> values/ids_generated.xml" % len(made_ids))

    print()
    print("规范化 %d 个文件" % n)


if __name__ == "__main__":
    main()
