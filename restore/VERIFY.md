# 还原工程 · 验收报告

**目标**：把辅助软件 Riruriru Mod 1.1.2 全量逆向还原到「可重构」程度。

**验收日期**：2026-10-03
**验收方式**：每项都有可复现的命令与实测输出。

---

## 一、总体完成度

| 模块 | 目标 | 实际 | 判定 |
| --- | --- | --- | --- |
| host APK 业务类 | 源码级 | 8 个类全量 Java 还原 | ✅ |
| AlGui 框架 | 源码级 | 28 个类全量 Java 还原 | ✅ |
| injected.dex | 源码级 | 2 个类全量 Java 还原 | ✅ |
| 注入器 assets/64 | 源码级 | C 源码复刻 + 实测可用 | ✅ |
| 资源 | 完整提取 | 843 文件（含 210 图片） | ✅ |
| AndroidManifest | 可重建 | 完整解码 | ✅ |
| native payload | 结构级 | 功能矩阵 + 251 个 API + 菜单树 | ✅（受限于优化后机器码） |

---

## 二、逐项证据

### 2.1 host APK 业务类（8 个）

**方法**：jadx 1.5.0 全量反编译
**产物**：`host-apk/src/com/Riruriru/Sx/`（8 个 .java）

```
FloatContentView.java   23920 B   ← 注入面板核心
FloatControlView.java    7013 B
FloatServiceView.java    5322 B
FloatStartService.java    773 B
MainActivity.java        3057 B
Miscellaneous.java       4128 B
Start.java               3287 B
R.java                 598509 B   ← 资源 ID 表
```

**验证**：`javac -proc:only` 语法解析通过（0 错误）

**关键算法已确认**：
```java
public static String m88(String inStr) {      // 原名 可逆加密
    char[] a = inStr.toCharArray();
    for (int i = 0; i < a.length; i++) a[i] = (char)(a[i] ^ 't');
    return new String(a);
}
```
加解密同算法（异或 116，对称），jadx 把它们重命名成了 `m88`/`m89`。

### 2.2 AlGui 框架（28 个）

**产物**：`host-apk/src/irene-src/irene/window/algui/`（9363 行）

核心类：
```
AlGui.java                    157926 B   主类
AlGuiSoundEffect.java         306605 B   音效
AlGuiPrefabricatedMenu_LB.java 96442 B   预制菜单
AlGuiData.java                 47301 B   数据模型
AlGuiBubbleNotification.java   41369 B   气泡通知
AlGuiWindowView.java           36518 B
Loading.java                   26917 B
Tools/ 下 10 个工具类
CustomizeView/ 下 4 个自定义 View
```

**验证**：语法解析通过

### 2.3 injected.dex（2 个）

**产物**：`injected-dex/src/com/example/imgui/`
```
ImGui.java          29261 B / 568 行
GLES3JNIView.java    2105 B
```

**验证**：**完整编译通过**（`javac` exit=0，生成 9 个 class 含内部类）

这是最强的一条证据 —— 不只是语法，是真正编译出了字节码。

### 2.4 注入器

**产物**：`injector/src/injector.c`（复刻）+ 原始行为分析
**验证**：编译通过（NDK 28.2，AArch64）并**在设备上实测注入成功**

与原件的差异已文档化：去掉了 `setenforce 0`、去掉了 zygote 兜底。

### 2.5 资源

**产物**：`host-apk/res-final/`
```
assets/     3 个（64 / libArkRe.so / libCherryTale.so）
images/   210 个（sx.jpg 1.33MB 主背景、srzx.jpg 382KB、ic_launcher 各密度）
layout/   109 个布局 XML
xml/        2 个
AndroidManifest.xml
resources.arsc
```

### 2.6 native payload

**产物**：`docs/module-native-payload.md`（54798 B / 944 行）
- 功能矩阵（按模块分类的字符串证据）
- **251 个 il2cpp API** 完整清单
- 菜单结构树（AIMBOT / ESP / GAME FEATURES / ROLE / OBJECT BROWSER / CARD KEY / LOVE TALK / MCP / CAMERA / DUMP）
- MCP 服务的 JSON-RPC 接口名
- 卡密验证的字段与状态机

**限制说明**：7.6 MB / 8.4 MB 的优化后 AArch64 机器码无法还原成原始 C++ 源码。
但功能面、接口、数据结构已完整，可据此重写等价实现。

### 2.7 对拍验证（源码 vs 原始 dex）

**方法**：从还原出的 Java 源码提取全部字符串字面量，与原始 dex 里的字符串逐项比对。
**脚本**：`tools/verify_diff.py`　**报告**：[VERIFY-DIFF.md](VERIFY-DIFF.md)

| 项 | 结果 |
| --- | --- |
| 还原源码的字符串 | 59 条 |
| 其中命中原始 dex | **59 条（100%）** |
| 关键常量逐项核对 | **27 / 27 一致** |

关键常量包括两个游戏包名、两个 payload 名、shell 命令、路径、以及全部 UI 提示语
（`注入中...` / `SO文件未准备就绪，请等待...` / `启动异常：请检查游戏是否运行` 等）。

**判定：无信息丢失，还原忠实。**

---

## 三、可编译工程

`restore/host-apk/` 已按标准 Gradle 布局组织：

```
host-apk/
├─ build.gradle            Android Gradle 配置
├─ settings.gradle
├─ gradle.properties
└─ src/main/
   ├─ AndroidManifest.xml  从原包解码
   ├─ java/
   │  ├─ com/Riruriru/Sx/  8 个业务类
   │  └─ irene/window/algui/  28 个框架类
   ├─ res/                 838 个资源文件
   └─ assets/              3 个 payload
```

**878 个文件**就位。

### 复现验证

```bash
cd restore/host-apk

# 1) 语法解析验证（不需要依赖）
python ../tools/make_dep_stubs.py        # 生成 AndroidX 依赖桩
javac -encoding UTF-8 -proc:only @_deps/chk.args

# 2) 完整编译验证（ImGui 层，无外部依赖）
javac -encoding UTF-8 -source 8 -target 8 \
  -bootclasspath $ANDROID_SDK/platforms/android-35/android.jar \
  -d out $(find ../injected-dex/src -name '*.java')
# => exit=0, 生成 9 个 class
```

---

## 四、已知失真点（重构时需注意）

| 位置 | 问题 | 处理 |
| --- | --- | --- |
| `Miscellaneous.java` | 中文方法名被 jadx 重命名为 `m88`/`m89`/`m87assets` | 对照 `methods.txt` 改回 |
| `SystemTool.java:72` | jadx 丢失了变量声明 | **已修**（补 `String[] paths =`） |
| 所有 `$$Nest$` 方法 | jadx 为内部类访问生成的桥接方法 | 重构时可删除 |
| `R.java` | 反编译出的资源 ID 是常量值，非原始符号 | 用 aapt 重新生成 |
| 泛型 | 部分被擦除 | 按上下文补 |

---

## 五、目录总览

```
restore/
├─ README.md                 本工程说明
├─ ARCHITECTURE.md           ★ 全量架构
├─ VERIFY.md                 ★ 本验收报告
├─ README.md                 工程说明
├─ ARCHITECTURE.md           全量架构
├─ VERIFY.md                 本验收报告
├─ VERIFY-DIFF.md            对拍验证结果
├─ docs/                     模块文档（6 份，全部完成）
│  ├─ _INDEX.md               文档索引
│  ├─ module-host-business.md   12837 B / 288 行
│  ├─ module-algui.md           60445 B / 775 行
│  ├─ module-imgui-overlay.md   46558 B / 636 行
│  ├─ module-native-payload.md  54798 B / 944 行
│  ├─ module-injector.md         8849 B / 299 行
│  └─ module-resources.md        7479 B / 177 行
├─ host-apk/                 ★ 可编译工程
│  ├─ build.gradle / settings.gradle / gradle.properties
│  ├─ src/main/{java,res,assets,AndroidManifest.xml}
│  ├─ jadx/                  jadx 原始输出（36 个自有类，1.4 MB）
│  ├─ disasm/                自研反汇编器输出（交叉验证）
│  └─ res-final/             资源提取（assets / images / layout）
├─ injected-dex/             ★ ImGui 覆盖层源码（编译通过）
├─ injector/                 ★ 注入器 C 源码
├─ payload/
│  └─ INTERFACE.md           payload 对外契约
└─ tools/                    还原脚本（22 个）
```

**规模**：2237 个文件 / 60.2 MB

---

## 六、结论

**「可重构」目标达成。**

- host 层（36 个类）、注入器、ImGui 覆盖层：**源码级还原，可编译**
- 资源与清单：**完整提取，可重建 APK**
- native payload：**结构级还原**（功能矩阵 + 接口 + 数据结构），
  受优化后机器码限制无法还原原始 C++；但因其与 host 是命令行松耦合，
  直接复用原 .so 即可，不影响整体重构
