# 模块还原 · host 业务类（com.Riruriru.Sx）

对 `restore/host-apk/src/com/Riruriru/Sx/` 下 7 个业务类（`R.java` 除外）的逐方法还原。

**还原来源**：jadx 1.5.0 反编译 + 我们自研的 `dalvik2java.py` 交叉验证 + 原始 dex 字符串对拍

---

## 一、类清单

| 类 | 继承 | 大小 | 职责 |
| --- | --- | --- | --- |
| `MainActivity` | `AppCompatActivity` | 3057 B | 入口，权限申请、加群、赞助跳转 |
| `FloatStartService` | `Service` | 773 B | 极薄的启动器，只负责拉起悬浮窗 |
| `FloatServiceView` | `Service` | 5322 B | 前台服务，读日志文件并回显到通知/UI |
| `FloatControlView` | `View`（自定义） | 7013 B | 悬浮球：拖动、吸附、旋转动画、显隐 |
| `FloatContentView` | **`PopupWindow`** | 23920 B | **核心面板**：解压 assets、起注入器、回显日志 |
| `Miscellaneous` | — | 4128 B | 静态工具方法集 |
| `Start` | — | 3287 B | 12 个功能占位（**全是空壳**） |

---

## 二、FloatContentView（核心）

### 2.1 字段

```java
private File arkReSoFile;          // 解压出的 libArkRe.so
private File binaryFile;           // 解压出的 assets/64 注入器
private File cherryTaleSoFile;     // 解压出的 libCherryTale.so
private List<Switch> colorSwitches;// 彩虹色开关集合
private TextView gameSelectionText;// 显示当前选中的游戏
private boolean isShowing;
private Context mContext;
private ScrollView scrollViewLog;  // 日志滚动区
private int selectedGame;          // 0=CherryTale  其他=ArkReCode
private TextView tvLogOutput;      // 日志输出
public WindowManager wManager;
public WindowManager.LayoutParams wParams;
```

### 2.2 构造链

```java
public FloatContentView(Context context) {
    super(context);                 // PopupWindow(Context)
    isShowing = false;
    selectedGame = 0;
    colorSwitches = new ArrayList();
    mContext = context;
    initView();                     // 建 UI
    load();                         // 建 WindowManager 参数
    setupAssets();                  // 后台线程解压三个 payload
}
```

### 2.3 方法逐个

| 方法 | 语义 |
| --- | --- |
| `<init>(Context)` | 构造链见上 |
| `initView()` | 建整个面板 UI（见 2.4） |
| `load()` | 建 `WindowManager.LayoutParams`，按 SDK 版本选 window type |
| `setupAssets()` | **后台线程**解压 `64` / `libArkRe.so` / `libCherryTale.so` 到 `filesDir`，并给注入器加执行权限 |
| `extractAssetFile(String, File)` | 从 assets 拷贝到目标文件（幂等：已存在且非空则跳过） |
| `extractAssetFile(String, File, String)` | 带日志输出的版本，会 `appendLog("✓ xxx 解压完成 (N 字节)")` |
| `setFilePermissions(File)` | `setReadable/setWritable/setExecutable(true, false)` —— 注意第二参数 `false` 表示**对所有用户** |
| `appendLog(String)` | 通过 `tvLogOutput.post()` 追加一行日志并滚动到底 |
| `startInjection(TextView)` | 按 `selectedGame` 选游戏包名与 payload，校验文件就绪，起线程调 `executeInjection` |
| `executeInjection(...)` | **真正的注入**：`chmod 777` → `ProcessBuilder(binary, "-pkg", 包名, "-lib", payload)` → 读回 stdout |
| `resetButton(TextView)` | 恢复按钮可点状态 |
| `showToast(String)` | Toast 提示 |
| `initColorChangingSwitch(Switch)` | 给开关加一个 10ms 周期的 RGB 循环变色 Runnable |
| `writeToFile(String)` | 把内容写到 `/sdcard/1.txt`（调试残留） |
| `showView()` / `hideView()` | 显示/隐藏 PopupWindow |
| `clearView()` | **空实现** |
| `CustomSwitch`（内部类） | 自定义 Switch：`GradientDrawable` 画圆形 thumb + 圆角 track |

### 2.4 initView() 的 UI 结构

```
LinearLayout（根，背景 = R.drawable.srzx，取不到则纯黑）
└─ LinearLayout（垂直）
   ├─ 标题区 / 游戏选择区（gameSelectionText）
   ├─ ScrollView（scrollViewLog）
   │  └─ TextView（tvLogOutput，日志输出）
   ├─ 若干 CustomSwitch（带彩虹变色）
   └─ 注入按钮
```

背景图通过**运行时资源查找**获取，不是硬编码 R 引用：

```java
int resId = mContext.getResources().getIdentifier("srzx", "drawable", mContext.getPackageName());
if (resId != 0) linearLayout.setBackgroundResource(resId);
else            linearLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
```

这是重建时要注意的点 —— **资源名 `srzx` 必须存在**，否则面板变纯黑。

### 2.5 load()：Window type 的选择

```java
wManager = (WindowManager) mContext.getSystemService("window");
wParams = new WindowManager.LayoutParams();
wParams.type  = (Build.VERSION.SDK_INT >= 26) ? 2038 : 2003;   // TYPE_APPLICATION_OVERLAY : TYPE_PHONE
wParams.flags = 1848;
```

`1848 = 0x738` = `FLAG_NOT_FOCUSABLE(8) | FLAG_NOT_TOUCH_MODAL(0x20) | FLAG_LAYOUT_NO_LIMITS(0x200) | FLAG_LAYOUT_IN_SCREEN(0x100) | FLAG_WATCH_OUTSIDE_TOUCH(0x400)`

### 2.6 游戏选择逻辑（关键）

`selectedGame == 0` → **Cherry Tale**，否则 → **Ark ReCode**：

```
selectedGame == 0
  ├─ 包名 = "com.neversoft.rpg.erolabs"
  └─ payload = cherryTaleSoFile
selectedGame != 0
  ├─ 包名 = "com.nerversoft.ark.recode"
  └─ payload = arkReSoFile
```

（与 payload 内部字符串 `(IsNetBattle=true)`、`libCherryTale.so` 吻合）

---

## 三、注入链（完整还原）

```
用户点「启动」
  │
  └─ startInjection(button)
       ├─ 按 selectedGame 选 {包名, payload}
       ├─ 校验 binaryFile 与 payload 都存在且非空
       │    不满足 → showToast("SO文件未准备就绪，请等待...")
       │              showToast("注入工具未准备就绪，请等待...")
       └─ new Thread(FloatContentView$5).start()
            │
            └─ executeInjection(pkg, soPath, button)
                 ├─ appendLog("启动中...")
                 ├─ Runtime.getRuntime().exec("chmod 777 " + binaryPath).waitFor()
                 ├─ new ProcessBuilder(binaryPath, "-pkg", pkg, "-lib", soPath)
                 │    .redirectErrorStream(true)      // stderr 并入 stdout
                 │    .start()
                 ├─ BufferedReader 逐行读回 → appendLog(...)
                 └─ waitFor() 拿退出码 → 更新按钮文案
```

**注意**：`executeInjection` 在 jadx 输出里被内联进了 `FloatContentView$5`（匿名 Runnable），
但我们的独立反汇编器（`disasm/com/Riruriru/Sx/FloatContentView.smali`）里能看到
它原本是独立私有方法 `executeInjection(String,String,String,TextView)`。

---

## 四、FloatControlView（悬浮球）

| 字段/方法 | 语义 |
| --- | --- |
| `wManager` / `wParams` | 悬浮球的 WindowManager 参数 |
| `downX/downY/moveX/moveY/signX/signY` | 触摸起点、移动量、符号位（用于判断拖拽方向） |
| `isView` | 球体当前是否可见 |
| `initView()` | 建球体视图 |
| `showView()` / `clearView()` | 显示 / 移除 |
| `startShowAnimation()` / `startHideAnimation()` | 显隐动画 |
| `startRotationAnimation()` | 旋转动画 |
| `onTouch`（内部类 `$2`） | 拖拽逻辑 + 吸附到屏幕边缘 |

---

## 五、FloatServiceView（前台服务）

| 方法 | 语义 |
| --- | --- |
| `onCreate()` | 建通知渠道，起前台服务 |
| `onBind()` | 返回 null（不提供绑定） |
| `onDestroy()` | 清理 |
| `initView()` | 初始化 |
| `readFileContent(File)` | 读日志文件内容 |
| `startFileReadThread()` | 起线程周期性读文件 |
| `updateTextView(String)` | 把读到的内容更新到 UI |

`permissionStatus` 字段用于跟踪悬浮窗权限状态。

---

## 六、Miscellaneous（工具集）

**关键算法已确认**（jadx 把中文名重命名了）：

| 原名 | jadx 名 | 实现 |
| --- | --- | --- |
| `可逆加密` | `m88` | `char[i] ^= 't'`（异或 116） |
| `可逆解密` | `m89` | **同样的算法**（异或对称） |
| `写出assets资源文件` | `m87assets` | assets → 文件，带目录创建 |
| `RunShell` | 同名 | `Runtime.exec(shell, null, null)` |
| `返回桌面` | `m92` | `Intent(ACTION_MAIN) + CATEGORY_HOME + flags 270532608` |
| `打开MIUI性能模式` | `m90MIUI` | 跳 `com.android.settings.fuelgauge.PowerModeSettings` |
| `网络检测` | `m91` | `ConnectivityManager.getActiveNetworkInfo().isConnected()` |
| `StatusNavigationColor` | 同名 | 设置状态栏/导航栏颜色 |

**加解密算法**（重构时直接用）：

```java
// 加解密同一个函数 —— 异或 't' 是对合的
public static String 可逆加解密(String inStr) {
    char[] a = inStr.toCharArray();
    for (int i = 0; i < a.length; i++) {
        a[i] = (char) (a[i] ^ 't');
    }
    return new String(a);
}
```

`StatusNavigationColor` 里有个细节：`window.addFlags(Integer.MIN_VALUE)`（= `0x80000000`），
这是 `FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS`，必须加才能改状态栏颜色。

---

## 七、Start（空壳）

12 个方法：`一功能1..3` / `二功能1..3` / `三功能1..3` / `四功能1..3`

**全部是** `return null;`（返回 `String`）—— 纯占位，无实际逻辑。
对应 12 个静态 String 字段（也是 null）。重构时可以整类删掉。

---

## 八、MainActivity

| 方法 | 语义 |
| --- | --- |
| `onCreate()` | 设布局、申请存储权限 |
| `储存权限()` | 申请 `WRITE_EXTERNAL_STORAGE` |
| `joinQQGroup()` | 跳 QQ 加群链接 |
| `openUrl(String)` | 用浏览器打开 URL |

---

## 九、全部硬编码常量（重建对照表）

### 9.1 命令与路径

| 常量 | 出处 | 用途 |
| --- | --- | --- |
| `"sh -c chmod 777 "` | `setupAssets` | 给注入器加执行权限 |
| `"chmod 777 "` | `executeInjection` | 同上（exec 版本） |
| `"64"` | `setupAssets` | 注入器 assets 名 |
| `"libArkRe.so"` / `"libCherryTale.so"` | `setupAssets` | payload assets 名 |
| `"-pkg"` / `"-lib"` | `executeInjection` | 注入器命令行参数 |
| `"1.txt"` | `writeToFile` | 调试输出（`/sdcard/1.txt`） |

### 9.2 游戏包名

| 常量 | 对应 |
| --- | --- |
| `"com.nerversoft.ark.recode"` | Ark ReCode（`selectedGame != 0`） |
| `"com.neversoft.rpg.erolabs"` | Cherry Tale（`selectedGame == 0`） |

### 9.3 UI 提示语

```
"注入中..."           "启动中..."
"SO文件未准备就绪，请等待..."
"注入工具未准备就绪，请等待..."
"启动失败：未下载驱动内部安装包"
"启动异常：请检查游戏是否运行"
"启动成功 即将退出"
"已存在，跳过解压"    "解压完成 ("    "解压失败: "
"数据已修改"          "失败: "
```

### 9.4 资源名

| 名 | 类型 | 用途 |
| --- | --- | --- |
| `srzx` | drawable | 面板背景图（运行时 getIdentifier 查找） |
| `sx` | drawable | 另一张背景图（1.33 MB，主视觉） |

### 9.5 颜色与尺寸

```java
thumbDrawable.setColor(-1856508746);   // = 0x91484848（半透明深灰）
trackDrawable.setColor(-16776961);     // = 0xFF0000FF（纯蓝）
trackDrawable.setCornerRadius(60.0f);
trackDrawable.setSize(150, 50);        // thumb 是 50x50
setPadding(20, 20, 20, 20);
```

---

## 十、Android 交互点

| 项 | 说明 |
| --- | --- |
| 权限 | `SYSTEM_ALERT_WINDOW`（悬浮窗，必须）、`INTERNET`、`READ/WRITE_EXTERNAL_STORAGE`、`READ_PHONE_STATE`、`FOREGROUND_SERVICE`、`MANAGE_EXTERNAL_STORAGE` |
| Service | `FloatServiceView`（前台服务，`exported=false`） |
| WindowManager | PopupWindow + `LayoutParams.type` 按 SDK 选 2038/2003 |
| sharedUserId | `com.Riruriru.Sx` —— 与配套游戏共享 UID |
| 存储 | `filesDir` 放解压出的 payload；外部存储放调试文件 |

---

## 十一、jadx 失真点（重构注意）

| 现象 | 说明 | 处理 |
| --- | --- | --- |
| `m88`/`m89`/`m87assets`/`m90MIUI`/`m91`/`m92` | 中文方法名被重命名 | 按第六节对照表改回 |
| `$$Nest$fgetxxx` / `$$Nest$mxxx` | 内部类访问私有成员时的桥接方法 | 重构时可删 |
| `executeInjection` 出现在 `$5` 里 | jadx 做了内联 | 语义等价，可保留内联形式 |
| `R.java` 是常量表 | 反编译出的资源 ID 是数值 | 用 aapt 重新生成 |
| `FloatContentView$CustomSwitch` | 内部类被拆成独立文件 | 合并回主类 |
| 泛型擦除 | `new ArrayList()` 少了 `<>` | 按上下文补 |
| 空 catch 块 | `catch (Exception e) {}` | 原代码就是这样（刻意吞异常） |

---

## 十二、对拍验证

还原的忠实度已用脚本验证，见 [VERIFY-DIFF.md](../VERIFY-DIFF.md)：

- 源码里 **59 条字符串全部命中**原始 dex，无信息丢失
- **关键常量 27/27 项一致**（包名、payload 名、命令、路径、提示语）
- 36 个类**语法解析全部通过**；ImGui 层**完整编译通过**（生成 9 个 class）
