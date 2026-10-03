# Tohka MOD · Recode

对哔哩哔哩弹幕网用户 [My-血-233](https://space.bilibili.com/) 发布的 **Tohka MOD**（Riruriru Mod 1.1.2）
做全量逆向还原的项目。

本仓库包含：**还原出的完整源码**、**可编译工程**、**逐模块架构文档**、以及**构建好的 APK**。

---

## 这是什么

Tohka MOD 是一款针对两款 Unity + IL2CPP 手游的**运行期修改器**：

| 目标游戏 | 包名 | 使用的载荷 |
| --- | --- | --- |
| Ark ReCode | `com.nerversoft.ark.recode` | `libArkRe.so` |
| Cherry Tale | `com.neversoft.rpg.erolabs` | `libCherryTale.so` |

它由三个层次组成：

```
宿主 App（Android，Java）
  ├─ com.Riruriru.Sx        8 个类    业务层：悬浮窗、解压载荷、调用注入器
  └─ irene.window.algui    28 个类    框架层：AlGui 悬浮窗框架（作者：艾琳）
        │
        ▼
注入器  assets/64（AArch64 ELF 可执行文件）
  └─ ptrace 附加游戏进程 → 远程 mmap → 远程 dlopen 载荷
        │
        ▼
载荷  libArkRe.so / libCherryTale.so（AArch64 共享库）
  ├─ ImGui 菜单（Dear ImGui 1.90.0 + OpenGL ES 3）
  ├─ il2cpp 挂钩（251 个 il2cpp API）
  └─ 内嵌 injected.dex（ImGui 覆盖层的 Java 侧）
```

---

## 源码作用（逐目录说明）

### `restore/host-apk/src/main/java/com/Riruriru/Sx/` —— 宿主业务层

| 文件 | 作用 |
| --- | --- |
| `MainActivity.java` | 入口。申请权限、跳转加群/赞助 |
| `FloatStartService.java` | 前台服务启动器 |
| `FloatServiceView.java` | 前台服务，读日志文件回显 |
| `FloatControlView.java` | 悬浮球：拖动、吸附、显隐动画 |
| **`FloatContentView.java`** | **核心面板**：解压三个载荷 → 起注入器 → 回显日志。**改这里可以改注入流程** |
| `Miscellaneous.java` | 工具方法（shell 执行、可逆加解密、跳转） |
| `Start.java` | 12 个空壳占位（原版遗留，可删） |

**关键算法**：`可逆加密` / `可逆解密` 是同一个函数 —— `char ^ 't'`（异或 116，对合）。

### `restore/host-apk/src/main/java/irene/` —— AlGui 悬浮窗框架

原作者**艾琳**的通用悬浮窗 UI 框架，28 个类：

| 类 | 作用 |
| --- | --- |
| `AlGui.java` | 主类：窗口管理、控件创建、配置 |
| `AlGuiData.java` | 数据模型与配置项 |
| `AlGuiPrefabricatedMenu_LB.java` | 预制菜单控件（开关/拖动条/按钮/折叠菜单） |
| `AlGuiBubbleNotification.java` | 气泡通知 |
| `AlGuiWindowView.java` | 窗口视图基类 |
| `AlGuiSoundEffect.java` | 音效 |
| `Tools/`（10 个） | SystemTool / FileTool / AppTool / ImageTool / RealTimeDataTextTool 等 |
| `CustomizeView/`（4 个） | GradientTextView / MarqueeTextView / vFrameLayout / vLinearLayout |

**改这里可以改 UI 外观与交互**。

### `restore/injected-dex/src/com/example/imgui/` —— ImGui 覆盖层

被载荷在运行时加载的 Java 代码：

| 文件 | 作用 |
| --- | --- |
| `ImGui.java` | **建 GLSurfaceView 覆盖层挂到 Activity 上**；多点触控分流（菜单区域吃掉触摸，区域外透传给游戏）；输入法桥接 |
| `GLES3JNIView.java` | GLSurfaceView 子类，渲染回调转发给 native |

**改这里可以改菜单的交互行为**（比如触摸判定、窗口位置）。

### `restore/injector/src/injector.c` —— 注入器

`assets/64` 的 C 语言复刻版，功能等价，但**去掉了 `setenforce 0`**（不改整机 SELinux）。

```
用法: injector -pkg <包名> -lib <SO 路径>
      injector -pid <PID>  -lib <SO 路径>
```

### `restore/docs/` —— 逆向文档（6 份）

| 文档 | 内容 |
| --- | --- |
| `module-host-business.md` | 业务层逐方法还原、注入链、全部硬编码常量 |
| `module-algui.md` | AlGui 框架 API 面 |
| `module-imgui-overlay.md` | 触摸分发与视图挂载的完整逻辑 |
| `module-native-payload.md` | 载荷功能矩阵、251 个 il2cpp API、菜单结构树 |
| `module-injector.md` | 注入器行为、112 条字符串、编译坑 |
| `module-resources.md` | 资源清单、Manifest 还原 |

---

## 教程

安装使用、从源码构建、逆向复现的完整步骤 → **[TUTORIAL.md](TUTORIAL.md)**

---

## 复用方法

### 方式一：直接用构建好的 APK

从 [Releases](../../releases) 下载 `TohkaMOD-Recode-*.apk`，安装即可。

**前提**：
1. 设备已 root（注入器需要 `ptrace` 权限）
2. 已安装目标游戏（Ark ReCode 或 Cherry Tale）
3. 授予悬浮窗权限

### 方式二：从源码构建

```bash
git clone https://github.com/BanCN-Re/Tohka-MOD-Recode.git
cd Tohka-MOD-Recode/restore/host-apk

# 需要 Android SDK + Gradle 8.x
# 载荷（assets/*.so）不在仓库里，需要自备
export ANDROID_HOME=/path/to/android-sdk
gradle assembleDebug
```

**注意**：`src/main/assets/` 下的三个载荷文件（`64` / `libArkRe.so` / `libCherryTale.so`）
因体积原因**不在仓库内**（共约 16 MB）。需要自备，或从 Release 的完整包里取。

### 方式三：只复用某一部分

| 想做什么 | 改哪里 |
| --- | --- |
| 改注入逻辑（换游戏/换载荷） | `host-apk/.../FloatContentView.java` 的 `startInjection()` |
| 改悬浮窗 UI 外观 | `host-apk/.../irene/window/algui/AlGui*.java` |
| 改菜单触摸行为 | `injected-dex/src/.../ImGui.java` 的 `handleTouchEvent()` |
| 换注入器（不用 ptrace） | `injector/src/injector.c` |
| 自己写一份载荷 | 照 `restore/payload/INTERFACE.md` 的契约实现 13 个 JNI 导出 |

### 方式四：接入自己的游戏

1. 改 `FloatContentView` 里的包名与载荷路径
2. 你的游戏需要是 **Unity + IL2CPP + arm64-v8a**
3. 载荷需要知道你的游戏类名/字段名（改 `libArkRe.so` 不现实，建议照契约重写）

---

## 构建产物

| 文件 | 说明 |
| --- | --- |
| `TohkaMOD-Recode-debug.apk` | 调试签名，可直接安装 |

签名是 debug keystore。如果要与目标游戏共享 UID，**签名必须与游戏一致**，
否则 `sharedUserId` 会校验失败。

---

## 项目结构

```
Tohka-MOD-Recode/
├─ README.md                      本文件
├─ LICENSE                        MIT
├─ DISCLAIMER.md                  免责声明
├─ CREDITS.md                     作者与致谢
├─ restore/
│  ├─ README.md                   还原工程总览
│  ├─ ARCHITECTURE.md             全量架构
│  ├─ VERIFY.md                   验收报告
│  ├─ VERIFY-DIFF.md              对拍验证
│  ├─ docs/                       6 份模块文档
│  ├─ host-apk/                   ★ 可编译工程
│  │  ├─ build.gradle
│  │  ├─ src/main/java/           36 个还原的 Java 类
│  │  ├─ src/main/res/            838 个资源
│  │  └─ src/main/AndroidManifest.xml
│  ├─ injected-dex/src/           ImGui 覆盖层源码
│  ├─ injector/src/               注入器 C 源码
│  ├─ payload/INTERFACE.md        载荷接口契约
│  └─ tools/                      还原用的脚本
└─ docs/                          分析报告（原始取证）
```

---

## 还原方法与验证

**方法**：
1. jadx 1.5.0 反编译 host APK 与内嵌 dex
2. 自研 `dalvik2java.py`（asmli→Java）做交叉验证
3. capstone 5.0.7 反汇编 native 层（注入器、载荷、卡密逻辑）
4. androguard 4.1.4 解析 dex 与 AXML

**验证**（见 `restore/VERIFY.md`、`restore/VERIFY-DIFF.md`）：
- 还原源码的 **59 条字符串 100% 命中**原始 dex
- **关键常量 27/27 一致**（包名、载荷名、命令、路径、UI 提示语）
- ImGui 层**完整编译通过**（生成 9 个 class）
- 全部 36 个类**语法解析通过**

**局限**：两个载荷 `.so`（7.6 MB / 8.4 MB 优化后的 AArch64 机器码）
**无法还原成原始 C++ 源码**。已还原到结构级：功能矩阵、251 个 il2cpp API、
菜单结构树、MCP 接口、卡密状态机 —— 见 `restore/payload/INTERFACE.md`。

---

## 作者与致谢

| 角色 | 承担者 |
| --- | --- |
| **原始 MOD 作者** | 哔哩哔哩 **My-血-233**（Riruriru Mod 1.1.2） |
| **AlGui 框架作者** | **艾琳**（irene，`irene.window.algui`，28 个类） |
| **逆向** | **GLM 5.3** —— DEX/AXML 拆解、native 汇编分析、卡密状态机定位、载荷接口提取 |
| **文档整理与推断** | **Opus 5.5** —— 模块文档、jadx 失真成因推断、修复策略、架构说明 |
| **还原实现** | **DeepSeek V4.1 Flash** —— 源码级还原、修复全部编译错误、搭可编译工程、构建验证与发布 |
| **仓库** | [BanCN-Re/Tohka-MOD-Recode](https://github.com/BanCN-Re/Tohka-MOD-Recode) |

三者的具体分工见 [CREDITS.md](CREDITS.md)。

详见 [CREDITS.md](CREDITS.md)。

---

## 协议

本项目以 **MIT License** 发布 —— 见 [LICENSE](LICENSE)。

```
Copyright (c) 2026 BanCN
```

**注意**：MIT 协议只覆盖**本仓库的逆向还原成果**（还原出的源码、文档、工具）。
原始 MOD 的著作权归原作者所有，本项目不主张任何权利。

---

## 免责声明

**使用本项目造成的任何后果，由使用者自行承担。**

详见 [DISCLAIMER.md](DISCLAIMER.md)。核心要点：

- 本项目**仅供安全研究与技术学习**
- 不得用于侵犯他人权益、破坏计算机信息系统、或任何违法用途
- 使用前请确认你有合法授权
- 作者不对任何直接或间接损失负责

---

## 已知限制

| 项 | 说明 |
| --- | --- |
| 载荷未随仓库分发 | 体积原因（16 MB），需自备或从 Release 取 |
| 载荷不可源码级还原 | 优化后的 AArch64 机器码 |
| 需 root | 注入器依赖 `ptrace` |
| 只支持 arm64-v8a 游戏 | 载荷是 AArch64 |
| 与游戏版本强绑定 | 游戏更新后类名/字段名变化会导致载荷失效 |
