# 教程

三个层次，按你的需要挑：

| 你是 | 看哪节 |
| --- | --- |
| 想直接用这个修改器 | [一、安装使用](#一安装使用) |
| 想改源码重新构建 | [二、从源码构建](#二从源码构建) |
| 想复现整个逆向过程 | [三、逆向复现](#三逆向复现) |

---

## 一、安装使用

### 1.1 前置条件

| 项 | 要求 | 怎么确认 |
| --- | --- | --- |
| Android 版本 | 6.0+ | 设置 → 关于手机 |
| Root | **必须** | 有 KernelSU / Magisk 即可 |
| 悬浮窗权限 | **必须** | 装完 App 后手动授予 |
| 存储权限 | **必须** | 首次启动会弹窗 |

**为什么必须 root**：注入器靠 `ptrace` 附加游戏进程，普通权限做不到。

### 1.2 安装

```bash
adb install TohkaMOD-Recode-v1.1.2.apk
```

或者把 APK 拷到设备上点击安装。

> **装不上？** 如果设备上已有原版 MOD，**必须先卸载**：
> ```bash
> adb uninstall com.Riruriru.Sx
> ```
> 原因：重建版与原版签名不同，Android 不允许签名不一致的覆盖安装。

### 1.3 首次启动

**第 1 步：给权限（最重要）**

启动后会弹出权限申请，**全部点允许**。如果误点了拒绝，App 会启动失败。

手动补授：

```
设置 → 应用 → Riruriru Mod → 权限 → 全部允许
设置 → 应用 → 特殊权限 → 显示在其他应用上层 → Riruriru Mod → 允许
```

**第 2 步：确认主界面**

正常应该看到：
- 背景是一张动漫图
- 标题 `Riruriru Mod`
- 三个按钮：**加入售后群** / **作者B站主页** / **购买卡密**
- **左上角有个悬浮球**

看到悬浮球就说明 `FloatControlView` 正常工作了。

**第 3 步：等 5 秒**

启动后前 5 秒别操作 —— 后台有 profile 优化任务在跑，此时点击可能无响应。

### 1.4 使用

1. **点悬浮球** → 打开注入面板
2. 面板里**选游戏**：
   - Ark ReCode（`com.nerversoft.ark.recode`）
   - Cherry Tale（`com.neversoft.rpg.erolabs`）
3. 点**启动注入**
4. 等面板日志出现成功提示
5. **切到游戏** → 菜单会自动出现（ImGui 覆盖层）

### 1.5 常见问题

| 现象 | 原因 | 解决 |
| --- | --- | --- |
| 装不上，提示签名冲突 | 设备上有原版 | `adb uninstall com.Riruriru.Sx` |
| 启动后立即闪退 | 缺存储权限 | 手动授予全部权限 |
| 启动后约 5 秒消失 | 依赖缺失（旧版本 bug） | 用 Release 里的 **v1.1.2** 或更新 |
| 悬浮球不出现 | 缺悬浮窗权限 | 设置里给「显示在其他应用上层」 |
| 注入失败 | 游戏没运行 / 游戏版本不匹配 | 先启动游戏，确认游戏版本 |
| 菜单不出来 | 载荷与游戏版本绑定了 | 载荷是针对特定游戏版本编译的，游戏更新后可能失效 |

---

## 二、从源码构建

### 2.1 环境

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | javac 要用 |
| Android SDK | API 35 | 提供 `android.jar` |
| build-tools | 34.0.0 | 提供 aapt2 / d8 / zipalign / apksigner |
| Python | 3.10+ | 跑构建脚本 |
| NDK | 28.x | 只在要重编注入器时需要 |

设置环境变量：

```bash
export ANDROID_HOME=/path/to/android-sdk
```

### 2.2 准备载荷

`assets/` 下三个载荷**不在仓库里**（体积大，且属于原作者作品）：

```
64                 14 KB      注入器（AArch64 ELF）
libArkRe.so        7.6 MB     Ark ReCode 载荷
libCherryTale.so   8.4 MB     Cherry Tale 载荷
```

放到 `restore/host-apk/src/main/assets/`。

也可以直接从原 APK 提取：

```bash
unzip -j "Riruriru Mod_1.1.2.apk" "assets/*" -d restore/host-apk/src/main/assets/
```

### 2.3 构建

```bash
cd restore
python tools/build_apk.py
```

产出：`host-apk/build-manual/TohkaMOD-Recode.apk`

**分步调试**（出错时用）：

```bash
python tools/build_apk.py --step resources   # 只编译自有资源
python tools/build_apk.py --step javac       # 跑到 javac
python tools/build_apk.py --step d8          # 跑到 dex 生成
```

### 2.4 构建流程（7 步）

```
1. aapt2 compile    自有 res -> .flat
2. gen_rjava.py     从原 APK 资源表反向生成 R.java
3. 组装 base.apk    原 APK 的 res/arsc + 我们的 manifest + assets
4. javac            源码 + R.java -> 478 个 .class
5. d8               .class + 依赖 jar -> classes.dex
6. 打包             base.apk + classes.dex -> zipalign
7. apksigner        签名
```

**为什么不用 Gradle**：见 [COMPILE-FIXES.md](restore/COMPILE-FIXES.md) 第 3.1 节。
简单说，AndroidX 的 2000+ 资源会让 `aapt2 link` 的命令行超 Windows 上限，
而 aapt2 不支持 `@argfile`。Gradle 用内部 API 绕过了这点，我们绕不过，
所以改成**复用原 APK 已编译好的资源**。

### 2.5 改代码

| 想改什么 | 改哪里 |
| --- | --- |
| 注入流程 / 换游戏 | `src/main/java/com/Riruriru/Sx/FloatContentView.java` 的 `startInjection()` |
| 主界面按钮 | `src/main/res/layout/activity_main.xml` |
| 主界面逻辑 | `src/main/java/com/Riruriru/Sx/MainActivity.java` |
| 悬浮球外观 | `src/main/java/com/Riruriru/Sx/FloatControlView.java` |
| 悬浮窗框架 | `src/main/java/irene/window/algui/AlGui*.java` |
| 注入器 | `injector/src/injector.c`（改完要重编放到 assets/64） |

### 2.6 重编注入器

```bash
NDK=/path/to/ndk/28.2.13676358/toolchains/llvm/prebuilt/windows-x86_64/bin

# AArch64（给 ARM 游戏用）
$NDK/aarch64-linux-android21-clang -O2 -o assets/64 injector/src/injector.c

# x86_64（如果游戏跑在翻译层下）
$NDK/x86_64-linux-android21-clang -O2 -o assets/64 injector/src/injector.c
```

> **不要加 `-static`** —— 静态链接的二进制在 houdini 翻译层下会启动即崩。

---

## 三、逆向复现

这一节完整记录**从 APK 到可编译源码**的过程，包括所有踩过的坑。

### 3.1 工具准备

```bash
# jadx（反编译 Java）
wget https://github.com/skylot/jadx/releases/download/v1.5.0/jadx-1.5.0.zip
unzip jadx-1.5.0.zip -d tools/jadx

# Python 依赖
pip install androguard==4.1.4 capstone==5.0.7
```

### 3.2 反编译（关键步骤）

**这一步的参数选择决定了后面 260 个编译错误的存亡。**

```bash
# ❌ 错误：默认模式会内联匿名类，产生 260 个编译错误
jadx -d out --no-res --show-bad-code "Riruriru Mod_1.1.2.apk.1"

# ✅ 正确：不内联匿名类
jadx -d out --no-res --show-bad-code --no-inline-anonymous \
     "Riruriru Mod_1.1.2.apk.1"
```

**为什么**：

jadx 默认会把匿名类内联回原位置，但它会**错误地给匿名类补构造参数**：

```java
// 源码（合法）
new View.OnClickListener() {
    public void onClick(View v) { AlGui.this.clearMenu(); }
}

// jadx 默认模式还原成（不合法）
new View.OnClickListener(this) {        // ← 匿名类不能有构造参数
    private final AlGui this$0;
    { this.this$0 = this; }             // ← this 是匿名类实例，字段却是 AlGui
    public void onClick(View v) { this.this$0.clearMenu(); }
}
```

加了 `--no-inline-anonymous` 后，jadx 把匿名类提升成**标准成员类**：

```java
class AlGui$100000017 implements View.OnClickListener {
    private final AlGui this$0;
    AlGui$100000017(AlGui alGui) {      // ← 构造函数正确接收 outer
        this.this$0 = alGui;            // ← 类型匹配
    }
}
// 调用处： new AlGui$100000017(this)
```

**这一个参数把错误从 260 个降到 1 个。**

> ⚠️ **不要**同时加 `--no-move-inner-classes`。
> 那个参数会让 jadx 用 `android.os.Build$VERSION` 形式写 import，
> Java 源码里不合法（必须写 `Build.VERSION`），反而新增 230 个错误。

### 3.3 交叉验证

jadx 不是唯一真相。用第二个工具对一遍：

```bash
# 自己写个反汇编器（本项目提供了 dalvik2java.py）
python tools/dump_host_disasm.py     # 输出 smali 风格反汇编
python tools/dalvik2java.py          # 转成 Java
```

两份结果比对，能发现 jadx 的失真点。

**对拍验证**（确认还原没丢信息）：

```bash
python tools/verify_diff.py
# 输出：还原源码里的 59 条字符串 100% 命中原始 dex
#       关键常量 27/27 一致
```

### 3.4 资源处理

```bash
# 1. 解出 APK 里的资源
python tools/extract_res_final.py

# 2. 只保留自有资源（其余交给 AndroidX 依赖）
python tools/rebuild_res.py

# 3. 二进制 AXML 转文本
python tools/decode_axml.py

# 4. 数值规范化（aapt2 要的是源码写法，不是编译后的内部表示）
python tools/normalize_layout.py
```

**第 4 步为什么需要**：

`androguard` 解出来的 AXML 属性值是**编译后的内部表示**：

```xml
<!-- 解出来是这样（aapt2 不认） -->
android:layout_width="-1"
android:orientation="1"
android:gravity="0x00000011"
android:padding="30.000000dip"

<!-- 必须转成 -->
android:layout_width="match_parent"
android:orientation="vertical"
android:gravity="center"
android:padding="30dip"
```

映射表在 `tools/normalize_layout.py` 的 `ENUMS` 里。

### 3.5 补 AndroidX 的 R 类

**问题**：AndroidX 每个库都有自己的 R 类（`androidx.startup.R$string`），
它们**不随 AAR 发布** —— 由 Gradle 构建时从 R.txt 生成。
缺了会在运行时崩：

```
NoClassDefFoundError: Failed resolution of: Landroidx/startup/R$string;
```

**解法**：jadx 反编译原 APK 时已经产出了这些类。提取并合并：

```bash
python tools/extract_androidx_r_classes.py    # 或直接从 jadx 产物拷
python tools/merge_r_classes.py               # 顶层 R$Type -> 嵌套 R.Type
python tools/fix_styleable.py                 # 处理 int[] 类型的 styleable
```

**为什么需要合并**：jadx 产出的是**顶层类** `R$string`，
而源码引用的是**嵌套** `R.string` —— 两者在 Java 源码层面不等价。

### 3.6 处理 jadx 的还原瑕疵

修完上面还剩约 15 个独立错误，逐个记录在
[COMPILE-FIXES.md](restore/COMPILE-FIXES.md) 第二节。典型几类：

| 类型 | 现象 | 修法 |
| --- | --- | --- |
| 丢变量声明 | `new String[]{...};` 没有变量名 | 补上（从 smali 看原名） |
| 枚举重复 | `valueOf(String)` 与自动生成的冲突 | 删掉手写的 |
| 非 public 类 | 用了库的内部类 | 内联成常量 |
| 已删除 API | `setAppCacheEnabled` 等 | 删调用 |
| 类型歧义 | `android.os.Process` vs `java.lang.Process` | 加全限定名 |
| 丢 `return` | jadx 标了 `code lost: return false` | 补回 |
| 静态上下文 | 非静态成员类在静态方法里 new | 加 `static` |
| 逻辑写反 | jadx 标了 "decompiled incorrectly" | 按语义改正 |

### 3.7 构建链的坑

| 坑 | 现象 | 解法 |
| --- | --- | --- |
| aapt2 参数超长 | 24.7 万字符 > 32767 上限 | 复用原 APK 的 `resources.arsc` |
| aapt2 不支持 @argfile | 传了会打印帮助 | 同上 |
| R.java 生成失败 | `Theme.AppCompat not found` | 从资源表反向生成 |
| **aapt2 `-o` 行为** | 目录不存在时当成文件名 | **先建目录** |
| d8 类型重复 | `Type xxx is defined multiple times` | 按**类路径**去重 |
| manifest 非 AXML | `malformed binary resource` | 用 aapt2 编译 |

**最后那个 aapt2 `-o` 的坑值得单独说**：

```bash
# ❌ 目录不存在 → aapt2 把 "out" 当成输出文件名，生成一个叫 out 的文件
aapt2 compile --dir res -o out

# ✅ 先建目录
mkdir -p out && aapt2 compile --dir res -o out
```

### 3.8 运行时依赖排查

装上去崩了，看日志：

```bash
adb logcat | grep -A20 "FATAL EXCEPTION"
```

常见的是缺类，按报错补依赖：

| 报错 | 缺什么 |
| --- | --- |
| `androidx/startup/R$string` | AndroidX R 类（见 3.5） |
| `kotlin/collections/CollectionsKt` | kotlin-stdlib |
| `androidx/concurrent/futures/AbstractResolvableFuture` | concurrent-futures |
| `com/google/common/util/concurrent/ListenableFuture` | listenablefuture |

**注意最后两个**：缺它们不会立刻崩，而是**启动约 5 秒后**整个进程被 SIG:9 杀掉
（`profileinstaller` 的后台线程抛异常）。这个延迟很容易误判成别的问题。

### 3.9 载荷（native 层）分析

两个 `.so` 是优化后的 AArch64 机器码（7.6/8.4 MB），
**无法还原成原始 C++ 源码**。但可以还原出结构：

```bash
# 字符串提取
strings -n 6 libArkRe.so > strings.txt

# 按功能模块分类统计
grep -c "il2cpp_" strings.txt        # 251 个 il2cpp API
grep "ImGui" strings.txt             # UI 框架标识

# 节表 / 导出 / 导入
python tools/so_analysis.py
```

**卡密状态机定位**（用 capstone 反汇编）：

```bash
python tools/find_authvar.py         # 找授权状态变量偏移
python tools/disasm_authcheck.py     # 反汇编判定函数
```

关键发现：授权状态变量在 payload 的**模块内偏移**处，
`libArkRe.so` 是 `0x740568`，`libCherryTale.so` 是 `0x812210`
（两个不一样，因为是各自编译的）。

---

## 四、目录说明

```
restore/
├─ README.md              工程总览
├─ ARCHITECTURE.md        全量架构
├─ VERIFY.md              验收报告
├─ VERIFY-DIFF.md         对拍验证结果
├─ COMPILE-FIXES.md       ★ 编译错误的根因与修法（最实用）
│
├─ docs/                  6 份模块文档
│  ├─ module-host-business.md   业务层逐方法说明
│  ├─ module-algui.md           AlGui 框架 API
│  ├─ module-imgui-overlay.md   ImGui 覆盖层（触摸分发）
│  ├─ module-native-payload.md  载荷功能矩阵
│  ├─ module-injector.md        注入器行为
│  └─ module-resources.md       资源与清单
│
├─ host-apk/              ★ 可编译工程
│  ├─ build.gradle            Gradle 配置（参考，正式构建不用它）
│  ├─ src/main/java/          76 个 Java 文件
│  ├─ src/main/res/           自有资源（17 个）
│  └─ src/main/AndroidManifest.xml
│
├─ injected-dex/src/      ImGui 覆盖层的 Java 源码
├─ injector/src/          注入器 C 源码
├─ payload/INTERFACE.md   载荷接口契约
└─ tools/                 构建与还原脚本（28 个）
```

---

## 五、快速参考

```bash
# 完整构建
cd restore && python tools/build_apk.py

# 验证还原忠实度
python tools/verify_diff.py

# 重新生成 R.java
python tools/gen_rjava.py

# 看构建中间产物
ls host-apk/build-manual/
#   flat/       自有资源的 .flat
#   gen/        生成的 R.java
#   classes/    编译出的 .class
#   dex/        classes.dex
#   base.apk    复用原 APK 资源打的基础包
```
