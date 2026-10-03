#!/usr/bin/env python3
"""完整构建 APK —— 手工流程（aapt2 + javac + d8 + apksigner）。

## 为什么不用 Gradle

AndroidX 有 2000+ 个资源 flat 文件，`aapt2 link` 必须把它们全部作为
命令行参数传入，路径总长超过 Windows 的 32767 上限，且 aapt2 不支持
@argfile。Gradle 的 AGP 插件用内部 API 绕过了这一点。

## 本脚本的做法

**复用原 APK 里已编译好的资源**（`resources.arsc` + `res/`）：
  - 这些资源 ID 与原版一致
  - 用 aapt2 link 生成一份匹配的 `R.java`（只编自有资源）
  - 不用重新编译 AndroidX 的 2000+ 资源

流程：
  1. aapt2 compile    自有 res -> .flat
  2. aapt2 link       自有 flat + manifest -> R.java
  3. 组装 base.apk    原 APK 的 res/arsc + 我们的 manifest + assets
  4. javac            源码 + R.java -> .class
  5. d8               .class + 依赖 jar -> classes.dex
  6. 打包 + zipalign + apksigner

用法:
    python tools/build_apk.py                   # 完整构建
    python tools/build_apk.py --step javac      # 只跑到 javac
"""
import glob
import re
import io
import os
import shutil
import subprocess
import sys
import zipfile

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
HOST = os.path.join(ROOT, "host-apk")
BUILD = os.path.join(HOST, "build-manual")
PROJ = os.path.dirname(ROOT)

SDK = os.environ.get("ANDROID_HOME") or os.path.join(
    os.environ.get("LOCALAPPDATA", ""), "Android", "Sdk")
BT = os.path.join(SDK, "build-tools", "34.0.0")
ANDROID_JAR = os.path.join(SDK, "platforms", "android-35", "android.jar")
JAVAC = r"C:\Program Files\Java\jdk-17\bin\javac.exe"
GRADLE_HOME = os.path.join(os.environ.get("USERPROFILE", ""), ".gradle")

AAPT2 = os.path.join(BT, "aapt2.exe")
D8 = os.path.join(BT, "d8.bat")
ZIPALIGN = os.path.join(BT, "zipalign.exe")
APKSIGNER = os.path.join(BT, "apksigner.bat")

MANIFEST = os.path.join(HOST, "src", "main", "AndroidManifest.xml")
RES = os.path.join(HOST, "src", "main", "res")
JAVA = os.path.join(HOST, "src", "main", "java")
ASSETS = os.path.join(HOST, "src", "main", "assets")

ORDER = ["resources", "link", "base", "javac", "d8", "package", "sign"]
STEP = sys.argv[sys.argv.index("--step") + 1] if "--step" in sys.argv else None


def reached(step):
    return STEP is None or ORDER.index(step) <= ORDER.index(STEP)


def run(cmd, desc, quiet=False):
    print()
    print("### %s" % desc)
    r = subprocess.run(cmd, capture_output=True, text=True,
                       encoding="utf-8", errors="replace")
    out = (r.stdout or "") + (r.stderr or "")
    lines = [l for l in out.splitlines()
             if l.strip() and "Picked up" not in l]
    if r.returncode != 0:
        for l in lines[:60]:
            print("    " + l[:170])
        print("!!! 失败 (exit %d)" % r.returncode)
        return r.returncode
    if not quiet:
        for l in lines[:12]:
            print("    " + l[:170])
    return 0


def dep_jars():
    """收集编译与 dex 需要的依赖 jar。

    两类来源：
      1. transforms 缓存里的 classes.jar（AAR 解出来的完整实现）
      2. modules-2 里的普通 jar（kotlin-stdlib / lifecycle-common 等）

    注意：classes.jar 每个 AAR 一个，文件名都相同，
    所以**必须按完整路径保留**，不能按文件名去重。
    """
    keep = []

    # 1) AAR 解出的 classes.jar —— 按路径保留，全部要
    for p in glob.glob(os.path.join(GRADLE_HOME, "caches", "*", "transforms",
                                    "*", "transformed", "*", "jars", "classes.jar")):
        low = p.lower().replace("\\", "/")
        if any(k in low for k in (
                "appcompat", "core-", "constraintlayout", "material",
                "cardview", "recyclerview", "annotation", "collection",
                "lifecycle", "activity", "fragment", "drawerlayout",
                "coordinatorlayout", "viewpager", "loader",
                "versionedparcelable", "savedstate", "interpolator",
                "emoji2", "profileinstaller", "startup", "tracing",
                "vectordrawable", "customview", "cursoradapter",
                "documentfile", "dynamicanimation", "resourceinspection",
                "transition", "arch")):
            keep.append(p)

    # 2) modules-2 的普通 jar —— 严格按 group 过滤
    want_groups = (
        "/androidx.", "/com.google.android.material/",
        "/org.jetbrains.kotlin/", "/org.jetbrains/",
        # profileinstaller 需要这两个：它依赖 ListenableFuture 接口，
        # 而 concurrent-futures 提供 AbstractResolvableFuture 实现。
        # 缺任何一个都会让 ProfileInstaller 的后台线程抛
        # NoClassDefFoundError，进而整个进程被 SIG:9 杀掉。
        "/com.google.guava/listenablefuture/",
        "/androidx.concurrent/",
    )
    seen = {}
    for p in glob.glob(os.path.join(GRADLE_HOME, "caches", "modules-2",
                                    "files-2.1", "**", "*.jar"), recursive=True):
        low = p.lower().replace("\\", "/")
        if "sources" in low or "javadoc" in low:
            continue
        if not any(g in low for g in want_groups):
            continue
        # 同名库只留最高版本（kotlin-stdlib 1.8.22 与 1.9.0 同时存在
        # 会让 d8 报 "Type xxx is defined multiple times"）
        m = re.match(r"^(.+?)-(\d[\d.]*)\.jar$", os.path.basename(p))
        if m:
            key, ver = m.group(1), m.group(2)
            try:
                vt = tuple(int(x) for x in ver.split("."))
            except ValueError:
                vt = (0,)
        else:
            key, vt = os.path.basename(p), (0,)
        if key in seen and seen[key] >= vt:
            continue
        seen[key] = vt
        keep.append(p)

    return sorted(set(keep))


def find_orig_apk():
    for cand in ("Riruriru Mod_1.1.2.apk.1", "Riruriru Mod_1.1.2.apk",
                 "Riruriru Mod_1.1.2.apk.2"):
        p = os.path.join(PROJ, cand)
        if os.path.exists(p):
            return p
    return None


def main():
    os.makedirs(BUILD, exist_ok=True)
    flat = os.path.join(BUILD, "flat")
    gen = os.path.join(BUILD, "gen")
    cls = os.path.join(BUILD, "classes")
    dex = os.path.join(BUILD, "dex")
    for d in (flat, gen, cls, dex):
        os.makedirs(d, exist_ok=True)

    base_apk = os.path.join(BUILD, "base.apk")
    unsigned = os.path.join(BUILD, "unsigned.apk")
    aligned = os.path.join(BUILD, "aligned.apk")
    final = os.path.join(BUILD, "TohkaMOD-Recode.apk")

    # ---- 1. aapt2 compile ----
    if reached("resources"):
        for f in glob.glob(os.path.join(flat, "*.flat")):
            os.remove(f)
        run([AAPT2, "compile", "--dir", RES, "-o", flat],
            "1/7 aapt2 compile (自有 res -> .flat)")

    # ---- 2. 生成 R.java ----
    # 不能用 aapt2 link 生成：它需要 AndroidX 的全部资源（2000+ flat），
    # 参数会超 Windows 命令行上限。改用 gen_rjava.py 从原 APK 的资源表生成 ——
    # 反正我们复用的就是那份 resources.arsc，ID 天然一致。
    if reached("link"):
        print()
        print("### 2/7 生成 R.java")
        r = subprocess.run([sys.executable,
                            os.path.join(HERE, "gen_rjava.py"),
                            "-o", gen],
                           capture_output=True, text=True,
                           encoding="utf-8", errors="replace")
        for l in (r.stdout or "").splitlines()[-4:]:
            print("    " + l.encode("utf-8", "replace").decode("utf-8", "replace"))
        found = glob.glob(os.path.join(gen, "**", "R.java"), recursive=True)
        print("    R.java: %s" % (found[0] if found else "未生成"))
        if not found:
            return 1

    # ---- 3. 组装 base.apk ----
    if reached("base"):
        orig = find_orig_apk()
        if not orig:
            print()
            print("!!! 找不到原 APK（用于复用已编译资源）: %s" % PROJ)
            return 1
        print()
        print("### 3/7 组装 base.apk (复用原 APK 资源)")
        with zipfile.ZipFile(orig) as zin, \
             zipfile.ZipFile(base_apk, "w", zipfile.ZIP_DEFLATED) as zout:
            n = 0
            for e in zin.infolist():
                if e.is_dir():
                    continue
                nm = e.filename
                if nm == "resources.arsc" or nm.startswith("res/"):
                    zout.writestr(e, zin.read(nm))
                    n += 1
            # manifest 必须是**二进制 AXML**，文本 XML 会让 apksigner 报
            # "malformed binary resource: AndroidManifest"。
            # 用 aapt2 link 编译一份（只带自有资源即可生成合法 AXML）。
            axml = None
            own_flats = sorted(glob.glob(os.path.join(flat, "**", "*.flat"),
                                         recursive=True))
            cmd = [AAPT2, "link",
                   "-o", os.path.join(BUILD, "manifest-only.apk"),
                   "-I", ANDROID_JAR,
                   "--manifest", MANIFEST,
                   "--min-sdk-version", "23",
                   "--target-sdk-version", "27",
                   "--version-code", "1",
                   "--version-name", "1.1.2",
                   "--no-version-vectors",
                   "--auto-add-overlay"]
            cmd += own_flats
            r2 = subprocess.run(cmd, capture_output=True, text=True,
                                encoding="utf-8", errors="replace")
            made = os.path.join(BUILD, "manifest-only.apk")
            if os.path.exists(made):
                with zipfile.ZipFile(made) as zt:
                    axml = zt.read("AndroidManifest.xml")
            if axml:
                zout.writestr("AndroidManifest.xml", axml)
                print("    资源条目 %d 个 + 二进制 AndroidManifest.xml (%d B)"
                      % (n, len(axml)))
            else:
                with zipfile.ZipFile(orig) as zo:
                    axml = zo.read("AndroidManifest.xml")
                zout.writestr("AndroidManifest.xml", axml)
                print("    [警告] aapt2 编译 manifest 失败，复用原包的 (%d B)"
                      % len(axml))
                for l in ((r2.stdout or "") + (r2.stderr or "")).splitlines()[:4]:
                    print("      " + l[:150])
            na = 0
            if os.path.isdir(ASSETS):
                for dp, dn, fn in os.walk(ASSETS):
                    for f in fn:
                        p = os.path.join(dp, f)
                        rel = os.path.relpath(p, ASSETS).replace("\\", "/")
                        zout.write(p, "assets/" + rel)
                        na += 1
            print("    assets: %d 个" % na)

    # ---- 4. javac ----
    if reached("javac"):
        srcs = []
        for dp, dn, fn in os.walk(JAVA):
            for f in fn:
                if f.endswith(".java"):
                    srcs.append(os.path.join(dp, f))
        rjavas = glob.glob(os.path.join(gen, "**", "R.java"), recursive=True)
        if rjavas:
            srcs.append(rjavas[0])
        jars = dep_jars()
        print()
        print("### 4/7 javac (%d 个源文件, %d 个依赖 jar)"
              % (len(srcs), len(jars)))
        args = os.path.join(BUILD, "javac.args")
        with open(args, "w", encoding="utf-8", newline="\n") as f:
            f.write("-encoding UTF-8\n-nowarn\n-proc:none\n-Xmaxerrs 500\n")
            f.write('-d "%s"\n' % cls.replace("\\", "/"))
            cp = os.pathsep.join([ANDROID_JAR] + jars)
            f.write('-cp "%s"\n' % cp.replace("\\", "/"))
            for s in srcs:
                f.write('"%s"\n' % s.replace("\\", "/"))
        rc = run([JAVAC, "@" + args], "    编译")
        if rc != 0:
            return rc

    # ---- 5. d8 ----
    if reached("d8"):
        classfiles = []
        for dp, dn, fn in os.walk(cls):
            for f in fn:
                if f.endswith(".class"):
                    classfiles.append(os.path.join(dp, f))
        print()
        print("### 5/7 d8 (%d 个 class)" % len(classfiles))
        if not classfiles:
            print("!!! 无 class")
            return 1
        for old in glob.glob(os.path.join(dex, "*.dex")):
            os.remove(old)
        # d8 的 @argfile 不接受引号（会当成路径的一部分），
        # 且参数多时命令行会超长 —— 所以用「先把 class 打成 jar，
        # 再把 jar 一起给 d8」的方式减少参数个数。
        import tempfile
        # 1) 把我们的 class 打成 jar
        myjar = os.path.join(BUILD, "classes.jar")
        with zipfile.ZipFile(myjar, "w", zipfile.ZIP_DEFLATED) as z:
            for c in classfiles:
                arc = os.path.relpath(c, cls).replace("\\", "/")
                z.write(c, arc)
        print("    classes.jar: %.2f MB"
              % (os.path.getsize(myjar) / 1024 / 1024))

        # 2) 依赖 jar 也合并成一个（避免参数过多）
        depjar = os.path.join(BUILD, "deps.jar")
        jars = dep_jars()
        # 合并依赖：按**类路径**去重 —— 不同版本的 jar 里同名类会让
        # d8 报 "Type xxx is defined multiple times"，这里只保留先到的
        seen_cls = set()
        dup = 0
        with zipfile.ZipFile(depjar, "w", zipfile.ZIP_STORED) as z:
            for j in jars:
                try:
                    with zipfile.ZipFile(j) as zj:
                        for e in zj.infolist():
                            if e.is_dir():
                                continue
                            nm = e.filename
                            if nm.endswith(".class"):
                                if nm in seen_cls:
                                    dup += 1
                                    continue
                                seen_cls.add(nm)
                            elif nm.startswith("META-INF/"):
                                continue        # 元数据不要，避免冲突
                            z.writestr(e, zj.read(nm))
                except Exception:
                    pass
        print("    去重跳过 %d 个重复类" % dup)
        print("    deps.jar: %.2f MB (%d 个依赖)"
              % (os.path.getsize(depjar) / 1024 / 1024, len(jars)))

        # 3) 调 d8（参数很短，不会超长）
        run([D8, "--min-api", "23", "--output", dex,
             "--lib", ANDROID_JAR,
             depjar, myjar],
            "    dex 生成")

    # ---- 6. 打包 ----
    if reached("package"):
        print()
        print("### 6/7 打包")
        dexes = sorted(glob.glob(os.path.join(dex, "classes*.dex")))
        if not dexes:
            print("!!! 无 dex")
            return 1
        shutil.copy2(base_apk, unsigned)
        with zipfile.ZipFile(unsigned, "a", zipfile.ZIP_DEFLATED) as z:
            for d in dexes:
                z.write(d, os.path.basename(d))
                print("    写入 %s (%.2f MB)"
                      % (os.path.basename(d), os.path.getsize(d) / 1024 / 1024))
        run([ZIPALIGN, "-f", "-p", "4", unsigned, aligned], "    zipalign")

    # ---- 7. 签名 ----
    if reached("sign"):
        ks = os.path.join(os.environ.get("USERPROFILE", ""),
                          ".android", "debug.keystore")
        print()
        print("### 7/7 签名")
        if not os.path.exists(ks):
            print("!!! 缺少 debug.keystore。执行：")
            print('  keytool -genkeypair -v -keystore "%s" -storepass android '
                  '-keypass android -alias androiddebugkey -keyalg RSA '
                  '-keysize 2048 -validity 10000 '
                  '-dname "CN=Android Debug,O=Android,C=US"' % ks)
            return 1
        run([APKSIGNER, "sign", "--ks", ks,
             "--ks-pass", "pass:android", "--key-pass", "pass:android",
             "--ks-key-alias", "androiddebugkey", "--out", final, aligned],
            "    apksigner")
        if os.path.exists(final):
            print()
            print("=" * 62)
            print("构建成功: %s" % final)
            print("大小: %.2f MB" % (os.path.getsize(final) / 1024 / 1024))
            print("=" * 62)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
