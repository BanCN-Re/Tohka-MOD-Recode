# Riruriru Mod 全量逆向还原工程

把 Android 辅助软件 **Riruriru Mod 1.1.2** 完整还原到**可重构**程度：
拿到这个目录，能重新编译出功能等价的软件。

- [ARCHITECTURE.md](ARCHITECTURE.md) — 全量架构（先看这个）
- [VERIFY.md](VERIFY.md) — 验收报告（每项都有证据）
- [docs/_INDEX.md](docs/_INDEX.md) — 模块文档索引

---

## 软件构成（三层）

```
Riruriru Mod 1.1.2
│
├─ host 层（Android App，Java）
│  ├─ com.Riruriru.Sx        8 个类   ← 业务：悬浮窗、调用注入器
│  └─ irene.window.algui    28 个类   ← AlGui 悬浮窗框架
│
├─ 注入器  assets/64
│  └─ AArch64 ELF 14120 B             ← ptrace 向游戏进程注入 .so
│
└─ payload  assets/*.so
   ├─ libArkRe.so       7587080 B     ← Ark ReCode 专用
   └─ libCherryTale.so  8446416 B     ← Cherry Tale 专用
      各内嵌 injected.dex 16136 B      ← ImGui 覆盖层的 Java 侧
```

---

## 还原成果

| 模块 | 程度 | 位置 | 验证 |
| --- | --- | --- | --- |
| host 业务类 8 个 | 源码级 | `host-apk/src/com/Riruriru/Sx/` | 语法解析通过 |
| AlGui 框架 28 个 | 源码级 | `host-apk/src/irene-src/` | 语法解析通过 |
| injected.dex 2 个 | 源码级 | `injected-dex/src/` | **完整编译通过** |
| 注入器 | 源码级 | `injector/src/injector.c` | 编译 + 设备实测 |
| 资源 843 个 | 完整 | `host-apk/res-final/` | — |
| Manifest | 完整 | `host-apk/src/main/AndroidManifest.xml` | 解码验证 |
| native payload | 结构级 | `docs/module-native-payload.md` | 见下方说明 |

**native payload 的说明**：两个 .so 是优化后的 AArch64 机器码（7.6/8.4 MB），
无法还原成原始 C++ 源码。但已还原出：
- 功能矩阵（按模块分类，每条有字符串证据）
- 251 个 il2cpp API 的完整清单
- 菜单结构树（10 个板块）
- MCP 服务的 JSON-RPC 接口
- 卡密验证的状态机与字段

由于 payload 与 host 之间是**命令行松耦合**（`-pkg X -lib Y`），
重构时直接复用原 .so 即可，不影响整体。

---

## 可编译工程

`host-apk/` 是标准 Gradle 布局，878 个文件已就位：

```
host-apk/
├─ build.gradle / settings.gradle / gradle.properties
└─ src/main/
   ├─ AndroidManifest.xml
   ├─ java/com/Riruriru/Sx/     8 个业务类
   ├─ java/irene/window/algui/  28 个框架类
   ├─ res/                      838 个资源
   └─ assets/                   3 个 payload
```

### 构建

```bash
cd host-apk
./gradlew assembleDebug        # 需要联网拉 AndroidX 依赖
```

### 不联网的验证方式

```bash
# 1) 语法解析（不需要依赖）
cd ..
python tools/make_dep_stubs.py           # 从原 APK 生成依赖桩
javac -encoding UTF-8 -proc:only @host-apk/_deps/chk.args

# 2) 完整编译（ImGui 层无外部依赖，可直接编）
javac -encoding UTF-8 -source 8 -target 8 \
  -bootclasspath $SDK/platforms/android-35/android.jar \
  -d out $(find injected-dex/src -name '*.java')
```

---

## 目录结构

```
restore/
├─ README.md              本文件
├─ ARCHITECTURE.md        全量架构
├─ VERIFY.md              验收报告
│
├─ docs/                  模块文档
│  ├─ _INDEX.md
│  ├─ module-host-business.md    业务类逐方法说明
│  ├─ module-algui.md            AlGui 框架 API
│  ├─ module-imgui-overlay.md    ImGui 覆盖层（触摸分发给重点）
│  ├─ module-native-payload.md   payload 功能矩阵
│  ├─ module-injector.md         注入器行为还原
│  └─ module-resources.md        资源与清单
│
├─ host-apk/              ★ 可编译工程
│  ├─ src/main/              Gradle 标准布局
│  ├─ jadx/                  jadx 原始输出
│  ├─ disasm/                独立反汇编器输出
│  ├─ res-final/             资源提取
│  └─ _deps/                 依赖桩（仅校验用）
│
├─ injected-dex/          ★ ImGui 覆盖层源码
├─ injector/              ★ 注入器 C 源码
├─ payload/               payload 分析
└─ tools/                 还原脚本（22 个）
   ├─ dump_host_disasm.py     反汇编 dex
   ├─ dalvik2java.py          自研 smali→Java 还原器
   ├─ make_buildable.py       整理 Gradle 布局
   ├─ make_dep_stubs.py       生成依赖桩
   ├─ carve_dex.py            提取内嵌 dex
   └─ find_authvar.py         定位卡密状态变量
```

---

## 还原方法（可复现）

| 步骤 | 工具 | 说明 |
| --- | --- | --- |
| 1. 反编译 host | jadx 1.5.0 | `jadx -d out --no-res <apk>` |
| 2. 反汇编交叉验证 | androguard 4.1.4 + 自研脚本 | `dump_host_disasm.py` |
| 3. smali→Java | 自研 `dalvik2java.py` | jadx 失败时的备份路线 |
| 4. 反编译 dex | jadx | 对提取出的 injected.dex |
| 5. 提资源 | python zipfile | `extract_res_final.py` |
| 6. 解 manifest | androguard AXMLPrinter | — |
| 7. 汇编级分析 | capstone 5.0.7 | payload/注入器/卡密 |

工具链版本：jadx 1.5.0 / androguard 4.1.4 / capstone 5.0.7 / JDK 17 / NDK 28.2

---

## 已知问题

| 位置 | 问题 | 状态 |
| --- | --- | --- |
| `SystemTool.java:72` | jadx 丢失变量声明 | 已修 |
| `Miscellaneous.java` | 中文方法名被重命名为 m88/m89 | 重构时按 `methods.txt` 改回 |
| `R.java` | 反编译的资源 ID 是常量 | 用 aapt 重新生成 |
| `AlGuiSoundEffect.java` | 306 KB，含大段 base64 音效资源 | 看结构即可，不必逐行读 |
