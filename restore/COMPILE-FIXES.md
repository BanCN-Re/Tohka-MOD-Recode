# 编译修复记录

从 jadx 反编译产物到可运行 APK，一共修掉 **260 个编译错误**。
这份文档记录**每类错误的根因与修法**，全部可复现。

---

## 一、根因：jadx 的匿名类还原

**260 个错误里有 200+ 个来自同一个根因。**

### 问题现象

jadx 默认模式把匿名类内联到外层，但**会错误地给匿名类补构造参数**：

```java
// 源码本来是这样（合法）：
button.setOnClickListener(new View.OnClickListener() {
    @Override public void onClick(View v) { AlGui.this.clearMenu(); }
});

// jadx 还原成了（不合法 —— Java 不允许匿名类带构造参数）：
button.setOnClickListener(new View.OnClickListener(this) {
    private final AlGui this$0;
    { this.this$0 = this; }                      // ← this 是匿名类实例，
                                                 //   字段类型却是 AlGui，类型不兼容
    @Override public void onClick(View v) {
        this.this$0.clearMenu();
    }
});
```

编译报错形态：
- `anonymous class implements interface; cannot have arguments`（90 处）
- `incompatible types: <anonymous X> cannot be converted to AlGui`（80+ 处）
- `cannot find symbol: variable this$0`（46 处）

### 为什么会产生

jadx 在还原匿名类时，把「外层实例引用（`AlGui.this`）」误判成了
「匿名类的合成字段 `this$0`」—— 后者是 Java 编译器为**真内部类**生成的，
匿名类根本没有这个字段。

### 修法

**换 jadx 的运行参数**，让它别内联匿名类：

```bash
# 不要这样（会内联，产生 260 个错误）：
jadx -d out --no-res <apk>

# 要这样（把匿名类提升成成员类，代码合法）：
jadx -d out --no-res --no-inline-anonymous <apk>
```

`--no-inline-anonymous` 会把匿名类还原成**标准成员类**：

```java
class AlGui$100000017 implements View.OnClickListener {
    private final AlGui this$0;
    private final boolean val$isUnfold;

    AlGui$100000017(AlGui alGui, boolean z) {   // ← 构造函数正确接收 outer
        this.this$0 = alGui;                     // ← 类型匹配
        this.val$isUnfold = z;
    }
}
// 调用处： new AlGui$100000017(this, z)
```

**这一个参数把错误从 260 个降到 1 个。**

> **注意**：不要同时加 `--no-move-inner-classes`。
> 那个参数会让 jadx 用 `android.os.Build$VERSION` 这种 `$` 形式写 import，
> Java 源码里不合法（必须写 `Build.VERSION`），反而引入 230 个新错误。

---

## 二、剩余的独立错误（逐个修）

换参数后剩 1 个 + 后续暴露的若干，全部是 jadx 在复杂结构上的还原瑕疵。

### 2.1 jadx 丢变量声明

`SystemTool.java`：

```java
public static boolean inspectRootPermission() {
    new String[]{"/system/bin/", "/system/xbin/"};   // ← 缺变量名
    return false;
}
```

**修**：补上声明。原 smali 里是 `String[] xxx = ...`。

同类：`getRealScreenDP` 里 `r2.widthPixels`（`r2` 是 `DisplayMetrics`）、
`brightenColor` 里 `Color.colorToHSV(i, r0)`（`r0` 是 `float[3]`）。

### 2.2 枚举里重复定义 `valueOf`

`AlGuiData.java`：

```java
public enum AlguiView {
    ...
    public static AlguiView valueOf(String str) { ... }   // ← 与编译器自动生成的冲突
}
```

**修**：删掉手写的那个（枚举的 `valueOf` 由编译器自动生成）。

### 2.3 非 public 的类

`FloatContentView.java` 里 `import com.google.android.material.card.MaterialCardViewHelper;`
—— 该类在 material 1.11 里不是 public，只用于取一个常量
`DEFAULT_FADE_ANIM_DURATION`（值 150）。

**修**：内联成字面量 `150`，删掉 import。

### 2.4 已删除的 API

- `WebSettings.setAppCacheEnabled()` —— API 33 移除 → 删掉调用
- `androidx.constraintlayout.solver.widgets.analyzer.BasicMeasure` ——
  constraintlayout 2.x 移除该内部包 → 内联常量
- `android.R.style.Animation.Toast` —— 应为 `Animation_Toast`
  （jadx 把常量后缀还原成了嵌套类访问）

### 2.5 类型歧义

`FloatContentView.java` 同时 import 了 `android.os.Process` 又用了
`java.lang.Process`（`Runtime.exec()` 的返回类型）。

**修**：给后者加全限定名 `java.lang.Process`。

### 2.6 jadx 丢弃了 `return`

jadx 在复杂 switch 里会报：

```
/* JADX WARN: Code restructure failed: missing block: B:21:0x00b5, code lost:
   return false;
 */
```

它把这个 `return` 丢了，`boolean` 方法就没有返回语句。

**修**：在方法末尾补回 `return false;`（共 3 处：`AlGui.java` 1 处、
`AlGuiWindowView.java` 2 处）。

### 2.7 静态上下文里的非静态内部类

jadx 提升出来的成员类默认非 static，但从静态方法里 `new` 它们时，
Java 要求先有 outer 实例。

**修**：给不需要 outer 的成员类加 `static`（63 个）。
判据：构造函数不接收 outer 类型参数、类体内不出现 `Outer.this`、不出现 `this$0`。

### 2.8 逻辑还原错误

`AlGui.setBallImage()` —— jadx 标了 "Code decompiled incorrectly"，
条件写反了：

```java
if (decodeDrawable == null) {    // ← 成功解出图片时该走图片分支，不是 null 分支
    imageView.setBackground(decodeDrawable);
    ...
}
```

**修**：改成 `!= null`，并清理掉尾部 jadx 生成的垃圾代码
（`decodeDrawable = null; if (decodeDrawable == null) {}`）。

### 2.9 多变量类型推断失败

`AppTool.isRootEnabled()` / `getRootPermission()` ——
jadx 明确标注 `JADX WARN: Multi-variable type inference failed`，
生成出 `Process process = (DataOutputStream) 0;` 这种非法代码。

**修**：按语义重写这两个方法（保持行为等价，用 try/finally 正确释放资源）。

---

## 三、构建链上的工程问题

源码编译通过后，打包环节还有几个坑。

### 3.1 aapt2 的参数长度限制

`aapt2 link` 必须把所有资源 `.flat` 作为**命令行参数**传入。
AndroidX 有 2000+ 个资源，路径总长 **247,589 字符**，
远超 Windows 的 32,767 上限。而 aapt2 **不支持 `@argfile`**
（传了会打印帮助并失败）。

**解法**：**复用原 APK 里已编译好的资源**（`resources.arsc` + `res/`）。
反正我们的资源 ID 与原版一致，不用重新编译 AndroidX 的 2000+ 资源。

### 3.2 R.java 无法用 aapt2 link 生成

因为 `aapt2 link` 需要 AndroidX 的资源（`Theme.AppCompat` 等），
而我们没有。报 `resource style/Theme.AppCompat... not found`。

**解法**：从原 APK 的**资源表反向生成** R.java：

```bash
aapt2 dump resources <原APK>     # 输出 4975 个资源条目
```

解析这份输出，按包名/类型分组生成 `R.java`（含嵌套类）。
产出 389 KB，与复用的 `resources.arsc` 完全匹配。

> ⚠️ 类型名要**保持小写**（`R.layout` / `R.id`），
> 首字母大写会让源码里的 `R.layout.xxx` 找不到符号。

### 3.3 AndroidX 的 `R$*` 类缺失

AndroidX 每个库都有自己的 R 类（`androidx.startup.R$string`），
它们**不随 AAR 发布** —— 由 Gradle 构建时从 R.txt 生成。
缺了会在运行时崩：

```
NoClassDefFoundError: Failed resolution of: Landroidx/startup/R$string;
```

**解法**：jadx 反编译原 APK 时已经产出了这些类（356 个）。
把它们拷进工程 —— 但 jadx 产出的是**顶层类** `R$string`，
而源码引用的是**嵌套** `R.string`，两者在 Java 源码层面不等价。

所以需要合并：把每个包的 `R$Type.java` 收拢成一个 `R.java`，
每个 `$Type` 变成内部类（`tools/merge_r_classes.py`）。
`R$styleable` 特殊（字段是 `int[]`），单独处理。

### 3.4 d8 的类型重复

多个依赖 jar 里有同名类（如 kotlin-stdlib 的 1.8.22 与 1.9.0），
d8 报 `Type xxx is defined multiple times`。

**解法**：合并依赖 jar 时**按类路径去重**（不是按 jar 文件名），
同名类只保留先到的。

### 3.5 运行时缺 ListenableFuture

`androidx.profileinstaller` 的后台线程依赖：

```
androidx.concurrent.futures.AbstractResolvableFuture
  ← com.google.common.util.concurrent.ListenableFuture
```

缺任何一方都会让这个后台线程抛 `NoClassDefFoundError`，
**进而整个进程被 SIG:9 杀掉** —— 表现为「App 启动约 5 秒后突然消失」。

**解法**：把 `concurrent-futures-1.1.0.jar` 与
`listenablefuture-1.0.jar` 加进依赖。

### 3.6 manifest 必须是二进制 AXML

直接把文本 XML 写进 APK，`apksigner` 会报：

```
malformed binary resource: AndroidManifest
```

**解法**：用 `aapt2 link` 编译出二进制 manifest。

> ⚠️ `aapt2 compile -o <dir>` 的 `<dir>` **必须预先存在**，
> 否则 aapt2 会把它当成输出**文件名**而非目录。

### 3.7 sharedUserId 导致的安装失败

原版用了 `android:sharedUserId="com.Riruriru.Sx"` 与游戏共享 UID。
重建版走 root，不需要它，留着会导致：

```
INSTALL_FAILED_SHARED_USER_INCOMPATIBLE
Package com.Riruriru.Sx has no signatures that match those in shared user
```

**解法**：从 manifest 里去掉 `sharedUserId`。
（注意 `pm install -r` 会触发校验，而**全新安装不会** ——
所以调试时先 `pm uninstall`。）

---

## 四、最终结果

| 项 | 值 |
| --- | --- |
| 编译错误 | 260 → **0** |
| 生成 class | 478 个 |
| classes.dex | 7.22 MB |
| APK | **14.51 MB** |
| 安装 | ✅ 成功 |
| 运行 | ✅ 主界面正常显示，悬浮球出现，稳定运行 |

设备实测截图见仓库 [README](README.md)。

---

## 五、如果用 Gradle 构建

本仓库也提供 Gradle 工程（`host-apk/build.gradle`）。
理论上 Gradle 能自动处理 3.1~3.4 这些问题，但实测在本机环境下：

- AGP 8.2.0 对「反编译还原」的工程容忍度低
- javac 错误信息被截断到约 100 字符，难以定位
- 无法绕过 aapt2 的参数长度限制

**所以本项目的正式构建走 `tools/build_apk.py`（手工流程）**，
每一步的输入输出都可见，出错容易定位。

Gradle 工程保留作为参考。
