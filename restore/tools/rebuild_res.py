#!/usr/bin/env python3
"""重建 host-apk 的资源目录：只保留自有资源，其余交给 AndroidX 依赖。

依据：aapt2 dump resources 显示自有资源只有 4 项 ——
  0x7f070093  drawable/ic_launcher     ← 各密度图标
  0x7f0700e5  drawable/srzx            ← 面板背景（代码里 getIdentifier("srzx") 查找）
  (未列 ID)   drawable/sx              ← 主视觉背景
  0x7f0e001c  string/app_name          = "Riruriru Mod"
  0x7f0f000b  style/AppTheme           parent=Theme.AppCompat.Light.DarkActionBar

其余 800+ 资源（anim/color/layout/... 里的 abc_* / mtrl_* / design_* 等）
都是 AndroidX / Material 的，应该由 Gradle 依赖提供 —— 手动还原进源码会有两个问题：
  1. 九宫格 .9.png 与 vector xml 经 jadx/zip 提取后不再是 aapt2 能编译的格式
  2. 与依赖里的同名资源冲突

所以这里只搭出自有资源，其余删除。
"""
import os
import shutil

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
RES = os.path.join(HOST, "src", "main", "res")

# 自有资源白名单（相对 res/ 的路径，用 / 分隔）
KEEP = [
    # 图片（代码里 getIdentifier 按名字查找，名字不能改）
    "drawable/sx.jpg",                        # 主视觉背景
    "drawable/srzx.jpg",                      # 面板背景（FloatContentView:280）
    "drawable/ic_launcher.png",               # 悬浮球图标（FloatControlView:43）
    "drawable-hdpi-v4/ic_launcher.png",
    "drawable-mdpi-v4/ic_launcher.png",
    "drawable-xhdpi-v4/ic_launcher.png",
    "drawable-xxhdpi-v4/ic_launcher.png",
    "drawable-xxxhdpi-v4/ic_launcher.png",
    # 按钮背景（activity_main.xml 引用）
    "drawable/button_style.xml",
    "drawable/button_style_gold.xml",
    # 布局（MainActivity:17 用 R.layout.activity_main）
    "layout/activity_main.xml",
    "layout/custom_dialog.xml",
    "layout/layout_webview.xml",
]

# 自有 ID（activity_main.xml 引用；aapt2 dump 确认）
IDS = {
    "btn_qq_group": "btn_qq_group",
    "btn_bilibili": "btn_bilibili",
    "btn_buy":      "btn_buy",
}


def main():
    # 1) 备份位置（放在 host-apk/ 下，不放 res 旁边免得被清掉）
    bak = os.path.join(HOST, "_res-full-backup")
    if not os.path.isdir(bak):
        if os.path.isdir(RES):
            shutil.copytree(RES, bak)
            print("已备份原 res -> _res-full-backup")
        else:
            # 退路：从 res-final 与 src/main/res-full-backup 里取
            alt = os.path.join(HOST, "src", "main", "res-full-backup")
            if os.path.isdir(alt):
                shutil.copytree(alt, bak)
                print("已从 src/main/res-full-backup 恢复备份 -> _res-full-backup")
            else:
                print("找不到资源备份，无法重建")
                return 1
    else:
        print("使用已有备份 _res-full-backup")

    # 2) 收集要保留的文件
    kept = []
    # 2) 收集要保留的文件（从备份里看，因为 res 可能已被删）
    kept = []
    for rel in KEEP:
        p = os.path.join(bak, rel.replace("/", os.sep))
        if os.path.isfile(p):
            kept.append(rel)
        else:
            print("  [警告] 白名单里有但备份里不存在:", rel)

    # 3) 清空并重建 res
    if os.path.isdir(RES):
        shutil.rmtree(RES)
    os.makedirs(RES, exist_ok=True)

    # 4) 放回白名单
    for rel in kept:
        src = os.path.join(bak, rel.replace("/", os.sep))
        dst = os.path.join(RES, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        shutil.copy2(src, dst)
        print("  保留 %-42s %8d B" % (rel, os.path.getsize(dst)))

    # 5) 写自有 values
    vdir = os.path.join(RES, "values")
    os.makedirs(vdir, exist_ok=True)

    with open(os.path.join(vdir, "strings.xml"), "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n'
                '<resources>\n'
                '    <string name="app_name">Riruriru Mod</string>\n'
                '</resources>\n')
    print("  写入 values/strings.xml  (app_name = Riruriru Mod)")

    with open(os.path.join(vdir, "styles.xml"), "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n'
                '<resources>\n'
                '    <!-- 原始：parent = Theme.AppCompat.Light.DarkActionBar，size=0（无自定义项） -->\n'
                '    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar"/>\n'
                '</resources>\n')
    print("  写入 values/styles.xml  (AppTheme -> Theme.AppCompat.Light.DarkActionBar)")

    with open(os.path.join(vdir, "ids.xml"), "w", encoding="utf-8") as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n')
        for k in IDS:
            f.write('    <item name="%s" type="id"/>\n' % k)
        f.write('</resources>\n')
    print("  写入 values/ids.xml     (%s)" % ", ".join(IDS))

    print()
    print("自有资源就位，其余交给 AndroidX 依赖。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
