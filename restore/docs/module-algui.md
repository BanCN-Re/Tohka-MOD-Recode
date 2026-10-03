# AlGui 悬浮窗框架 — 可重构架构文档

> 分析对象：`restore/host-apk/src/irene-src/irene/window/algui/`
> 规模：**28 个 .java 文件，约 9,363 行**（其中 12 个在 `algui` 包根、4 个在 `CustomizeView`、12 个在 `Tools`）
> 来源：jadx 反编译自 `host-apk`（`Riruriru Mod 1.1.2`）的 `classes.dex`（原始包名 `irene.window.algui`）
> 约束：本文所有方法签名均由 `grep`/`Select-String` 从源码提取，未凭记忆书写。字节数为实测值。

---

## 1. 框架定位

### 1.1 它是什么

**AlGui 是一套可直接嵌入 Android 应用的「悬浮窗控件框架」，而非业务 MOD。**

它不包含任何游戏逻辑、内存读写或 IL2CPP 调用。它提供的全部能力是：

1. **两个系统级悬浮窗**——一个可拖动的圆形「悬浮球」，一个可拖动/可缩放的矩形「菜单窗」；
2. **一套声明式控件工厂**——`addSwitch` / `addSeekBarInt` / `addButton` / `addEditText` / `addCollapse` 等，一行调用即生成一个带圆角、描边、配色、字体、回调的成品控件；
3. **一套全局外观配置系统**——颜色与尺寸通过 `AlGuiData` 落到 `SharedPreferences`，配合预制菜单可让**用户自己改主题**；
4. **气泡通知、音效、对话框、准星覆盖层**等配套 UI 原语。

宿主 App 只需写一个 `Activity`，调用 `Loading.start(this)`，就得到一个完整可用的悬浮菜单骨架，然后在滚动列表里塞自己的控件即可。

### 1.2 给谁用 / 作者归属

| 证据 | 内容 |
| --- | --- |
| 包名 | `irene.window.algui` —— **irene** 是作者笔名，`algui` 是框架名 |
| `AlGui.java:413` 默认副标题 | 「作者：**艾琳**⼁仅供用于学习交流请勿用于违法用途⼁如果您有任何疑问请进游戏逆向交流群：**730967224**进行交流讨论」 |
| 预制菜单「使用说明」 | 「开发者：**艾琳 (唯一作者)**」「开发者QQ：**3353484607**」「当前版本：**v2.0 (正式版)**」「本项目由 麻省理工学院许可证 进行发布和分发」 |
| `AlGuiPrefabricatedMenu_LB.java` ×4 处 | 「Copyright © 2023 **艾琳** 版权所有」 |
| `BuildConfig.java` | `APPLICATION_ID = "irene.window.algui"`, `VERSION_NAME = "2.0"`, `VERSION_CODE = 2`, `BUILD_TYPE = "release"` |
| `R.java` | 独立资源包（`colorPrimary = 0x7f040000` 等），说明**它是一个可独立编译发布的 AAR/项目**，不是为某个宿主私有编写 |

结论：**AlGui 是作者「艾琳」独立开发的通用 Android 悬浮窗 UI 框架（v2.0，MIT 协议，2023 年）**，通过「使用文档 / 交流社区(Bug反馈) / 作者B站 / 赞助作者(加入开发) / 更新GUI」等预制入口对外分发。本项目里的 `host-apk` 只是**它的一个使用者**——一个用 AlGui 做外壳的注入器。

### 1.3 一个关键的架构事实：它并不真正依赖宿主

`irene.window.algui.MainActivity` 自己就是入口：

```java
// MainActivity.java —— irene 包自带的入口 Activity
public class MainActivity extends Activity {
    public static Context context;
    @Override protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(com.Riruriru.Sx.R.attr.actionBarDivider);  // ← 唯一的外部引用
        Loading.start(this);
    }
}
```

**整个 28 个类的框架对外部宿主只有一个引用点**：`MainActivity:14` 的 `setContentView(com.Riruriru.Sx.R.attr.actionBarDivider)`。
在 `host-apk` 中，打包者把 irene 的类塞进了 `com.Riruriru.Sx` 的资源命名空间，于是这里被重写成了宿主的 `R`。

> **重构提示**：换掉这一行（或改用 `irene.window.algui.R.layout.activity_main`）后，整个框架可脱离 `com.Riruriru.Sx` 独立运行。`AppTool.java:52` 用的是 `android.R.drawable.sym_def_app_icon`（系统资源），不构成宿主耦合。
>
> 反过来，`host-apk` 的 `com.Riruriru.Sx.*`（`MainActivity` / `FloatStartService` / `FloatServiceView` / `FloatControlView` 等 8 个类）**没有任何一处调用 `AlGui` 或 `Loading`**——宿主有自己独立的一套悬浮窗实现。二者是**并行共存的两套 UI**，irene 包是被整体搬运进来的第三方框架。

---

## 2. 核心类职责表

### 2.1 `irene.window.algui` 根包（12 个文件）

| 类 | 字节 / 行 | 一句话职责 |
| --- | --- | --- |
| **`AlGui`** | 157,926 B / 3,090 L | 框架主类：单例 `GUI(Context)`，持有悬浮球与菜单两个 `WindowManager`，提供全部控件工厂方法与 6 个回调接口 |
| `AlGuiPrefabricatedMenu_LB` | 96,442 B / 1,359 L | 预制菜单：把 `AlGui` 的原子控件组装成 4 个开箱即用的功能面板（说明/属性/菜单设置/准星） |
| `AlGuiData` | 47,301 B / 376 L | 数据模型与持久化：全部外观配置字段、4 组 `HashMap` 配置元数据、4 个 `SharedPreferences`、14 个 base64 图标、2 个枚举 |
| `AlGuiBubbleNotification` | 41,369 B / 795 L | 右上角气泡通知窗：单例 `Inform(Context)`，4 种语义 × 2 种风格 × 有/无按钮 = 16 个预设方法 |
| `AlGuiWindowView` | 36,518 B / 680 L | 独立悬浮视图工厂（静态方法）：霓虹渐变文字、普通文字、内嵌 WebView / 网页全屏窗口 |
| `Loading` | 26,917 B / 348 L | **使用范例与启动器**：`start(Context)` 一键跑通全流程，`initConfigurations()` + `initMenu()` 是最好的 API 用法参考 |
| `AlGuiSoundEffect` | 306,605 B / **110 L** | 音效播放：10 段 .ogg/.mp3 以 base64 硬编码在字段里，首次使用解到 `cacheDir` 再用 `MediaPlayer` 播放 |
| `AlGuiDialogBox` | 3,585 B / 69 L | 对话框封装：`showDiaLog` 任意视图弹窗、`showTextDiaLog` 标题+正文+单按钮 |
| `AlGuiPrefabricatedMenu_LB$$ExternalSyntheticBackport0` | 822 B / 25 L | D8 脱糖产物，提供 `String.join` 语义的 `m(CharSequence, Iterable)`；**重构时可删除** |
| `MainActivity` | 463 B / 17 L | 框架自带入口 Activity，`onCreate` 里调 `Loading.start(this)` |
| `R.java` | 1,013 B / 36 L | 框架自身的资源索引（attr/color/drawable/layout/string/style） |
| `BuildConfig.java` | 377 B / 10 L | `APPLICATION_ID="irene.window.algui"`, `VERSION_NAME="2.0"` |

### 2.2 `irene.window.algui.CustomizeView`（4 个类）

| 类 | 行 | 一句话职责 |
| --- | --- | --- |
| `vLinearLayout` | 88 | 带圆角/描边/背景色的 `LinearLayout`，`dispatchDraw` 用 `Path.clipPath` 裁圆角 |
| `vFrameLayout` | 88 | 同上，`FrameLayout` 版本（菜单根布局用它） |
| `MarqueeTextView` | 61 | 自动跑马灯 `TextView`，构造即建 `TranslateAnimation(2,1.0f → 2,-1.0f)`，`onAttachedToWindow` 重启 |
| `GradientTextView` | 108 | 动态线性渐变文字，`setColors(int...)` + `setDynamicEnabled(boolean)`，用于霓虹灯效果 |

### 2.3 `irene.window.algui.Tools`（**12 个**类，非任务描述中的 10 个）

| 类 | 行 | 一句话职责 |
| --- | --- | --- |
| `SystemTool` | 381 | 系统信息：SELinux/MIUI/HarmonyOS/内核/内存/存储/电量/分辨率/IP |
| `FileTool` | 366 | 文件工具：MIME、复制/删除/重命名/遍历、大小格式化、读写 txt、保存 `.plt` |
| `RealTimeDataTextTool` | 299 | 把 `TextView` 变成实时数据牌：时间/电量/FPS/内存/存储/SELinux/Root 检测 |
| `AppTool` | 278 | 应用与权限：联网检测、回桌面、通知栏、前台判断、root 检查与提权、包信息、服务/调试检测 |
| `ImageTool` | 269 | 图像：Bitmap↔Drawable、读 assets 图、base64→Bitmap、高斯模糊 |
| `AppPermissionTool` | 133 | 权限：批量申请、读 Manifest 权限表、悬浮窗权限检查与跳转设置 |
| `ViewTool` | 125 | 视图工具：dp/px 互转、颜色明暗/反色/分层、文字换行、阴影、渐变背景 |
| `VariousTools` | 95 | 杂项：剪贴板、开网页、加 QQ 群、**文本转语音(TTS)** |
| `RegularTool` | 54 | 正则校验：HTML/手机号/座机/邮箱/URL/中文/用户名/自定义匹配 |
| `HackerTool` | 39 | **执行 shell**：`Runtime.exec`，专门用于 `chmod 777` + 执行 native 库（框架里唯一的「危险」工具） |
| `MusicTool` | 30 | 播放 `assets` 里的 mp3（`AssetFileDescriptor` 方式） |
| `DataTool` | 34 | base64 编解码——**两个方法都是 `private`，实际不可用**，属反编译残留 |

---

## 3. `AlGui` 主类 API 面

### 3.1 单例与生命周期

```java
public static AlGui GUI(Context context)   // 单例入口；context 为 null 抛 IllegalArgumentException
AlGui(Context context)                     // 包级私有，构造即初始化两窗口+两视图树
```

**构造函数副作用（重构时必须保留顺序）**：

```
initBallWindow() → initBall() → initMenuWindow() → initMenu()
→ updateMenuAppearance() → setAllViewMargins(8,8,8,8) → setBallImage(null, 50, 50)
→ 校验 AndroidManifest 是否声明 SYSTEM_ALERT_WINDOW
   ├─ 未声明 → Toast：「开发者未在AndroidManifest.xml文件中声明悬浮窗权限…」
   ├─ 已声明但未授权 → Toast：「请授予悬浮窗权限，才能显示窗口」(API<26 另有文案)
   └─ 已授权 → 正常返回
```

> **重构要点**：构造函数**不显示任何窗口**，只建好 View 树并检查权限。窗口显示必须显式调 `showBall()` / `showMenu()`。`setBallImage(null, ...)` 是必须的收尾（它会 `removeAllViews` 重建悬浮球内容）。

### 3.2 窗口管理（6 个方法）

两个窗口对称设计，各有 `show / update / clear` 三件套：

```java
public void showBall()      // 权限已授予且 !isBallView 时 ballManager.addView(ballLayout, ballParams)
public void updateBall()    // isBallView 时 updateViewLayout
public void clearBall()     // isBallView 时 removeView

public void showMenu()      // 同上，作用于 menuManager / menuLayout
public void updateMenu()
public void clearMenu()
```

**窗口参数（`initBallWindow` / `initMenuWindow`）**：

| 项 | 悬浮球 | 菜单窗 |
| --- | --- | --- |
| `flags` | `getLiveStreamFlags() \| 0x1000000 \| 0x8` | `getLiveStreamFlags() \| 0x1000000 \| 0x20 \| 0x40000` |
| `gravity` | `51`（左上） | `51`（左上） |
| `width/height` | `-2 / -2`（WRAP_CONTENT） | `-2 / -2`，`updateMenuAppearance` 里再次置 `-2/-2` |
| `type` | `SDK≥26 ? 2038 : 2003` | 同 |
| `format` | `1`（TRANSLUCENT） | `1` |
| `windowAnimations` | `android.R.style.Animation.Toast` | 同 |

> `2038 = TYPE_APPLICATION_OVERLAY`，`2003 = TYPE_PHONE`，跨 Android 8.0 的兼容分支。

### 3.3 视图访问器（27 个 getter，`AlGui.java:142–251`）

这是宿主定制界面的主要手段——拿到内部 View 直接改属性。

```java
public Context getContext()
public AlGuiPrefabricatedMenu_LB PrefabricatedMenu()   // 懒加载预制菜单，首次调用才 new

// 悬浮球
public WindowManager getBallWindow()
public WindowManager.LayoutParams getBallWindowParams()
public vLinearLayout getBallLayout()

// 菜单窗
public WindowManager getMenuWindow()
public WindowManager.LayoutParams getMenuWindowParams()
public vFrameLayout getMenuRootLayout()                 // menuLayout
public vLinearLayout getMenuMainLayout()                // mainLayout（纵向三段）
public vLinearLayout getMenuTopLayout()                 // lineTouchLayout
public vLinearLayout getMenuTopLine()                   // touchMoveLine（顶部拖拽条）
public vLinearLayout getMenuTitleLiveStreamRootLayout() // 标题+直播图标行
public vLinearLayout getMenuTitleLayout()
public TextView getMenuMainTitle()                      // title
public TextView getMenuSubTitle()                       // edition
public vLinearLayout getMenuLiveStreamLayout()
public ImageView getMenuLiveStreamIcon()
public vLinearLayout getMenuExplanationLayout()         // subTitleLayout
public TextView getMenuExplanation()                    // subTitle（跑马灯说明）
public ScrollView getMenuScrollingList()                // scroll ← 宿主塞控件的地方
public vLinearLayout getMenuScrollingListLayout()       // scrollLayout ← 实际上塞这里
public RelativeLayout getMenuBottomLayout()
public Button getMenuBottomLeftButton()                 // 「隐藏/退出」
public Button getMenuBottomRightButton()                // 「最小化」
public View getMenuBottomRightTriangleView()            // 右下角缩放三角
public Path getMenuBottomRightTrianglePath()
public Paint getMenuBottomRightTrianglePaint()
```

> **陷阱**：`getMenuSubTitle()` 返回的是字段 `edition`（副标题/版本号），而 `getMenuExplanation()` 返回的是 `subTitle`（说明文字）。命名与字段名错位，重构时建议直接改名 `getMenuEdition()` / `getMenuSubTitle()`。

### 3.4 外观与全局配置（3 个方法）

```java
public Drawable setBallImage(String str, float f, float f2)
```
从 **assets** 读图片作为悬浮球图标，`f/f2` 为宽高（dp）。支持 GIF：`ImageDecoder.decodeDrawable` 返回 `AnimatedImageDrawable` 时自动播放。传 `null` 时走兜底分支（清空 child 后按传入尺寸建默认内容）。**会先 `removeAllViews()`**。

```java
public void setAllViewMargins(int i, int i2, int i3, int i4)
```
设置全局外边距 `int[]{left, top, right, bottom}`，存进 `viewWBJ` 字段。**所有 `addXxx` 工厂方法都会读取 `this.viewWBJ` 设置 `setMargins`**，所以这是「一键统一间距」的开关。

```java
public void updateMenuAppearance()
```
**一次调用把 `AlGuiData` + `SharedPreferences` 的全部配置刷到实景上**。逐项对应关系（`AlGui.java:344–364`）：

| 目标 | 配置键（中文 key） | 类型 |
| --- | --- | --- |
| `menuLayout` 圆角 | 根布局圆角半径 | float |
| `menuLayout` 描边 | 根布局描边宽度 / 根布局描边颜色 | float / int |
| `menuLayout` 背景 | 根布局背景颜色 | int |
| `menuLayout` 透明度 | 菜单透明度 | float |
| `touchMoveLine` 背景/圆角 | 菜单顶部线条颜色 / 菜单顶部线条圆角半径 | int / float |
| `title` / `edition` 文字色 | 菜单主标题文本颜色 / 菜单副标题文本颜色 | int |
| `liveStreamIcon` 着色 | 菜单直播模式图标颜色 | int |
| `subTitleLayout` / `subTitle` | 菜单说明背景颜色 / 菜单说明文本颜色 | int |
| `scroll` 背景/宽/高 | 菜单滚动列表背景颜色 / …宽度 / …高度 | int / float / float |
| `leftButton` / `rightButton` 文字色 | 菜单左下角按钮文本颜色 / 菜单右下角按钮文本颜色 | int |
| `trianglePaint` 颜色 | 菜单右下角三角形颜色 | int |

末尾会 `zoomTriangleView.invalidate()`、重置 `menuParams` 尺寸并调 `updateMenu()`。

### 3.5 控件工厂（32 个重载，覆盖 14 类控件，全部 `public`）

**统一约定**：
- 每个控件都有 **两个重载**：带首参 `ViewGroup viewGroup`（自动 `addView` 到父容器）与不带（只返回，由调用方自己 `addView`）。
- 参数中反复出现的 `Typeface typeface` 传 `null` 即用默认字体。
- 颜色参数是 `int`（ARGB），尺寸参数是 `float`（单位 **dp**，内部经 `ViewTool.convertDpToPx` 转换）。
- 创建的控件都会 `setId(AlGuiData.AlguiView.XXX.getId())`，这是**用枚举 ID 做类型标记**，供气泡通知等模块 `findViewById` 反查。
- 所有控件都会用 `viewWBJ` 设 margins。

| 工厂 | 返回类型 | 重载数 | 说明 |
| --- | --- | --- | --- |
| `addCollapse` | `LinearLayout` / `vLinearLayout` | 2 | 折叠菜单；带 `View... viewArr` 的重载把子视图放进折叠区 |
| `addLinearLayout` | `LinearLayout` | 2 | 纯容器，参数 `(orientation, gravity, width, height, View...)` |
| `addSwitch` | `Switch` / `LinearLayout` | 2 | 开关+说明文字；含 4 个颜色（圆开/圆关/轨开/轨关） |
| `addSmallButton` | `vLinearLayout` | 2 | 小按钮（WRAP_CONTENT） |
| `addButton` | `vLinearLayout` | 2 | 大按钮（可指定 width/height） |
| `addEditText` | `EditText` / `LinearLayout` | 4 | 输入框；带按钮的重载会在右侧生成「应用」小按钮 |
| `addSeekBarFloat` | `SeekBar` / `LinearLayout` | 2 | 小数拖动条，**内部 ×10 取整**，回调里 ÷10 还原 |
| `addSeekBarInt` | `SeekBar` / `LinearLayout` | 2 | 整数拖动条 |
| `addCheckBox` | `CheckBox` | 2 | 复选框 |
| `addWebView` | `WebView` | 4 | 内嵌 HTML（`loadData`）；2 个重载可指定宽高背景 |
| `addWebSite` | `WebView` | 2 | 内嵌网址（`loadUrl`） |
| `addTextView` | `TextView` | 2 | 普通文本，`LinkMovementMethod` 支持 HTML 链接 |
| `addMarqueeTextView` | `MarqueeTextView` / `HorizontalScrollView` | 2 | 跑马灯文本，包在 `HorizontalScrollView` 里 |
| `addLine` | `View` | 2 | 分隔线；`boolean z` = true 为水平线，false 为垂直线 |

**完整签名**（可直接照抄重构）：

```java
public LinearLayout addCollapse(ViewGroup viewGroup, CharSequence charSequence, int i, int i2,
        Typeface typeface, float f, int i3, float f2, int i4, boolean z)
public vLinearLayout addCollapse(CharSequence charSequence, int i, int i2, Typeface typeface,
        float f, int i3, float f2, int i4, boolean z, View... viewArr)
public LinearLayout addLinearLayout(ViewGroup viewGroup, int i, int i2, int i3, int i4)
public LinearLayout addLinearLayout(int i, int i2, int i3, int i4, View... viewArr)
public Switch addSwitch(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface,
        CharSequence charSequence2, float f2, int i2, Typeface typeface2,
        int i3, int i4, int i5, int i6, T_SwitchOnChangeListener l)
public LinearLayout addSwitch(CharSequence charSequence, float f, int i, Typeface typeface,
        CharSequence charSequence2, float f2, int i2, Typeface typeface2,
        int i3, int i4, int i5, int i6, T_SwitchOnChangeListener l)
public boolean setSwitchColor(Switch r3, int i, int i2)   // 圆/轨颜色，PorterDuff.MULTIPLY
public vLinearLayout addSmallButton(ViewGroup viewGroup, CharSequence charSequence, float f, int i,
        Typeface typeface, float f2, int i2, float f3, int i3, T_ButtonOnChangeListener l)
public vLinearLayout addSmallButton(CharSequence charSequence, float f, int i, Typeface typeface,
        float f2, int i2, float f3, int i3, T_ButtonOnChangeListener l)
public vLinearLayout addButton(ViewGroup viewGroup, CharSequence charSequence, float f, int i,
        Typeface typeface, float f2, int i2, float f3, int i3, int i4, int i5, T_ButtonOnChangeListener l)
public vLinearLayout addButton(CharSequence charSequence, float f, int i, Typeface typeface,
        float f2, int i2, float f3, int i3, int i4, int i5, T_ButtonOnChangeListener l)
public EditText addEditText(ViewGroup viewGroup, float f, Typeface typeface, int i,
        CharSequence charSequence, int i2, CharSequence charSequence2,
        float f2, int i3, float f3, int i4, T_EditTextOnChangeListener l)
public EditText addEditText(float f, Typeface typeface, int i, CharSequence charSequence, int i2,
        CharSequence charSequence2, float f2, int i3, float f3, int i4, T_EditTextOnChangeListener l)
public EditText addEditText(ViewGroup viewGroup, float f, Typeface typeface, int i, CharSequence charSequence,
        int i2, CharSequence charSequence2, float f2, int i3, float f3, int i4, int i5,
        CharSequence charSequence3, float f4, int i6, float f5, int i7, T_EditTextOnChangeListener l)
public LinearLayout addEditText(float f, Typeface typeface, int i, CharSequence charSequence, int i2,
        CharSequence charSequence2, float f2, int i3, float f3, int i4, int i5,
        CharSequence charSequence3, float f4, int i6, float f5, int i7, T_EditTextOnChangeListener l)
public SeekBar addSeekBarFloat(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface,
        float f2, float f3, float f4, int i2, int i3, int i4, T_SeekBarFloatOnChangeListener l)
public LinearLayout addSeekBarFloat(CharSequence charSequence, float f, int i, Typeface typeface,
        float f2, float f3, float f4, int i2, int i3, int i4, T_SeekBarFloatOnChangeListener l)
public SeekBar addSeekBarInt(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface,
        int i2, int i3, int i4, int i5, int i6, int i7, T_SeekBarIntOnChangeListener l)
public LinearLayout addSeekBarInt(CharSequence charSequence, float f, int i, Typeface typeface,
        int i2, int i3, int i4, int i5, int i6, int i7, T_SeekBarIntOnChangeListener l)
public CheckBox addCheckBox(ViewGroup viewGroup, CharSequence charSequence, float f, int i,
        Typeface typeface, int i2, T_CheckBoxOnChangeListener l)
public CheckBox addCheckBox(CharSequence charSequence, float f, int i, Typeface typeface,
        int i2, T_CheckBoxOnChangeListener l)
public WebView addWebView(ViewGroup viewGroup, String str)
public WebView addWebView(String str)
public WebView addWebView(ViewGroup viewGroup, String str, float f, float f2, int i)
public WebView addWebView(String str, float f, float f2, int i)
public WebView addWebSite(ViewGroup viewGroup, String str)
public WebView addWebSite(String str)
public TextView addTextView(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface)
public TextView addTextView(CharSequence charSequence, float f, int i, Typeface typeface)
public MarqueeTextView addMarqueeTextView(ViewGroup viewGroup, int i, CharSequence charSequence, float f,
        int i2, Typeface typeface, long j, int i3, boolean z)
public HorizontalScrollView addMarqueeTextView(int i, CharSequence charSequence, float f, int i2,
        Typeface typeface, long j, int i3, boolean z)
public View addLine(ViewGroup viewGroup, float f, int i, boolean z)
public View addLine(float f, int i, boolean z)
```

**行为细节（重构时别漏）**：

- **`addCollapse` 返回值不一致**：带 `ViewGroup` 的重载返回**折叠内容区** `LinearLayout`（往里 `addView` 即可），不带的重载返回**外层** `vLinearLayout`。`Loading.java:164–166` 同时用到了两种语义。
- **折叠箭头**：展开 `▼`（11dp），折叠 `▶`（8dp）；点击时播放 `AlGuiSoundEffect.COLLAPSEMENU_OPEN/SHUT`。
- **`addButton` 的 `T_ButtonOnChangeListener.onClick` 第四参 `boolean z` 是自翻转的 toggle 状态**，每次点击取反（`AnonymousClass100000026.isChecked`），可用于做「按下/弹起」双态按钮。回调内可 `gradientDrawable.setColor(...)` 改色。
- **`addEditText` 长按弹 `PopupMenu`**：「全选(95) / 复制(96) / 粘贴(97) / 剪切(98)」，用 `ClipboardManager` 实现；`setInputType(131072 /* TYPE_TEXT_FLAG_MULTI_LINE */)`。
- **`addSeekBarFloat` 的定点缩放**：`max = (int)(maxFloat*10)`，`min = (int)(minFloat*10)`，回调里 `f9 = i7 / 10.0f`。**最小步进 0.1**。
- **`addSeekBarInt/Float` 标签格式**：`"•" + charSequence + "：" + progress`。
- **`addLine` 参数顺序**：`addLine(float 粗细dp, int 颜色, boolean 是否水平)`。

### 3.6 事件回调接口（6 个 `public interface`，`AlGui.java:99–140`）

```java
public interface T_ButtonOnChangeListener {
    void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z);
}

public interface T_CheckBoxOnChangeListener {
    void onClick(CompoundButton compoundButton, boolean z);
}

public interface T_EditTextOnChangeListener {
    void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3);
    void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3);
    void afterTextChanged(EditText editText, Editable editable);
    void buttonOnClick(EditText editText, View view, TextView textView, boolean z);   // 「应用」按钮
}

public interface T_SeekBarFloatOnChangeListener {
    void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z);
    void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f);
    void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f);
}

public interface T_SeekBarIntOnChangeListener {
    void onProgressChanged(TextView textView, SeekBar seekBar, int i, boolean z);
    void onStartTrackingTouch(TextView textView, SeekBar seekBar, int i);
    void onStopTrackingTouch(TextView textView, SeekBar seekBar, int i);
}

public interface T_SwitchOnChangeListener {
    void onClick(CompoundButton compoundButton, TextView textView, boolean z);
}
```

> 命名规律：`T_` 前缀 = Type/Listener，是作者自定的风格。回调普遍把「关联的 UI 组件」一起回传（如 seekbar 把它的标签 `TextView` 传回），方便调用方直接改显示，无需闭包捕获。

### 3.7 内部交互实现（私有，不计入 API 面）

| 机制 | 位置 | 行为 |
| --- | --- | --- |
| 悬浮球拖动 | `initBall()` 的 `OnTouchListener` | `moveThreshold=20`；未超阈值抬手 → `showMenu() + clearBall()`；拖动时 `alpha=0.3f`，抬手恢复 `1.0f` |
| 菜单整体拖动 | `initMenu()` 的 `menuLayout.setOnTouchListener` | `moveThreshold=20`，直接改 `menuParams.x/y` + `updateMenu()` |
| 菜单缩放 | `ResizeViewTouchListener`（私有内部类） | 绑在右下三角；`moveThreshold=10`；拖动时把 `scroll` 宽高加大（下限 350 / 0），抬手写回 `SharedPreferences` 的「菜单滚动列表宽度/高度键名」 |
| 右下三角 | `zoomTriangleView` 匿名 `View.onDraw` | 画一个 19dp 高的直角三角形，颜色取 `menuBottRightTriangleColor` |
| 隐藏悬浮球 | `leftButton` 单击 | 弹 `AlGuiDialogBox` 确认框；确认后 `hideBall()`，`CountDownTimer(5000, 1000)` 高亮 5 秒提示位置 |
| 退出悬浮窗 | `leftButton` 长按 | 弹确认框；确认后 `AlGuiSoundEffect` + `clearMenu()` + `clearBall()` + 通知 |
| 直播模式 | `liveStreamIcon` 点击 | 切 `AlGuiData.setIsLiveStream()`，改三个窗口的 `flags` 加 `0x2000 (FLAG_SECURE)`，使录屏/直播看不到窗口 |
| 最小化 | `rightButton` | `clearMenu(); showBall();` |

---

## 4. 数据模型：`AlGuiData`

### 4.1 外观配置字段（**全部 `public static`，18 项可配 + 1 项缓存**）

这是全局可变状态，任何地方都能直接赋值。

```java
// —— 颜色（int, ARGB） ——
public static int rootLayoutBackColor        = -13619152;  // 0xFF303030 根布局背景
public static int rootLayoutStrokeColor      = -12434878;  // 0xFF424242 根布局描边
public static int menuTopLineColor           = -4342339;   // 0xFFBDBDBD 顶部拖拽条
public static int menuMainTitleTextColor     = -1;         // 0xFFFFFFFF 主标题
public static int menuSubTitleTextColor      = 1627389951; // 0x60FFFFFF 半透明白（副标题）
public static int menuLiveStreamIconColor    = -4342339;   // 直播图标着色
public static int menuExplanationBackColor   = 1627389951; // 说明区背景
public static int menuExplanationTextColor   = -1;         // 说明文字
public static int menuScrollBackColor        = -14606047;  // 0xFF212121 滚动列表背景
public static int menuBottLeftButtonTextColor  = -1;
public static int menuBottRightButtonTextColor = -1;
public static int menuBottRightTriangleColor   = -12434878;
```

> 配色沿用 **Material Design 调色板**（实测十进制→十六进制换算）：`-12627531 = 0xFF3F51B5` Indigo 500、`-11234587 = 0xFF5492E5`、`-11751600 = 0xFF4CAF50` Green 500、`-10044566 = 0xFF66BB6A` Green 400、`-1092784 = 0xFFEF5350` Red 400、`-769226 = 0xFFF44336` Red 500、`-2039584 = 0xFFE0E0E0` Grey 300、`-6381922 = 0xFF9E9E9E` Grey 500、`-3814679 = 0xFFC5CAE9` Indigo 100。

```java
// —— 尺寸（float, dp） ——
public static float rootLayoutFilletRadius = 10.9f;   // 根布局圆角
public static float rootLayoutStrokeWidth  = 0.4f;    // 根布局描边宽
public static float menuTopLineFilletRadius= 20.0f;   // 顶部条圆角
public static float menuTransparency       = 1.0f;    // 菜单整体透明度
public static float menuScrollWidth        = 809.0f;  // 滚动列表宽（像素，由缩放逻辑写回）
public static float menuScrollHeight       = 554.0f;  // 滚动列表高

// —— 直播模式 ——
public static int   liveStreamFlags;                  // 计算结果缓存
private static boolean isLiveStream = false;
```

### 4.2 枚举（2 个，作控件类型 ID 用）

```java
public enum AlguiView {          // 每个控件创建时 set 的 id
    Collapse(99), Switch(100), SmallButton(101), Button(102), EditText(103),
    SeekBarInt(104), SeekBarFloat(105), CheckBox(106), WebView(107),
    TextView(108), MarqueeTextView(109), Line(110), WebSite(111);
    public int getId();
    public static AlguiView valueOf(String str);
}

public enum AlguiNotification {  // 气泡通知根布局 id
    MessageNotification(1000), ButtonNotification(2001);
    public int getId();
    public static AlguiNotification valueOf(String str);
}
```

> 用途：`AlGuiBubbleNotification:589` 用 `findViewById(AlguiNotification.ButtonNotification.getId())` 判断当前气泡是否带按钮，从而决定窗口 `flags`（带按钮时不设 `FLAG_NOT_TOUCHABLE`）。这是**省掉自定义 View 类**的轻量类型标记法。

### 4.3 四组配置元数据（`HashMap<String, Object>`，中文键）

作者的设计模式：**用中文键名做「人类可读的标签」，再映射到 SharedPreferences 的实际键**。每项都是「…键名」+「…默认数据」成对出现。

| HashMap | 项数 | 前缀 | 对应 SharedPreferences |
| --- | --- | --- | --- |
| `MenuColorData` | 22（11 项 ×2） | 根布局背景颜色 / 描边颜色 / 菜单顶部线条颜色 / 主标题文本颜色 / 副标题文本颜色 / 直播模式图标颜色 / 说明背景颜色 / 说明文本颜色 / 滚动列表背景颜色 / 左下角按钮文本颜色 / 右下角按钮文本颜色 / 右下角三角形颜色 | `"MenuColorData"` |
| `MenuAttributeData` | 12（6 项 ×2） | 根布局圆角半径 / 根布局描边宽度 / 菜单顶部线条圆角半径 / 菜单透明度 / 菜单滚动列表宽度 / 菜单滚动列表高度 | `"MenuAttributeData"` |
| `DiaLogFlagData` | 4（2 项 ×2） | 悬浮窗隐藏弹窗不再提示（`hideMenuDiaLogBZTS`）/ 悬浮窗退出弹窗不再提示（`exitMenuDiaLogBZTS`），默认 `false` | `"DiaLogFlagData"` |
| `GameFrontSightData` | 12（6 项 ×2） | 游戏准星样式（默认 `"╋"`）/ 颜色（默认 `-11751600`）/ 大小（默认 `15.0f`）/ 透明度（默认 `1.0f`）/ X偏移（默认 `0`）/ Y偏移（默认 `0`） | `"GameFrontSightData"` |

### 4.4 持久化 API（8 组 getter）

固定模式：`getXxxSP(Context)` 懒加载 `SharedPreferences`；`getXxxSPED(Context)` 懒加载 `.edit()`；`getXxxData()` 返回 HashMap 元数据。`context == null` 时返回 `null`。

```java
public static SharedPreferences        getMenuColorSP(Context)      / getMenuColorSPED(Context)      / HashMap getMenuColorData()
public static SharedPreferences        getMenuAttributeSP(Context)  / getMenuAttributeSPED(Context)  / HashMap getMenuAttributeData()
public static SharedPreferences        getDiaLogFlagSP(Context)     / getDiaLogFlagSPED(Context)     / HashMap getDiaLogFlagData()
public static SharedPreferences        getGameFrontSightSP(Context) / getGameFrontSightSPED(Context) / HashMap getGameFrontSightData()
```

**读取范例**（`AlGui.java:344`）：
```java
sp.getFloat((String) getMenuAttributeData().get("根布局圆角半径键名"),
            ((Float) getMenuAttributeData().get("根布局圆角半径默认数据")).floatValue())
```

**写入范例**（`AlGuiPrefabricatedMenu_LB.java:383`）：
```java
getMenuColorSPED(ctx).putInt((String) getMenuColorData().get("根布局背景颜色键名"), parseColor);
getMenuColorSPED(ctx).apply();
```

> **重构警示**：这套设计极其冗长（每个配置项要写 4 次 `HashMap.get` + 强制转换）。重构时建议替换为**类型安全的配置类 + 注解/枚举**，但**必须保持中文键字符串和 4 个 SharedPreferences 文件名不变**，否则用户已保存的配置会丢失。

### 4.5 内置 base64 资源（14 个静态字段）

**这是 `AlGuiData` 47 KB 体积的主要来源**——所有图标以 base64 PNG 字符串硬编码，运行时经 `ImageTool.getBase64Image()` 解码。

| getter | 用途 |
| --- | --- |
| `getVideo_Icon_LiveStart()` / `getVideo_Icon_LiveEnd()` | 菜单右上角直播模式图标（开/关两态，`initMenu` 用 LiveEnd 作初始） |
| `getExquisiteNotice_Icon_Message/Success/Mistake/Alert()` | **精致风格**气泡通知的 4 个图标 |
| `getSimplicityNotice_Icon_Message/Success/Mistake/Alert()` | **简约风格**气泡通知的 4 个图标 |
| `getOther_Icon_Hacker1()` / `getOther_Icon_Hacker2()` | 通用图标（Hacker2 用作通知栏 largeIcon） |

### 4.6 直播模式实现（关键业务逻辑）

```java
public static boolean getIsLiveStream()
public static int     getLiveStreamFlags()      // 开=16781312 (0x1001000) / 关=16777216 (0x1000000)
public static void    setIsLiveStream(Context context, boolean z)
```

> 差值经核算为 `16781312 - 16777216 = 4096 = 0x1000`。**`0x1000` 并非 `FLAG_SECURE`（`FLAG_SECURE` 是 `0x2000`）**，而是一个未在公开 `WindowManager.LayoutParams` 常量表中出现的位。原 APK 中 irene 包的 smali 未随工程保留，故无法从字节码二次确认。功能上该位使窗口在录屏/直播中不可见，但**具体语义待验证**——重构时若需精确等价，应实测该位的观感差异，或直接改用 `0x2000 (FLAG_SECURE)`。

`setIsLiveStream` 会**同步改三个窗口的 flags 并立即刷新**：

| 窗口 | 开启（直播中） | 关闭 |
| --- | --- | --- |
| 悬浮球 | `getLiveStreamFlags() \| 0x1000000 \| 0x8` | `16777224` |
| 菜单窗 | `getLiveStreamFlags() \| 0x1000000 \| 0x20 \| 0x40000` | `android.R.string.config_feedbackIntentNameKey`（= `0x1000028`，反编译把常量还原成了资源 ID） |
| 气泡通知 | `getLiveStreamFlags() \| 0x1000000 \| 0x8` | `16777224` |

差值 `0x1000`（**注意：不是 `FLAG_SECURE` 的 `0x2000`**，见 4.6 节说明），使窗口内容在截图/录屏/直播中不可见（「只有在现实中的你自己可见」）。所有 `AlGuiWindowView` 静态窗也统一用 `getLiveStreamFlags()` 起手，保证一致。

---

## 5. 菜单系统：`AlGuiPrefabricatedMenu_LB`

### 5.1 定位

`AlGui` 提供的是**原子控件**；`AlGuiPrefabricatedMenu_LB` 提供的是**组装好的成品面板**。命名后缀 `_LB` 含义未在代码中出现（推测为作者内部版本标识）。

```java
public AlGuiPrefabricatedMenu_LB(Context context, AlGui alGui)   // 构造为 protected，由 AlGui.PrefabricatedMenu() 懒加载
```

### 5.2 4 个预制面板（`public`，参数统一为 `ViewGroup`，传 `null` 即不自动挂载）

#### ① `addExplanation(ViewGroup)` — 「使用说明」（L58）

折叠菜单，`isUnfold=true`（默认展开）。内容：项目 MIT 许可证、开发者「艾琳 (唯一作者)」、开发者 QQ `3353484607`、交流群 `730967224`、当前版本 `v2.0 (正式版)`、底部 `Copyright © 2023 艾琳 版权所有`。

按钮（`T_ButtonOnChangeListener`）：
- **使用文档** → `VariousTools.gotoWeb(...)`
- **更新GUI** → `VariousTools.gotoWeb(ctx, "https://www.123pan.com/s/RMOtVv-G8ijh.html")`
- **作者B站** → 打开 B 站主页
- **赞助作者(加入开发)** → 打开赞助页
- **交流社区(Bug反馈)** → `VariousTools.joinQQGroup(...)`
- 另有一处"打开方式"选择：`AlGuiBubbleNotification.Inform(...).showMessageNotification_Simplicity_Button(null, "请选择打开方式", "你希望在内部直接打开，还是在外部游览器打开", "内部", ...)` — 分「内部 WebView」与「外部浏览器」两条路径，WebView 加载失败时提示「内部无法加载 / 可能没有网络权限，我们将自动跳转到第三方游览器加载！」（`showMistakeNotification_Simplicity`）

#### ② `addAttributeStatusMenu(ViewGroup)` — 「属性状态」（L237）

折叠菜单，`isUnfold=false`。三段式信息面板，**全部只读**：

| 分区 | 内容 |
| --- | --- |
| 系统状态实时监测 | 当前时间、实时电量、实时帧率、总共/可用内存、总共/可用存储（用 `RealTimeDataTextTool` 的实时绑定版本） |
| 设备信息 | `Build.BRAND` / `MODEL` / `CPU_ABI`、分辨率、真实 DP、系统版本（`SystemTool.isHarmonyOs()` 分支显示「HarmonyOs [鸿蒙系统]」或「Android [安卓系统]」）、内核版本、IP 地址、Root 权限、调试模式 |
| 应用信息 | 包名、版本号、入口 Activity、签名、UID、系统应用判定、同名 UID 应用 |
| 环境检测机制 | SELinux 模式（`textAddSELinuxMode()` 私有方法，起后台线程每秒刷新一次，L194） |

#### ③ `addAppearanceSettingsMenu(ViewGroup)` — 「菜单设置」（L337，**最长，约 700 行**）

这是**框架的自配置界面**，也是 `AlGuiData` 全部配置项的可视化编辑器。结构：

**A. 全局颜色设置**（11 个 `addEditText`，每个右侧带「应用」按钮）

输入框 hint 依次为：菜单上下边栏背景颜色、菜单说明布局背景颜色、菜单滚动列表背景颜色、菜单描边边框绘制颜色、菜单右下三角绘制颜色、菜单顶部线条绘制颜色、菜单主要标题文本颜色、菜单小副标题文本颜色、菜单说明内容文本颜色、菜单左下按钮文本颜色、菜单右下按钮文本颜色、菜单直播模式图标颜色。

- **颜色解析**：`"0x"` 开头且长度 10 → `Long.parseLong(sub, 16)`；否则 `Color.parseColor(str)`。
- **输入过滤**：`afterTextChanged` 里 `editable.toString().replaceAll("[^a-zA-Z0-9#]", "")`，只允许字母数字和 `#`。
- **成功** → `showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000)`，并写 `SharedPreferences`；
- **格式错** → `showMistakeNotification_Simplicity(..., "应用失败", "无效的颜色值", ...)`；
- **空输入** → `showAlertNotification_Simplicity(..., "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", ...)`；
- **恢复默认颜色** 按钮 → `getMenuColorSPED(ctx).clear(); apply();` + `"已恢复所有颜色并清除保存的颜色配置"`。

**B. 全局属性设置**（4 个 `addSeekBarFloat`）

| 拖动条标签 | 关联配置键 | 范围/步进 |
| --- | --- | --- |
| 菜单圆角半径 | 根布局圆角半径 | float，步进 0.1 |
| 菜单描边宽度 | 根布局描边宽度 | float |
| 顶部线条圆角 | 菜单顶部线条圆角半径 | float |
| 悬浮窗透明度 | 菜单透明度 | 0.1 ~ 1.0 |

- **恢复默认属性** 按钮 → `getMenuAttributeSPED(ctx).clear(); apply();` + `"已恢复所有属性并清除保存的属性配置"`。

#### ④ `addGameFrontSightMenu(ViewGroup)` — 「游戏准星」（L1049）

一个**独立的全屏覆盖层**（不挂在菜单里）：

```java
public boolean initFrontSight()      // 建 WindowManager + TextView，flags=1336，gravity=17(居中)，type=2038/2003
public void updateFrontSightCF()     // 从 SP 读样式/颜色/大小/透明度/X偏移/Y偏移 刷到 TextView + wParams
```

> `initFrontSight` / `updateFrontSightCF` 在 jadx 输出中标注为 `public`，但上方带有 `/* JADX INFO: Access modifiers changed from: private */` 注释——**原字节码中它们是 `private`**。重构时按 `private` 处理更贴近原设计（仅由 `addGameFrontSightMenu` 内部调用）。

控件：
| 类型 | 标签 | 行为 |
| --- | --- | --- |
| `addSwitch` | 游戏准星 | 开：惰性 `initFrontSight()`（失败则 `setChecked(false)` + 「准星初始化失败 / 可能没有悬浮窗权限或空指针异常」），成功则 `setVisibility(0)`；关：`setVisibility(8)` |
| `addEditText` | 准星样式 例如：⊙☉⊕·✛╋☩ 等等 | 写「游戏准星样式键名」 |
| `addEditText` | 准星颜色 例如：#009688 或 0xFF009688等等 | 写「游戏准星颜色键名」 |
| `addSeekBarFloat` | 准星大小 | 写「游戏准星大小键名」 |
| `addSeekBarFloat` | 准星透明 | 写「游戏准星透明度键名」 |
| `addSeekBarInt` | X偏移(相对中心点) | 实时 `wParams.x`，抬手保存 |
| `addSeekBarInt` | Y偏移(相对中心点) | 实时 `wParams.y`，抬手保存 |
| `addButton` | 恢复默认配置 | `getGameFrontSightSPED(ctx).clear()` + `updateFrontSightCF()` + 「已重置准星所有配置」 |

> `initFrontSight()` 用 `flags = 1336`（= `0x538` = `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE | FLAG_LAYOUT_NO_LIMITS | ...`），即**完全穿透、不可交互**，符合准星语义。注意它**不随直播模式变化**（写死 1336），是一个与 `AlGuiData.getLiveStreamFlags()` 不一致的地方，重构时应统一。

### 5.3 预制控件类型总览

| 控件 | 工厂 | 是否出现在预制菜单 | 出现位置 |
| --- | --- | --- | --- |
| 折叠菜单 | `addCollapse` | ✅ | 4 个面板的容器外壳 |
| 开关 | `addSwitch` | ✅ | 游戏准星（1 处） |
| 按钮 | `addButton` | ✅ | 使用说明 4 个 + 颜色恢复 1 + 属性恢复 1 + 准星恢复 1 |
| 输入框+应用按钮 | `addEditText`（带按钮重载） | ✅ | 颜色 12 个 + 准星 2 个 |
| 小按钮 | `addSmallButton` | ⚪ | 仅出现在 `addEditText` 内部（作为「应用」按钮） |
| 文本框 | `addTextView` | ✅ | 大量标签与信息 |
| 分隔线 | `addLine` | ✅ | 各分区之间 |
| 跑马灯 | `addMarqueeTextView` | ✅ | SELinux 状态行 |
| 拖动条（小数） | `addSeekBarFloat` | ✅ | 4 属性 + 2 准星 |
| 拖动条（整数） | `addSeekBarInt` | ✅ | 准星 X/Y 偏移 |
| 复选框 | `addCheckBox` | ❌ | 预制菜单未用，仅 `Loading` 演示 |
| WebView / WebSite | `addWebView` / `addWebSite` | ❌ | 预制菜单未用，仅 `Loading` 演示 |

### 5.4 辅助文件

`AlGuiPrefabricatedMenu_LB$$ExternalSyntheticBackport0.java` —— D8 为 `String.join(CharSequence, Iterable)` 生成的脱糖类，**不含业务逻辑，重构时可直接删除**。

---

## 6. 工具层能力（12 个 `Tools` 类）

### 6.1 UI 支撑类（框架内部高频使用）

**`ViewTool`（4,491 B / 125 L / 11 个 public 方法）—— 单位与颜色的唯一入口**
```java
public static int  convertDpToPx(Context context, float f)     // ← 全框架尺寸换算都走它
public static int  dip2px(Context context, float f)
public static int  px2dip(Context context, float f)
public static int  createLayeredColor(int i, float f)          // 按比例改 alpha
public static int  calculateColorInverse(int i)
public static int  darkenColor(int i, float f)
public static int  brightenColor(int i, float f)
public static CharSequence wrapText(CharSequence charSequence, int i)  // 按「中文=1、西文=0.5」加权折行
public static TextView setTextViewShadow(TextView textView, float f, float f2, float f3, int i)
public static Drawable setGradientBackground(ViewGroup viewGroup, int[] iArr, int i)
public static int  getByteCount(byte b)
```

**`ImageTool`（6 方法）**
```java
public static Drawable bitmapToDrawable(Bitmap bitmap)
public static Bitmap   drawableToBitmap(Drawable drawable)
public static Bitmap   getAssetsImage(Context context, String str)
public static Bitmap   getBase64Image(String str)     // ← AlGuiData 14 个图标的解码器
public static Bitmap   fastblur(Bitmap bitmap, int i)
public static Bitmap   blur(Bitmap bitmap, int i)
```

**`RealTimeDataTextTool`（14,444 B / 299 L / 9 个 public 方法 + 若干匿名内部类）—— 把 TextView 变成实时数据牌**

每个方法接收一个 `TextView` 并返回同一个 `TextView`，内部起 `Thread` + `Handler(Looper.getMainLooper())` 轮询（FPS 用 `Choreographer.postFrameCallback` 逐帧）。

```java
public static void     setOrdinaryDataColor_RGB(String str)   // 默认 "#9C27B0"，须匹配 ^#[0-9a-fA-F]{6}$
public static String   getOrdinaryDataColor_RGB()
public static TextView textAddPower(Context context, TextView textView)                    // 电量
public static TextView textAddAvailableMemory(Context context, boolean z, TextView textView) // z=是否显示单位
public static TextView textAddAvailableStorage(boolean z, TextView textView)
public static TextView textAddTime(String str, TextView textView)                          // str 为 SimpleDateFormat 模板
public static TextView textAddFps(TextView textView)                                       // Choreographer，输出 "%.2f FPS"
public static TextView textAddSELinuxMode(TextView textView)
public static TextView textAddDetectAppRooted(TextView textView)
```
输出统一为 `原文本 + <font color='#9C27B0'>值</font>` 经 `Html.fromHtml` 渲染。

### 6.2 系统与应用信息类

**`SystemTool`（17,014 B / 381 L / 23 个 public `static`）**
```java
isOpenNetwork(Context) / isMIUI() / OpenMIUIPerformanceMode(Context)   // 跳 MIUI 省电设置
inspectRootPermission() / getSELinuxMode() / isSELinuxPermissive() / getKernelVersion()
getNetworkStatus(Context) / getBatteryLevel(Context) / getIPAddress(Context)
getTotalMemory(Context,boolean) / getMemoryUsage(Context,boolean) / formatMemorySize(long)
getTotalStorage(boolean) / getAvailableStorage(boolean)
getScreenResolution(Context) / getRealScreenDP(Context) / getScreenWidth(Activity) / getScreenHeight(Activity)
printSystemInfo()                                                     // 一次性汇总
isHarmonyOs() / getHarmonyVersion() / getHarmonyDisplayVersion()      // 鸿蒙识别（读 getprop）
```
实现手段：`System.getProperty("selinux.mode")`（API≥26）、`Runtime.exec("getenforce")`（legacy）、`ActivityManager.MemoryInfo`、`StatFs`、`/proc` 解析。

**`AppTool`（11,162 B / 278 L / 19 个 public `static`）**
```java
isNetworkAvailable(Context) / isAppInForeground() / isDebugMode()
backToDesktop(Context) / showNotificationBar(Context, String, String)
isRootEnabled()                       // exec("su") 写 "echo test" 看 exitCode
getRootPermission(Context)            // chmod 777 包路径后提权
isInstalled(Context,String) / isSystemApp(Context)
getVersionCode / getVersion / getAppName / getAppIcon / getAppPackageName / getAppSignature / getAppUid / getLauncherActivityName
getSameUidAppNames(Context) / isServiceRunning(Context,String)
```

**`AppPermissionTool`（6,455 B / 133 L / 7 个 public `static`）**
```java
public static void   initPermission(Context context, Activity activity)  // 批量申请 10~11 项权限 + 跳悬浮窗授权页
public static void   storePermission(Context context, Activity activity) // 存储权限子集
public static String[] getAllPermissionsFromManifest(Context context)    // 反射读 Manifest
public static void   batchApplyPermission(Context context, String... strArr)
public static void   floatingWindowPermission(Context context)           // 跳 ACTION_MANAGE_OVERLAY_PERMISSION
public static boolean checkOverlayPermission(Context context)            // Settings.canDrawOverlays
public static boolean isAndroidManifestPermissionExist(Context context, String str)  // ← AlGui 构造里用它
```

### 6.3 文件与数据类

**`FileTool`（17,222 B / 366 L / 21 个 public `static`）**
```java
getMIMEType / getSDPath / getFileSuffix / getDirName / getFileName / getFileNameWithSuffix
createFile(String,String) / createFolder(String) / deleteFile(String) / deleteFolder(File)
copyFile(String,String) / copyFolder(String,String) / renameTo(String,String) / renameTo(File,String)
getFileSize(String) / getFolderSize(File) / getFileCount(File)
formatSize(long) / formatTime(long) / getFileList(String) / ReadTxtFile(String)
saveFilePlt(Context, String, String)   // 存到 getExternalFilesDir("")/lensun/<name>.plt
```

**`RegularTool`（9 个静态校验）**：`isHtmlText / isMobileSimple / isMobileExact / isTel / isEmail / isURL / isChz / isUsername / isMatch(String,String)`

**`DataTool`（2 个 `private` 方法）**：`base64Encode` / `base64Decode` —— **都是私有，外部无法调用**，属反编译残留，重构可删或改为 `public`。

### 6.4 副作用类（重构时需重点审计）

**`HackerTool`（3,148 B / 39 L / 3 个 public `static`）—— 框架内唯一的命令执行**
```java
public static void shell(String str)                                   // Runtime.getRuntime().exec(str, null, null)
public static void linuxHackerFile(Context context, String str)        // chmod 777 + 执行 nativeLibraryDir/cacheDir//data/data/<pkg>/lib 下的 str
public static void linuxHackerFile(Context context, String str, String str2, boolean z)  // z 选 str 或 str2
```
每个 `linuxHackerFile` 会对 **6 个路径**依次 `chmod 777` 并执行。这是「加载 SO 补丁/开关」的通用手法：把 `libModOn.so` / `libModOff.so` 放进 lib 目录后直接执行，靠 SO 的 `constructor` 生效。

> **安全提示**：这是框架里唯一具备任意命令执行能力的位置，参数若来自用户输入即构成命令注入。重构时应改为 `ProcessBuilder(List<String>)` 并白名单校验文件名。

**`VariousTools`（3,897 B / 95 L / 4 个 public `static`）**
```java
public static boolean copyToClipboard(Context context, String str)
public static boolean gotoWeb(Context context, String str)
public static boolean joinQQGroup(Context context, String str)         // mqqopensdkapi:// scheme
public static boolean convertTextToSpeech(Context context, String str, Locale locale)  // TTS
```

**`MusicTool`（1 方法）**：`playAssetMp3(Context, String)` —— `AssetFileDescriptor` + `MediaPlayer`，静态单实例（`reset()` 复用）。

---

## 7. 与宿主 App 的接口

### 7.1 初始化路径（三段式）

```
宿主 Activity.onCreate()
   └─ Loading.start(this)                                    【Loading.java:27】
        ├─ context = context2
        ├─ AppPermissionTool.floatingWindowPermission(context)       ① 检查/请求悬浮窗权限
        ├─ AlGuiDialogBox.showTextDiaLog(...)                        ② 弹一次示例对话框
        ├─ initConfigurations()                                      ③ 用代码设定全部外观
        │    ├─ AlGui.GUI(context).getMenuMainTitle().setText("ALGUI")
        │    ├─ ...setTextSize / setText / setBallImage("shi.gif", 50, 50)
        │    ├─ setAllViewMargins(8,8,8,8)
        │    ├─ AlGuiData.xxx = ...  （18 个静态字段逐个赋值）
        │    ├─ updateMenuAppearance()                               刷外观
        │    └─ updateMenu()
        ├─ initMenu()                                                ④ 往滚动列表塞控件
        │    └─ getMenuScrollingListLayout().addView(
        │           PrefabricatedMenu().addExplanation(null),
        │           PrefabricatedMenu().addAttributeStatusMenu(null),
        │           PrefabricatedMenu().addAppearanceSettingsMenu(null),
        │           PrefabricatedMenu().addGameFrontSightMenu(null),
        │           addCollapse(...), addTextView(...), ... )
        └─ AlGui.GUI(context).showBall()                             ⑤ 显示悬浮球（菜单不显示）
```

> **`AlGui.GUI(context)` 是单例**，`Loading` 里被反复调用 **71 次**（`initConfigurations` 10 次，`initMenu` 61 次）却只创建一次实例。这是作者刻意的写法风格，不是 bug，但重构时建议提取局部变量。

### 7.2 宿主最小接入清单

要重建这个框架，宿主**只需 4 件事**：

**① AndroidManifest.xml 声明权限**（框架会在构造时校验，缺失则弹 Toast 拒绝工作）
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```
（`AppPermissionTool.initPermission` 会申请 10 项，`host-apk` 实际声明了 `DUMP / FOREGROUND_SERVICE / INTERNET / MANAGE_EXTERNAL_STORAGE / READ_EXTERNAL_STORAGE / READ_PHONE_STATE / SYSTEM_ALERT_WINDOW / WRITE_EXTERNAL_STORAGE`）

**② 声明一个 Activity**（可复用框架自带的）
```xml
<activity android:name="irene.window.algui.MainActivity"
          android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity>
```

**③ 在 `onCreate` 里调 `Loading.start(this)`**

**④ assets 放一个 `shi.gif`**（悬浮球图标；也可换成任意文件名，改 `setBallImage` 参数即可）

### 7.3 注册菜单的两种方式

**方式 A：用预制面板（零成本）**
```java
vLinearLayout scroll = AlGui.GUI(this).getMenuScrollingListLayout();
scroll.addView(AlGui.GUI(this).PrefabricatedMenu().addExplanation(null));
scroll.addView(AlGui.GUI(this).PrefabricatedMenu().addAttributeStatusMenu(null));
```
传 `null` 表示不自动挂载，由调用方决定插入位置；传 `ViewGroup` 则自动 `addView`。

**方式 B：自己组装（完全自由，`Loading.initMenu()` 就是范本）**
```java
AlGui gui = AlGui.GUI(this);
// 1) 带 ViewGroup 参数 = 自动挂载
gui.addSwitch(scroll, "自动瞄准", 11f, 0xFF000000, null, "说明文字", 8f, -6381922, null,
    -11751600, -10044566, -769226, -1092784,
    new AlGui.T_SwitchOnChangeListener() {
        @Override public void onClick(CompoundButton cb, TextView desc, boolean checked) { /* ... */ }
    });
// 2) 不带 ViewGroup = 返回控件，自己 addView（用于先建后插、或塞进折叠菜单）
LinearLayout row = gui.addLinearLayout(3, 0, -1, -2);   // (orientation, gravity, w, h)
gui.addTextView(row, "数值", 11f, -1092784, null);
gui.addSmallButton(row, " + ", 11f, -1, null, 5f, -12627531, 0f, 0xFF000000, listener);
```

**带子视图的折叠菜单**（`addCollapse` 的 `View...` 重载）：
```java
gui.addCollapse("功能分组", 10, 0xFF000000, null, 3f, -1, 0f, -3814679, false,
    gui.addLine(0.5f, -2039584, true),
    gui.addLinearLayout(17, 0, -1, -2, gui.addTextView("标题", 11f, 0xFF000000, null)),
    gui.addButton("执行", 11f, -1, null, 5f, -12627531, 0f, 0xFF000000, -1, -2, listener)
);
```
返回的是**外层 `vLinearLayout`**（已自动加入 `scrollLayout`）。

### 7.4 反向调用：框架主动回调宿主

框架不定义抽象回调接口，宿主通过**匿名内部类**接入（6 个 `T_*` 接口）。业务逻辑全部写在回调体里，例如 `Loading.java:101–115` 的开关回调：

```java
if (z) {                                   // z = 新状态
    HackerTool.linuxHackerFile(context, "libModOn.so");
    AlGuiBubbleNotification.Inform(context).showSuccessNotification_Simplicity(null, name, "开启成功！", 5000);
    VariousTools.convertTextToSpeech(context, name + "开启成功！", Locale.CHINA);
} else {
    HackerTool.linuxHackerFile(context, "libModOff.so");
    ...
}
```

### 7.5 配套模块的独立入口

| 模块 | 入口 | 说明 |
| --- | --- | --- |
| 气泡通知 | `AlGuiBubbleNotification.Inform(Context)` | 单例；构造即建窗并 `showW()`，无需显式显示 |
| 音效 | `AlGuiSoundEffect.getAudio(Context)` | 单例；构造即把 10 段音频解到 `cacheDir` |
| 对话框 | `AlGuiDialogBox.showDiaLog(Context, int 背景色, float 圆角, View...)` | 静态，无单例 |
| 独立浮窗 | `AlGuiWindowView.showNeonLightText / showText / showWebView / showWebSite` | 全静态，各自 `addView` 到独立 `WindowManager` |

### 7.6 与 `host-apk` 的真实关系（重要澄清）

在 `Riruriru Mod 1.1.2` 这个具体宿主中：

- **`com.Riruriru.Sx`（8 个类）完全不知道 AlGui 的存在**——`MainActivity` 调 `FloatStartService.load(this)`，起 `FloatServiceView`（一个 `Service`），用 `FloatControlView` / `FloatContentView` 自建悬浮窗。它走的是**自己的悬浮窗 + assets 里的 `64`（ptrace 注入器）+ `libArkRe.so`** 的路径。
- **`irene.window.algui`（28 个类）是被整体打包进来的第三方 UI 框架**，保留了自己的 `MainActivity`、`R.java`、`BuildConfig.java`，说明它是**原样搬运**（打包者只改了 `MainActivity:14` 那一行资源引用）。
- 因此这份文档描述的是一套**通用悬浮窗框架**，与「注入 Ark ReCode / Cherry Tale」的业务无关。若目标是复刻宿主功能，需要另看 `com.Riruriru.Sx`；若目标是**重建一个 AlGui 风格的悬浮菜单**，本文 + `Loading.java` 即是完整规格。

---

## 8. 重构指南（落地建议）

### 8.1 依赖顺序（自底向上）

```
① Tools（12 个类，纯静态，零框架依赖）
     └ ViewTool 是地基（所有 dp 换算都靠它）
② CustomizeView（4 个类，仅依赖 ViewTool）
③ AlGuiData（数据模型 + 资源，零依赖）
④ AlGui（主类，依赖 ①②③）
⑤ AlGuiPrefabricatedMenu_LB（依赖 ④）
⑥ AlGuiBubbleNotification / AlGuiDialogBox / AlGuiWindowView（依赖 ③①，可并行）
⑦ AlGuiSoundEffect / Loading / MainActivity（最上层）
```

### 8.2 体积优化

| 项 | 现状 | 建议 |
| --- | --- | --- |
| `AlGuiSoundEffect` | 306,605 B / 110 行（**base64 音频占 99.5%**） | 移到 `res/raw/` 或 `assets/`，删除 `saveAudio`/`initBase` 全部逻辑；**结构上只剩 `getAudio()` + `playSoundEffect()` 两个方法** |
| `AlGuiData` | 47,301 B / 376 行（14 个 base64 PNG） | 移到 `res/drawable/`，`getXxxIcon()` 改返回资源 ID |
| `AlGuiPrefabricatedMenu_LB$$ExternalSyntheticBackport0` | 822 B | 直接删除 |

### 8.3 已识别的问题（重构时修掉）

| 问题 | 位置 | 建议 |
| --- | --- | --- |
| getter 名与字段名错位 | `getMenuSubTitle()` → `edition`；`getMenuExplanation()` → `subTitle` | 改名为 `getMenuEdition()` / `getMenuExplanation()` |
| `addCollapse` 两个重载返回类型/语义不一致 | `AlGui.java:1013` vs `1107` | 统一为「返回内容区」，外层通过 `getParent()` 取 |
| `getMenuMainTitle()` 文档说主标题、实际是 `title`，但 `updateMenuAppearance` 里注释与实际 | — | 补 KDoc |
| 配置读取样板代码 | 每项 4 次 `HashMap.get` + 强转 | 用 `enum ConfigKey { KEY("中文键", 默认值) }` + 泛型 `get()/put()` |
| `DataTool` 方法全 `private` 不可用 | `Tools/DataTool.java` | 改 `public static` 或删除 |
| 准星窗口 flags 写死 `1336`，不随直播模式 | `AlGuiPrefabricatedMenu_LB.java:1333` | 改为 `AlGuiData.getLiveStreamFlags() \| ...` |
| `HackerTool.shell` 任意命令执行 | `Tools/HackerTool.java:12` | `ProcessBuilder(List)` + 文件名白名单 |
| `updateMenuAppearance` 里 `menuParams` 宽高被重置为 `-2` | `AlGui.java:362-363` | 会覆盖用户缩放后的尺寸，需在调用前保存/恢复 |
| `AlGuiData` 全 `public static` 可变字段 | 全局状态 | 改为实例 + `AlGui` 持有，或至少收敛为 private + getter/setter |
| 反编译生成的 `AnonymousClass1000000XX` 内部类名 | 全文 | 重命名为有意义的监听器类名 |

### 8.4 关键常量速查

```
窗口类型      2038 (TYPE_APPLICATION_OVERLAY, API≥26) | 2003 (TYPE_PHONE, API<26)
直播模式差值  0x1000  (16781312 - 16777216 = 4096) —— 语义待验证，非 FLAG_SECURE(0x2000)
0x1000000     悬浮窗通用基址位（16384 项 flags 中的最高位区）
0x8           FLAG_NOT_FOCUSABLE
0x20          FLAG_NOT_TOUCH_MODAL
0x40000       FLAG_LAYOUT_INSET_DECOR
直播 flags    开 16781312 (0x1001000) / 关 16777216 (0x1000000)
准星 flags    1336 (0x538)
悬浮球/菜单 gravity  51 (TOP|LEFT)    气泡 gravity 8388693 (0x00800055 = BOTTOM|RIGHT)
气泡偏移      x = y = dpToPx(16)
拖动阈值      悬浮球 20px / 菜单 20px / 缩放 10px
菜单窗关闭态  flags = android.R.string.config_feedbackIntentNameKey （反编译把常量还原成资源 ID，实际数值未知）
```

---

*本文档由 `read` + `grep`/`Select-String` 从源码逐项提取生成；方法签名、字节数、行数、常量值均为实测，未作推测。*
