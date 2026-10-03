"""建立可重构项目的骨架，并把已还原的成果落到正确位置。

项目结构（目标）：
  restore/
    README.md                 还原说明与验收标准
    ARCHITECTURE.md           全量架构
    host-apk/
      src/                    ★ jadx 还原的 Java 源码（8 个自有类）
      res-final/              ★ 资源（assets / 图片 / 布局 / manifest / arsc）
      disasm/                 反汇编（我的还原器产出，作为交叉验证）
      jadx/                   jadx 全量输出（参考）
      build.gradle            重建用构建脚本
    injected-dex/
      src/                    ★ ImGui 覆盖层 Java 源码
      build.gradle
    payload/
      libArkRe/               ★ native 侧还原
      libCherryTale/
    injector/
      src/                    ★ assets/64 的 C 源码（已有基线）
    tools/                    还原用的脚本
"""
import os
import shutil

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
R = os.path.join(ROOT, "restore")


def main():
    # 1) host-apk/src 用 jadx 的产出（质量最好）
    src = os.path.join(R, "host-apk", "src")
    # 递归复制 com/Riruriru 与 irene 下的全部 java（都是自有代码）
    jadx_root = os.path.join(R, "host-apk", "jadx", "sources")
    n = 0
    ni = 0
    for dp, dn, fn in os.walk(jadx_root):
        rel = os.path.relpath(dp, jadx_root)
        keep = (rel == "com" or rel.startswith("com" + os.sep + "Riruriru")
                or rel == "irene" or rel.startswith("irene" + os.sep))
        if not keep:
            continue
        for f in fn:
            if not f.endswith(".java"):
                continue
            if rel.startswith("com"):
                dst = os.path.join(src, rel, f)
                n += 1
            else:
                dst = os.path.join(src, "irene-src", rel, f)
                ni += 1
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(os.path.join(dp, f), dst)
    print("host-apk/src: com.Riruriru %d 个类" % n)
    print("host-apk/src: irene(AlGui框架) %d 个类" % ni)

    # 顺带看看 irene 包（可能是作者相关）
    ir = os.path.join(jadx_root, "irene")
    if os.path.isdir(ir):
        ni = 0
        for dp, dn, fn in os.walk(ir):
            ni += len([f for f in fn if f.endswith(".java")])
        print("host-apk: 另有 irene 包 %d 个类（第三方库，暂不还原）" % ni)

    # 2) injected-dex/src
    isrc = os.path.join(R, "injected-dex", "src")
    ijadx = os.path.join(R, "injected-dex", "sources", "com", "example", "imgui")
    if os.path.isdir(ijadx):
        os.makedirs(os.path.join(isrc, "com", "example", "imgui"), exist_ok=True)
        n = 0
        for f in os.listdir(ijadx):
            if f.endswith(".java"):
                shutil.copy2(os.path.join(ijadx, f),
                             os.path.join(isrc, "com", "example", "imgui", f))
                n += 1
        print("injected-dex/src: 复制 %d 个类" % n)

    # 3) injector/src 用我写的 C 实现
    idir = os.path.join(R, "injector", "src")
    os.makedirs(idir, exist_ok=True)
    for f in ("injector.c", "probe.c"):
        p = os.path.join(ROOT, "replicate", f)
        if os.path.exists(p):
            shutil.copy2(p, os.path.join(idir, f))
            print("injector/src: %s" % f)

    # 4) 目录骨架
    for d in ("payload/libArkRe", "payload/libCherryTale", "tools"):
        os.makedirs(os.path.join(R, d), exist_ok=True)

    # 5) 还原脚本收进来
    for f in os.listdir(os.path.join(ROOT, "replicate")):
        if f.endswith(".py"):
            shutil.copy2(os.path.join(ROOT, "replicate", f),
                         os.path.join(R, "tools", f))
    print("tools: %d 个脚本" % len([x for x in os.listdir(os.path.join(R, "tools")) if x.endswith(".py")]))

    print()
    print("骨架建好:", R)


if __name__ == "__main__":
    main()
