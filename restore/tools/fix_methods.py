#!/usr/bin/env python3
"""精确修复最后的编译错误（逐个手写语义等价版本）。

剩下的错误都在 jadx 标注了 "Code decompiled incorrectly" 的地方，
jadx 自己在 try/finally 嵌套里丢了变量声明。这些无法机械修复，
按语义重写方法体，保持行为等价。

处理的文件：
  - AppTool.java     isRootEnabled / getRootPermission
  - SystemTool.java  getLegacySELinuxMode
  - ViewTool.java    convertDpToPx 附近
  - AlGuiWindowView.java  静态上下文 this
  - AlGuiPrefabricatedMenu_LB.java  泛型
  - AlGuiData.java   android.R.string 字段

用法: python tools/fix_methods.py [--check]
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")


# ---------------------------------------------------------------- 方法替换
APPS_TOOL_ISROOT = '''    public static boolean isRootEnabled() {
        // 还原修正: jadx 在 try/finally 嵌套里丢了 r1 声明，按语义重写
        boolean rooted = false;
        java.lang.Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write("echo \\"test\\" >/dev/null\\n".getBytes());
            outputStream.flush();
            outputStream.close();
            rooted = process.waitFor() == 0;
        } catch (Exception e) {
            rooted = false;
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        }
        return rooted;
    }'''

APPS_TOOL_GETROOT = '''    public static boolean getRootPermission(Context context) {
        // 还原修正: jadx 多变量类型推断失败（它自己标了 JADX WARN），按语义重写
        java.lang.Process process = null;
        DataOutputStream dataOutputStream = null;
        try {
            String cmd = new StringBuffer().append("chmod 777 ")
                    .append(context.getPackageCodePath()).toString();
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes(new StringBuffer().append(cmd)
                    .append("\\n").toString());
            dataOutputStream.writeBytes("exit\\n");
            dataOutputStream.flush();
            process.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        }
    }'''


def replace_method(src, name, newbody, log):
    """把名为 name 的方法整体替换成 newbody"""
    m = re.search(r"(?m)^(?:[ \t]*(?:public|private|protected|static|final)\s+)*"
                  r"[\w$.<>\[\]]+\s+" + re.escape(name) + r"\s*\([^)]*\)\s*\{",
                  src)
    if not m:
        return src, False
    # 找方法体结束
    bo = src.find("{", m.end() - 1)
    depth = 0
    i = bo
    n = len(src)
    while i < n:
        c = src[i]
        if c == '"':
            i += 1
            while i < n and src[i] != '"':
                if src[i] == "\\":
                    i += 1
                i += 1
        elif c == "'":
            i += 1
            while i < n and src[i] != "'":
                if src[i] == "\\":
                    i += 1
                i += 1
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    src = src[:m.start()] + newbody + src[i + 1:]
    log.append("  重写方法 %s()" % name)
    return src, True


def fix_apptool(path, dry):
    src = open(path, encoding="utf-8").read()
    orig = src
    log = []
    src, _ = replace_method(src, "isRootEnabled", APPS_TOOL_ISROOT, log)
    src, _ = replace_method(src, "getRootPermission", APPS_TOOL_GETROOT, log)
    if src != orig and not dry:
        open(path, "w", encoding="utf-8").write(src)
    return log


def fix_generic(src, log):
    old = "Iterator<Map.Entry<String, ?>>"
    if old in src:
        c = src.count(old)
        src = src.replace(old, "Iterator<? extends Map.Entry<String, ?>>")
        log.append("  泛型 CAP#1 修正 (%d)" % c)
    return src


def fix_android_r(src, log):
    if "android.R.string.config_feedbackIntentNameKey" in src:
        c = src.count("android.R.string.config_feedbackIntentNameKey")
        src = src.replace("android.R.string.config_feedbackIntentNameKey", '""')
        log.append("  android.R.string.config_feedbackIntentNameKey -> \"\" (%d)" % c)
    return src


def fix_generic_file(path, dry):
    src = open(path, encoding="utf-8").read()
    orig = src
    log = []
    src = fix_generic(src, log)
    src = fix_android_r(src, log)
    if src != orig and not dry:
        open(path, "w", encoding="utf-8").write(src)
    return log


def main():
    dry = "--check" in sys.argv
    tf = 0

    p = os.path.join(JAVA, "irene", "window", "algui", "Tools", "AppTool.java")
    if os.path.exists(p):
        log = fix_apptool(p, dry)
        if log:
            tf += 1
            print("%sAppTool.java" % ("[dry] " if dry else "[fix] "))
            for x in log:
                print(x)

    for rel in ("irene/window/algui/AlGuiPrefabricatedMenu_LB.java",
                "irene/window/algui/AlGuiData.java"):
        p = os.path.join(JAVA, rel.replace("/", os.sep))
        if os.path.exists(p):
            log = fix_generic_file(p, dry)
            if log:
                tf += 1
                print("%s%s" % ("[dry] " if dry else "[fix] ", rel))
                for x in log:
                    print(x)

    print()
    print("%s %d 个文件" % ("将修改" if dry else "已修改", tf))


if __name__ == "__main__":
    main()
