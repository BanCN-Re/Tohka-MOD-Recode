# 把恢复工程整理成标准 Gradle 布局，并生成可编译的工程骨架。
#
# 做三件事：
#   1. 把还原的源码/资源搬到 src/main/{java,res,assets} 标准位置
#   2. 生成 AndroidManifest.xml（从原包解码）
#   3. 生成 settings.gradle / gradle.properties / gradle wrapper 所需文件
#
# 用法: python tools/make_buildable.py
import os
import re
import shutil
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
R = os.path.dirname(HERE)                    # restore/
ROOT = os.path.dirname(R)                    # ArkReCodeHack/
HOST = os.path.join(R, "host-apk")
SRCMAIN = os.path.join(HOST, "src", "main")


def copytree(src, dst):
    if not os.path.isdir(src):
        return 0
    n = 0
    for dp, dn, fn in os.walk(src):
        rel = os.path.relpath(dp, src)
        for f in fn:
            d = os.path.join(dst, rel, f) if rel != "." else os.path.join(dst, f)
            os.makedirs(os.path.dirname(d), exist_ok=True)
            shutil.copy2(os.path.join(dp, f), d)
            n += 1
    return n


def main():
    os.makedirs(SRCMAIN, exist_ok=True)

    # 1) Java 源码
    java_dir = os.path.join(SRCMAIN, "java")
    n1 = copytree(os.path.join(HOST, "src", "com"),
                  os.path.join(java_dir, "com"))
    n2 = copytree(os.path.join(HOST, "src", "irene-src", "irene"),
                  os.path.join(java_dir, "irene"))
    print("java: com.Riruriru %d + irene %d" % (n1, n2))

    # 2) 资源
    res_dir = os.path.join(SRCMAIN, "res")
    n3 = copytree(os.path.join(HOST, "res"), res_dir)
    print("res: %d 个文件" % n3)

    # 3) assets
    as_dir = os.path.join(SRCMAIN, "assets")
    n4 = copytree(os.path.join(R, "host-apk", "res-final", "assets"), as_dir)
    print("assets: %d 个文件" % n4)

    # 4) settings.gradle / gradle.properties
    with open(os.path.join(HOST, "settings.gradle"), "w", encoding="utf-8") as f:
        f.write("""pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "RiruriruMod"
include ':app'
""")

    with open(os.path.join(HOST, "gradle.properties"), "w", encoding="utf-8") as f:
        f.write("""android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx2048m
""")

    # 5) AndroidManifest —— 从原包解码
    print()
    print("解码 AndroidManifest.xml ...")
    try:
        from loguru import logger
        logger.remove()
    except Exception:
        pass
    try:
        from androguard.core.axml import AXMLPrinter
        z = zipfile.ZipFile(os.path.join(ROOT, "Riruriru Mod_1.1.2.apk.1"))
        raw = z.read("AndroidManifest.xml")
        pr = AXMLPrinter(raw)
        xml = pr.get_xml()
        if isinstance(xml, bytes):
            xml = xml.decode("utf-8", errors="replace")
        dst = os.path.join(SRCMAIN, "AndroidManifest.xml")
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(dst, "w", encoding="utf-8") as f:
            f.write(xml)
        print("  写出 %d 字节 -> src/main/AndroidManifest.xml" % len(xml))
        # 也留一份原始二进制供 apktool 用
        with open(os.path.join(HOST, "AndroidManifest.bin"), "wb") as f:
            f.write(raw)
        print("  原始二进制 -> AndroidManifest.bin")
    except Exception as e:
        print("  解码失败: %r" % e)

    # 6) 目录树概览
    print()
    print("可编译工程位置:", SRCMAIN)
    tot = 0
    for dp, dn, fn in os.walk(SRCMAIN):
        tot += len(fn)
    print("总文件数: %d" % tot)


if __name__ == "__main__":
    main()
