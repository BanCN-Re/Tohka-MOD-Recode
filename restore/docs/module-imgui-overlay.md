# ImGui 覆盖层模块还原（`com.example.imgui`）

本文还原 `libArkRe.so` / `libCherryTale.so` 内嵌 `injected.dex` 里 **两个类的完整逻辑**：
`ImGui`（挂载 + 触摸分发）与 `GLES3JNIView`（GL 表面 + 渲染回调）。

- 源码位置：[ImGui.java](ArkReCodeHack/restore/injected-dex/src/com/example/imgui/ImGui.java)（29,261 B / 568 行）
- 源码位置：[GLES3JNIView.java](ArkReCodeHack/restore/injected-dex/src/com/example/imgui/GLES3JNIView.java)（2,105 B / 62 行）
- 反汇编证据：[imgui-dex-disasm.txt](ArkReCodeHack/work/imgui-dex-disasm.txt)（androguard 逐条反汇编，含分支目标解析）
- 导出符号证据：[so-analysis.txt](ArkReCodeHack/work/so-analysis.txt) 第 240–253 行

> **重要前提：JADX 的这份反编译有一处会误导人。**
> `dispatchGameEvent` 被 JADX 渲染成 4 个 `MotionEvent.obtain(...)` 调用点、且 `action == 0`
> 时「先发 ACTION_DOWN、再无条件发 ACTION_POINTER_DOWN」。**真实字节码只有一个 `obtain` 调用点**，
> 是一个干净的 if/else。详见 §5.5。其余方法逐条核对与源码一致。

---

## 1. 两个类的分工

```
游戏进程
 └─ DexClassLoader 加载 injected.dex
     ├─ ImGui                       ← 无参实例；【触摸状态机】的唯一持有者
     │   ├─ setupImGuiViewOnMainThread(Activity)   ← native 通过 JNI 调进来的唯一入口
     │   └─ 建 GLES3JNIView，挂到 android.R.id.content
     └─ GLES3JNIView : GLSurfaceView : SurfaceView : View
         └─ implements GLSurfaceView.Renderer     ← 渲染线程回调 → native
```

关键点：**`ImGui` 的所有触摸状态都是实例字段，不是静态的。**
`setupImGuiViewOnMainThread` 里 `new ImGui()` 造出来的那个实例，
靠 `ImGui$4`（匿名 `OnTouchListener`）持有的 `this$0` 隐式引用活下来——
只要 View 还挂在窗口上，这个实例就不会被回收，状态机也就一直有效。
（反汇编确认：`ImGui$4` 构造函数签名 `(Lcom/example/imgui/ImGui; Landroid/widget/FrameLayout;)V`。）

---

## 2. 完整方法清单

### 2.1 `com.example.imgui.ImGui`

**字段**（`static_values` 取自 DEX `class_data`，非猜测）：

| 字段 | 类型 | 初值 | 说明 |
|---|---|---|---|
| `IMGUI_VIEW_TAG` | `String` | `"imgui_overlay_view"` | private static final，覆盖层 View 的身份标记 |
| `MAX_POINTERS` | `int` | `10` | private static final，两个布尔数组的长度 |
| `TAG` | `String` | `"ImGui"` | private static final，日志 tag |
| `cardKeyText` | `String` | `""` | public static，卡密过滤文本 |
| `classFilterText` | `String` | `""` | public static，类名过滤文本 |
| `objectFilterText` | `String` | `""` | public static，对象过滤文本 |
| `objectNameFilterText` | `String` | `""` | public static，对象名过滤文本 |
| `roleFilterText` | `String` | `""` | public static，角色过滤文本 |
| `selectedFilterType` | `int` | `0` | public static，当前编辑哪个过滤框（编号见 §7）|
| `display` | `GLES3JNIView` | `null` | private static，唯一的覆盖层实例 |
| `currentActivityRef` | `WeakReference<Activity>` | — | private，**只写不读**（本版本无任何读取点）|
| `pointerInImGui` | `boolean[10]` | — | private，按 pointerId 索引 |
| `pointerInButton` | `boolean[10]` | — | private，按 pointerId 索引 |
| `gamePointerIdMap` | `Map<Integer,Integer>` | 空 HashMap | private，**逻辑上是死字段**，见 §5.2 |
| `activeGamePointers` | `List<Integer>` | 空 ArrayList | private，透传给游戏的指针白名单 |
| `gameDownTime` | `long` | `0` | private，合成事件的 `downTime` |
| `imguiPointerCount` | `int` | `0` | private，按在 ImGui 上的手指数 |

**方法**：

| 签名 | 可见性 | 语义 |
|---|---|---|
| `static void setupImGuiViewOnMainThread(Activity)` | public static | **native 侧入口**。先打日志 `setupImGuiViewOnMainThread called`；若已在主线程 → `new ImGui().setupImGuiView(activity)`；否则 `new Handler(getMainLooper()).post(...)` 里做同样的事。注意**每次调用都 new 一个新实例**。 |
| `static void setSelectedFilterType(int)` | public static | **native 侧入口**，`selectedFilterType = i`。决定 `showDirectInputMethodFromNative` 编辑哪个过滤框。 |
| `static void showDirectInputMethodFromNative()` | public static | **native 侧入口**，把系统输入法接进来。详见 §6。 |
| `void setupImGuiView(Activity)` | private | 主线程兜底：不在主线程就 `Handler.post` → `setupImGuiViewInternal`，在主线程就直接调。 |
| `void setupImGuiViewInternal(Activity)` | private | **真正干活**：建 View、配参数、挂载。详见 §4。 |
| `boolean handleTouchEvent(View, MotionEvent, ViewGroup)` | private | **触摸分发总入口**，由 `ImGui$4.onTouch` 调用。详见 §5。 |
| `void initPointerTracking()` | private | 清空全部指针状态：两个布尔数组全 `false`、`imguiPointerCount=0`、两个集合 `clear()`、`gameDownTime=0`。挂载时调一次。 |
| `void addGamePointer(int)` | private | 登记一个「要透传给游戏」的指针。已在列表则直接 return；否则 `map.put(id, size)`、`list.add(id)`，**若加完 size==1 则 `gameDownTime = SystemClock.uptimeMillis()`**。 |
| `void removeGamePointer(int)` | private | 从 list/map 移除该 id，然后 **`map.clear()` 并按当前 list 顺序重建 `map`**（即 id → 新下标）。 |
| `void dispatchGameEvent(MotionEvent, int action, int pointerId, ViewGroup)` | private | 合成 DOWN/UP/POINTER_DOWN/POINTER_UP 回灌游戏。详见 §5.4 + §5.5。 |
| `void dispatchGameMoveEvent(MotionEvent, ViewGroup)` | private | 合成 `ACTION_MOVE`（action=2）回灌游戏。 |
| `void dispatchGameCancelEvent(MotionEvent, ViewGroup)` | private | 合成 `ACTION_CANCEL`（action=3）回灌游戏。 |
| `int findPointerIndex(MotionEvent, int id)` | private | 线性扫 `getPointerCount()` 找 `getPointerId(i)==id` 的下标，找不到返回 `-1`。 |
| `void dispatchToChildren(ViewGroup, MotionEvent)` | private | **倒序**遍历子 View，跳过 tag==`imgui_overlay_view` 的那个，做坐标平移后 `dispatchTouchEvent`；被消费则 return。详见 §5.4。 |
| `static boolean isImGuiComponentTouched(float, float)` | public static | 纯 Java 命中测试：调 `nativeGetImGuiWindowBounds()` 拿矩形数组，任一矩形命中即 true。 |
| `static boolean isCircularButtonTouched(float, float)` | public static | 纯 Java 命中测试：调 `nativeGetCircularButtonBounds()` 拿圆数组，任一圆命中即 true。 |
| `static native float[] nativeGetImGuiWindowBounds()` | private static native | 见 §3。 |
| `static native float[] nativeGetCircularButtonBounds()` | private static native | 见 §3。 |
| `<init>()` | — | 只初始化两个 `boolean[10]`、HashMap、ArrayList、`gameDownTime=0`、`imguiPointerCount=0`。 |
| `<clinit>()` | — | **空**（`return-void` 一条）。静态初值都在 DEX `static_values` 里，没有 Java 初始化代码。 |
| `access$000/100/200` | 合成 | 编译器生成的桥：分别转发 `setupImGuiView` / `setupImGuiViewInternal` / `handleTouchEvent` 给匿名内部类。 |

**匿名内部类**（`ImGui$1` ~ `ImGui$4`）：

| 类 | 位置 | 作用 |
|---|---|---|
| `ImGui$1` | `setupImGuiViewOnMainThread` 的非主线程分支 | `Handler.post` 里再 `new ImGui().setupImGuiView(activity)` |
| `ImGui$2` | `setupImGuiView` 的非主线程分支 | `Handler.post` 里调 `setupImGuiViewInternal` |
| `ImGui$3` | `showDirectInputMethodFromNative` | `runOnUiThread` 里建 EditText、挂 TextWatcher、拉输入法 |
| `ImGui$3$1` | 上面那个 EditText 的 `TextWatcher` | `onTextChanged` 把文本写回静态字段 + 调 `nativeUpdateFilterText` |
| `ImGui$3$2` | 同一个 EditText 的 `OnKeyListener` | 回车 → 收起 |
| `ImGui$3$2$1` | 上面那个 `postDelayed` | `clearFocus()` + `removeView()` |
| `ImGui$4` | `setupImGuiViewInternal` | `OnTouchListener` → `handleTouchEvent(view, event, frameLayout)` |

### 2.2 `com.example.imgui.GLES3JNIView`

| 签名 | 可见性 | 语义 |
|---|---|---|
| `GLES3JNIView(Context)` | public | `super(context)`；`setEGLConfigChooser(8,8,8,8,16,0)`；`setEGLContextClientVersion(3)`；`setRenderer(this)`。 |
| `void onSurfaceCreated(GL10, EGLConfig)` | public override | `GLES20.glClearColor(0,0,0,0)` → `init(getHolder().getSurface())`。 |
| `void onSurfaceChanged(GL10, int, int)` | public override | `GLES20.glViewport(0,0,w,h)` → `resize(w,h)`。 |
| `void onDrawFrame(GL10)` | public override | `GLES20.glClear(16640)` → `step()`。 |
| `void onDetachedFromWindow()` | protected override | `super` → `imgui_Shutdown()`。 |
| `static byte[] fontData` | public static 字段 | **Java 侧从不读写**，留给 native 灌字体图集用（见 §8）。 |
| `TAG = "GLES3JNIView"` | private static final | 日志 tag，本类也没用到。 |
| 9 个 `native` 声明 | — | 见 §3。 |

---

## 3. native 方法声明 ↔ `.so` 导出对照

Java 侧一共声明 **11 个 native 方法**（`ImGui` 2 个 + `GLES3JNIView` 9 个）。
但 `.so` 里有 **13 个** `Java_com_example_imgui_*` 导出，多出的两个没有 Java 声明。

| Java 类 | 方法 | JNI 签名 | `.so` 符号 | 大小 |
|---|---|---|---|---|
| `ImGui` | `nativeGetImGuiWindowBounds` | `()[F` | `Java_com_example_imgui_ImGui_nativeGetImGuiWindowBounds` | 376 |
| `ImGui` | `nativeGetCircularButtonBounds` | `()[F` | `Java_com_example_imgui_ImGui_nativeGetCircularButtonBounds` | 1588 |
| `GLES3JNIView` | `init` | `(Ljava/lang/Object;)V` | `Java_com_example_imgui_GLES3JNIView_init` | 340 |
| `GLES3JNIView` | `resize` | `(II)V` | `Java_com_example_imgui_GLES3JNIView_resize` | 80 |
| `GLES3JNIView` | `step` | `()V` | `Java_com_example_imgui_GLES3JNIView_step` | 1056 |
| `GLES3JNIView` | `imgui_Shutdown` | `()V` | `Java_com_example_imgui_GLES3JNIView_imgui_1Shutdown` | 64 |
| `GLES3JNIView` | `MotionEventClick` | `(ZFF)V` | `Java_com_example_imgui_GLES3JNIView_MotionEventClick` | 68 |
| `GLES3JNIView` | `nativeOnTouchEvent` | `(II[I[F[F[FI)V` | `Java_com_example_imgui_GLES3JNIView_nativeOnTouchEvent` | 552 |
| `GLES3JNIView` | `isImGuiComponentTouched` | `(FF)Z` | `Java_com_example_imgui_GLES3JNIView_isImGuiComponentTouched` | 176 |
| `GLES3JNIView` | `nativeUpdateFilterText` | `(ILjava/lang/String;)V` | `Java_com_example_imgui_GLES3JNIView_nativeUpdateFilterText` | 488 |
| `GLES3JNIView` | `updateHexInput` | `(Ljava/lang/String;)V` | `Java_com_example_imgui_GLES3JNIView_updateHexInput` | 848 |
| —（无声明） | — | — | `Java_com_example_imgui_GLES3JNIView_getWindowRect` | 224 |
| —（无声明） | — | — | `Java_com_example_imgui_GLES3JNIView_real` | 20 |

**三个必须记住的坑**：

1. **`imgui_Shutdown` 的符号名是 `imgui_1Shutdown`**——JNI 把方法名里的 `_` 转义成 `_1`。
   按 `Java_com_example_imgui_GLES3JNIView_imgui_Shutdown` 去 `dlsym` 必然失败。
2. **`init` 的参数是 `Ljava/lang/Object;` 而不是 `Landroid/view/Surface;`**。
   调用点传的确实是 `getHolder().getSurface()`，但声明是 `Object`。
   native 侧对应 `ANativeWindow_fromSurface(env, surface)`（`.so` 导入表里有这个符号），
   它接受任意 `jobject`，所以能用。注册 native 时签名必须写 `(Ljava/lang/Object;)V`。
3. **`getWindowRect` / `real` 有导出但 dex 里没有对应声明**。
   `RegisterNatives` 是批量接口，**注册一个不存在的方法会导致整批失败**——
   所以必须逐个注册（`replicate/loader.c` 就是这么做的），或干脆排除这两个。
4. **两个 `isImGuiComponentTouched` 不是同一个东西**：
   `GLES3JNIView.isImGuiComponentTouched(FF)Z` 是 **native**（有导出）；
   `ImGui.isImGuiComponentTouched(FF)Z` 是 **纯 Java**（内部调 `nativeGetImGuiWindowBounds`）。
   名字撞车但语义不同层次，别混。

**native 反向调用 Java 的入口**（`.so` 的 `.data` 段里能找到这些 JNI 方法名字符串，
说明是 native 主动 `GetStaticMethodID` + `CallStaticVoidMethod` 拉起来的）：

- `setupImGuiViewOnMainThread`（`(Landroid/app/Activity;)V`）
- `showDirectInputMethodFromNative`（`()V`）
- `setSelectedFilterType`（`(I)V`）
- `updateHexInput` / `fontData`（留给更新版本 Java 代码的钩子）

---

## 4. 视图挂载流程（菜单能显示的全部秘密）

### 4.1 三级调用链

```
native (JNI CallStaticVoidMethod)
  └─ ImGui.setupImGuiViewOnMainThread(activity)          [public static]
       ├─ 已在主线程 → new ImGui().setupImGuiView(activity)
       └─ 否则      → Handler(getMainLooper()).post(ImGui$1) → new ImGui().setupImGuiView(activity)
            └─ ImGui.setupImGuiView(activity)            [private]
                 ├─ 已在主线程 → setupImGuiViewInternal(activity)
                 └─ 否则      → Handler(getMainLooper()).post(ImGui$2) → setupImGuiViewInternal(activity)
                      └─ ImGui.setupImGuiViewInternal(activity)   [private，真正干活]
```

`setupImGuiViewOnMainThread` 和 `setupImGuiView` 都做了「已在主线程就直调、否则 post」，
所以**从任何线程调用都是安全的**，最坏情况多绕一次 Handler。整个 `Internal` 包在
`try { ... } catch (Exception e) { Log.i(TAG, "ImGui Hook Error: " + e.getMessage()); }` 里，
任何失败都只留一行日志，不会崩进程。

### 4.2 `setupImGuiViewInternal` 逐步

```java
setupImGuiViewInternal(Activity activity):
  1. initPointerTracking()                                   // 清空触摸状态
  2. FrameLayout root = (FrameLayout) activity.findViewById(16908290)
        // 16908290 = 0x01020002 = android.R.id.content
     if (root == null) { log("ImGui Hook: rootView is null"); return; }
  3. if (root.findViewWithTag("imgui_overlay_view") != null) return;   // 幂等，防重复挂载
  4. this.currentActivityRef = new WeakReference<>(activity);          // 只写不读
  5. GLES3JNIView v = new GLES3JNIView(activity);   // 构造里就 setRenderer(this)
     display = v;                                   // 静态字段，全局唯一
  6. v.setTag("imgui_overlay_view");                // ← dispatchToChildren 靠它跳过自己
  7. v.setZOrderOnTop(true);                        // ← 覆盖层在窗口最上层
  8. v.getHolder().setFormat(-3);                   // -3 = PixelFormat.TRANSLUCENT
  9. v.setOnTouchListener(new ImGui$4(root));       // → handleTouchEvent(v, e, root)
 10. v.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));  // MATCH_PARENT x MATCH_PARENT
 11. root.addView(v);
 12. log("ImGui Hook: Successfully attached to " + activity.getClass().getName());
```

（第 2 步的 `findViewById` 结果被 `check-cast` 成 `FrameLayout`；
若宿主 Activity 的 content 不是 FrameLayout → `ClassCastException` → 被 catch 吞掉，
日志里只会出现一行 `ImGui Hook Error: ...`。）

### 4.3 四个关键参数的为什么

| 调用 | 值 | 为什么必须这样 |
|---|---|---|
| `setEGLConfigChooser(8,8,8,8,16,0)` | RGBA8888 + depth16 + stencil0 | 需要 **Alpha 通道**才能做出透明覆盖层；stencil 用不上所以给 0。 |
| `setEGLContextClientVersion(3)` | GLES 3 | ImGui 的 GL3 后端（`glBindVertexArray`/`glGenVertexArrays` 在 `.so` 导入表里）。 |
| `setZOrderOnTop(true)` | — | `SurfaceView` 默认在窗口 **下面**；不设这个，GL 画面会被游戏盖住。 |
| `getHolder().setFormat(-3)` | `TRANSLUCENT` | 不设的话 Surface 默认不透明，`glClearColor(0,0,0,0)` 清出来的透明区会变黑，直接糊住游戏。 |
| `LayoutParams(-1,-1)` | MATCH_PARENT × 2 | 覆盖层必须和 content 一样大，否则触摸命中矩形和可视区域对不上。 |
| `setTag("imgui_overlay_view")` | — | **回灌事件时跳过自己的唯一依据**（`dispatchToChildren`），不设会导致无限递归。 |

### 4.4 挂载后的视图树与触摸流向

```
DecorView
 └─ ... (系统栏等)
     └─ FrameLayout  android.R.id.content      ← 挂载点
         ├─ 游戏自己的 View（宿主 Activity 的 contentView）
         └─ GLES3JNIView   tag="imgui_overlay_view"   ← 最后 addView = 最上层 = 最先收到触摸
```

窗口把 `MotionEvent` 交给 content `FrameLayout` → FrameLayout **倒序**问子 View →
覆盖层（最后加的）先拿到 → `ImGui$4.onTouch` → `handleTouchEvent`。
返回 `true` 就到此为止（游戏看不到）；返回 `false` 则 FrameLayout 继续问游戏的 View。

`showDirectInputMethodFromNative` 用的 EditText 是挂到 **DecorView** 上的，
比 content 还高一层——这样输入法弹出时不会被 content 裁剪掉。

---

## 5. 触摸事件分发（重点）

### 5.1 `handleTouchEvent` 入口：先无条件喂 native

```java
private boolean handleTouchEvent(View view, MotionEvent e, ViewGroup root) {
    int actionMasked = e.getActionMasked();
    int actionIndex  = e.getActionIndex();
    int pointerCount = e.getPointerCount();

    int[]   ids      = new int[pointerCount];
    float[] xs       = new float[pointerCount];
    float[] ys       = new float[pointerCount];
    float[] pressures= new float[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
        ids[i] = e.getPointerId(i);  xs[i] = e.getX(i);
        ys[i]  = e.getY(i);          pressures[i] = e.getPressure(i);
    }

    // ★ 第一件事：原始多点数据无条件喂给 ImGui
    GLES3JNIView.nativeOnTouchEvent(actionMasked, actionIndex, ids, xs, ys, pressures, pointerCount);

    ... 按 actionMasked 分支 ...
}
```

**这一步是「ImGui 永远知道真相」的保证**：不管后面吃不吃、透不透传，
native 侧总是先拿到未经过滤的完整多点数据（含每个点的 id/x/y/pressure）。
后面的 `MotionEventClick` 只是给覆盖层补「按下/抬起」语义的**第二条通道**。

### 5.2 指针状态机

| 状态 | 类型 | 含义 |
|---|---|---|
| `pointerInImGui[id]` | `boolean[10]` | 该指**按下时**落在 ImGui 窗口矩形内 |
| `pointerInButton[id]` | `boolean[10]` | 该指**按下时**落在圆形悬浮按钮内 |
| `activeGamePointers` | `List<Integer>` | 要**透传给游戏**的指针 id，顺序即游戏侧的新 id |
| `gamePointerIdMap` | `Map<Integer,Integer>` | 逻辑上**死字段**（见下） |
| `gameDownTime` | `long` | 合成事件的 `downTime`，第一个 game 指针落下时打时间戳 |
| `imguiPointerCount` | `int` | 当前按在 ImGui 上的手指数 |

**判定只在 DOWN / POINTER_DOWN 那一刻做一次**，之后整个手势期间不再重算
（`pointerInImGui[id]` 就是缓存的判定结果）。这就是「手指从菜单上滑出去，
菜单仍然跟着拖」的原因——也是「手指从游戏滑进菜单，菜单不会被误触」的原因。

**`gamePointerIdMap` 是死代码。** 全 DEX 只有 4 处碰它，都在写：
`<init>` 建空 Map、`addGamePointer` 里 `put`、`removeGamePointer` 里 `remove`+`clear`+重建、
`ACTION_CANCEL` 分支里 `clear`。**没有任何一处 `get`**。
真正决定游戏侧 pointer id 的是 `activeGamePointers` 的**下标**
（`dispatchGameEvent` 里 `pointerPropertiesArr[i].id = i`）。
可以理解为：作者本来想用 map 做 id 重映射，最后改成用下标了，map 忘了删。
**移植时可以直接丢掉这个字段。**

`removeGamePointer` 里那个「先 `remove` 再 `clear` 再整表重建」的写法，
是在维护「id → 紧凑下标」的不变式；因为没人读，等同空操作。

### 5.3 命中测试（Java 侧，供状态机用）

```java
public static boolean isImGuiComponentTouched(float x, float y) {
    float[] b = nativeGetImGuiWindowBounds();          // 每 4 个 float 一个矩形
    if (b == null || b.length == 0) return false;
    for (int i = 0; i < b.length / 4; i++) {
        int o = i * 4;
        if (x >= b[o] && x <= b[o+2] && y >= b[o+1] && y <= b[o+3]) return true;
    }
    return false;
}
```
数组布局：`[x1, y1, x2, y2] × N`（左上 + 右下，闭区间）。支持多窗口。

```java
public static boolean isCircularButtonTouched(float x, float y) {
    float[] b = nativeGetCircularButtonBounds();
    if (b == null || b.length == 0) return false;
    int count = (int) b[0];                            // ★ b[0] 是数量
    for (int k = 0; k < count; k++) {
        int base = k * 3;
        if (base + 3 >= b.length) break;               // 越界保护
        float cx = b[base + 1], cy = b[base + 2], r = b[base + 3];
        float dx = x - cx, dy = y - cy;
        if (dx*dx + dy*dy <= r*r) return true;
    }
    return false;
}
```
数组布局：`b[0] = count`，之后每 3 个一组 `[cx, cy, r]`（圆心 + 半径，闭区间）。

注意 `GLES3JNIView.isImGuiComponentTouched(FF)Z` 这个 **native** 版本在 Java 侧
**从来没被调用过**——真正的命中测试走的是 `ImGui` 里的纯 Java 版本。
native 版本是给别的调用方（或更新版本代码）留的。

### 5.4 回灌游戏：合成事件

`dispatchGameEvent` / `dispatchGameMoveEvent` / `dispatchGameCancelEvent` 三个方法
套路完全一致：**按 `activeGamePointers` 重新组装一个只含游戏指针的 MotionEvent，
再 `dispatchToChildren` 灌进游戏视图树。**

共同的三步：

```java
int size = activeGamePointers.size();
if (size == 0) return;

PointerProperties[] props = new PointerProperties[size];
PointerCoords[]     cords = new PointerCoords[size];
for (int i = 0; i < size; i++) {
    int idx = findPointerIndex(e, activeGamePointers.get(i));   // 原始事件里的下标
    props[i] = new PointerProperties();
    cords[i] = new PointerCoords();
    if (idx >= 0) {
        e.getPointerProperties(idx, props[i]);   // 拷贝 toolType 等
        e.getPointerCoords(idx, cords[i]);       // 拷贝 x/y/pressure/size 等
    } else {
        // 该指针已不在本次事件里：造一个「静止」占位点
        props[i].id = i;  props[i].toolType = 1;         // 1 = FINGER
        cords[i].x = 0f;  cords[i].y = 0f;
        cords[i].pressure = 1f; cords[i].size = 1f;
    }
    props[i].id = i;      // ★ 一律重编号成 0..size-1（游戏只看到连续 id）
}

MotionEvent ev = MotionEvent.obtain(
        gameDownTime,          // downTime ← 我们自己记的
        e.getEventTime(),      // eventTime ← 原始
        outAction,             // action（含 pointer index 时是 (index<<8)|action）
        size, props, cords,
        e.getMetaState(), e.getButtonState(),
        e.getXPrecision(), e.getYPrecision(),
        e.getDeviceId(), e.getEdgeFlags(), e.getSource(), e.getFlags());
dispatchToChildren(root, ev);
ev.recycle();
```

`downTime` 用自己记的 `gameDownTime`（而不是原始事件的）是个关键细节：
游戏侧 `View`/`GestureDetector` 会校验 `downTime` 一致性，
游戏看到的那根手指在它眼里是「从 `gameDownTime` 开始按下的」，必须自洽。

三个方法的差别只有 action：

| 方法 | action | 备注 |
|---|---|---|
| `dispatchGameEvent` | 由入参算（0→ACTION_DOWN 或 (idx<<8)\|5；1→ACTION_UP；6→(idx<<8)\|6） | 见 §5.5 |
| `dispatchGameMoveEvent` | `2`（ACTION_MOVE） | 常量 |
| `dispatchGameCancelEvent` | `3`（ACTION_CANCEL） | 常量 |

### 5.5 ⚠️ `dispatchGameEvent`：JADX 失真修正

JADX 版（`ImGui.java` 第 411–436 行）读起来像这样：

```java
if (i4 == 0) {
    if (size == 1) { obtain(..., 0, ...);  dispatch; recycle; }   // ACTION_DOWN
    i4 = (indexOf << 8) | 5;
    obtain(..., i4, ...); dispatch; recycle;                       // 又发一次 POINTER_DOWN
}
if (i4 == 1) { obtain(..., 1, ...); dispatch; recycle; }
if (i4 == 6) { i4 = (indexOf << 8) | 6; }
obtain(..., i4, ...); dispatch; recycle;
```

这读起来是「action==0 时先发 DOWN 再**无条件**发 POINTER_DOWN」，
而且有 4 个 `obtain` 调用点。**这是 JADX 的控制流还原错误，不是真实逻辑。**

对照真实字节码（证据：[imgui-dex-disasm.txt](ArkReCodeHack/work/imgui-dex-disasm.txt) 中
`dispatchGameEvent` @00ee–@011c）：

```
@00ee  if-nez  v2        -> @0106      ; action != 0 → 跳
@00f2  if-ne   v9, v6    -> @00fa      ; size != 1 → 跳
@00f6  const/4 v8, 0                   ; size == 1 : action = 0   (ACTION_DOWN)
@00f8  goto             -> @011c
@00fa  shl-int/lit8 v2, v3, 8          ; v2 = indexOf << 8
@00fe  or-int/lit8  v2, v2, 5          ; v2 = (indexOf<<8)|5      (ACTION_POINTER_DOWN)
@0102  move    v8, v2
@0104  goto             -> @011c
@0106  if-ne   v2, v6    -> @010e      ; action != 1 → 跳
@010a  const/4 v8, 1                   ; action = 1               (ACTION_UP)
@010c  goto             -> @011c
@010e  const/4 v4, 6
@0110  if-ne   v2, v4    -> @0102      ; action != 6 → 跳（v8 = action 原样）
@0114  shl-int/lit8 v2, v3, 8
@0118  or-int/2addr v2, v4             ; v2 = (indexOf<<8)|6      (ACTION_POINTER_UP)
@011a  goto             -> @0102
@011c  iget-wide ...                   ; ← 四条分支全部汇合到这里
...                                    ;   统一装填参数
@0168  invoke-static/range ... obtain   ; ★ 全方法唯一一次 MotionEvent.obtain
@0174  dispatchToChildren + recycle
```

**真实语义**（严格 if/else，单个 `obtain`）：

```java
int outAction;
if (action == 0) {
    outAction = (size == 1) ? 0 : ((indexOf << 8) | 5);
} else if (action == 1) {
    outAction = 1;
} else if (action == 6) {
    outAction = (indexOf << 8) | 6;
} else {
    outAction = action;
}
// 然后唯一一次 obtain(gameDownTime, eventTime, outAction, size, ...) + dispatchToChildren + recycle
```

这个修正**很重要**，因为两者行为不同：

- JADX 版：第一根游戏手指会先收到 `ACTION_DOWN`，紧接着又收到一个
  `ACTION_POINTER_DOWN(index=0)` —— 游戏会看到「同一根手指按了两次」，多半直接错乱。
- 真实版：`size == 1` → 只发 `ACTION_DOWN`；`size > 1` → 只发
  `ACTION_POINTER_DOWN(index = 当前下标)`。这才是能正常玩的行为。

修正后的关键片段（在 §5.4 的公共三步之后）：

```java
int indexOf = activeGamePointers.indexOf(pointerId);
if (indexOf == -1 && action == 0) indexOf = size - 1;   // @0036–@003e
...
if (action == 0)      outAction = (size == 1) ? 0 : ((indexOf << 8) | 5);
else if (action == 1) outAction = 1;
else if (action == 6) outAction = (indexOf << 8) | 6;
else                  outAction = action;
```
（`indexOf == -1 && action == 0` 时用 `size-1` 兜底，是因为
`handleTouchEvent` 在 `addGamePointer` **之后**才调它，新指针必然已在列表里；
这个兜底是防御性的。）

### 5.6 `dispatchToChildren`：把事件塞进游戏视图树

```java
private void dispatchToChildren(ViewGroup root, MotionEvent e) {
    for (int i = root.getChildCount() - 1; i >= 0; i--) {   // ★ 倒序 = 从上到下
        View child = root.getChildAt(i);
        if (IMGUI_VIEW_TAG.equals(child.getTag())) continue; // ★ 跳过覆盖层自己
        try {
            e.getX(0); e.getY(0);                            // 结果被丢弃（疑似调试残留）
            if (child.getVisibility() == 0) {                // 0 = VISIBLE
                e.offsetLocation(-child.getLeft(), -child.getTop());  // 转成子视图局部坐标
                boolean handled = child.dispatchTouchEvent(e);
                e.offsetLocation(child.getLeft(), child.getTop());    // ★ 必须还原
                if (handled) return;
            }
        } catch (Exception ex) {
            Log.i(TAG, "ImGui Hook: dispatchToChildren error - " + ex.getMessage());
        }
    }
}
```

三个要点：

1. **倒序**：后加的在上面，从上往下试，语义和 Window 的分发一致。
2. **按 tag 跳过自己**——这是防无限递归的唯一手段。
   覆盖层是 content 的子 View，如果不跳过，
   `dispatchToChildren` 会把事件又送回 `handleTouchEvent`，直接栈溢出。
3. **`offsetLocation` 必须成对还原**：同一个 `MotionEvent` 对象会被后续子 View 继续用，
   不还原坐标系就串了。注意还原写在 `try` 里、`dispatchTouchEvent` 之后，
   如果 `dispatchTouchEvent` 抛异常，还原就被跳过（坐标残留），
   但异常被 catch 后循环继续——属于已知的边界瑕疵。

### 5.7 吃掉 vs 透传：完整判定表

返回 `true` = 覆盖层吃掉（游戏收不到原始事件）；`false` = FrameLayout 会继续把**原始事件**给游戏。

| `actionMasked` | 条件 | 副作用 | 返回 |
|---|---|---|---|
| `0` DOWN | 命中 ImGui 窗口 | `pointerInImGui=1`，`imguiPointerCount++`，`MotionEventClick(true,x,y)` | **true**（吃掉）|
| `0` DOWN | 只命中圆钮 | `pointerInButton=1`，`imguiPointerCount++` | **true** |
| `0` DOWN | 都没命中 | 无（**注意：不登记为 game pointer**）| **false**（透传）|
| `1` UP | `pointerInImGui[id]` | `MotionEventClick(false,x,y)`，`imguiPointerCount--` | **true** |
| `1` UP | `pointerInButton[id]` | `imguiPointerCount--` | **true** |
| `1` UP | 是 game pointer | 合成 UP 给游戏 + `removeGamePointer` | **false** |
| `1` UP | 都不是 | 无 | **false** |
| `2` MOVE | 逐指：`pointerInImGui[id]` → `MotionEventClick(true,x,y)`（拖拽）| — | `imguiPointerCount>0 \|\| !activeGamePointers.isEmpty()` |
| `2` MOVE | 有 game pointer → `dispatchGameMoveEvent` | — | 同上 |
| `3` CANCEL | 逐指发 `MotionEventClick(false,...)`，清 4 个布尔/计数 | 有 game pointer → `dispatchGameCancelEvent`；然后 `imguiPointerCount=0`、两个集合 `clear()`、`gameDownTime=0` | **false**（恒）|
| `5` POINTER_DOWN | 命中 ImGui 窗口 | `imguiPointerCount++` + `MotionEventClick(true)` | `imguiPointerCount>0 \|\| activeGamePointers.size()>0` |
| `5` POINTER_DOWN | 只命中圆钮 | `imguiPointerCount++` | 同上 |
| `5` POINTER_DOWN | 都没命中 | **`addGamePointer(id)`** + `dispatchGameEvent(e, 0, id, root)` | 同上（此时 size≥1，实际恒 true）|
| `6` POINTER_UP | `pointerInImGui[id]` | `MotionEventClick(false)`，`imguiPointerCount--` | `imguiPointerCount>0 \|\| !activeGamePointers.isEmpty()` |
| `6` POINTER_UP | `pointerInButton[id]` | `imguiPointerCount--` | 同上 |
| `6` POINTER_UP | 是 game pointer | `dispatchGameEvent(e, size==1 ? 1 : 6, id, root)` + `removeGamePointer` | 同上 |
| 其它 | — | 无 | **false** |

**一句话总结口径**：

- **落在 ImGui 窗口或圆钮上的手指 → 永远被吃掉**（DOWN/MOVE/UP 全程，判定只做一次）。
- **不在 ImGui 上的手指 → 走「合成事件」通道透传给游戏**，而不是放行原始事件：
  原始事件里混着 ImGui 的手指，必须先剔除 + 重编号才能给游戏。
- **ACTION_DOWN 是个例外**：第一根手指按下时如果没命中 ImGui，直接 `return false` 放行原事件，
  不进 `activeGamePointers`。这是因为此刻只有一个指针，原事件本来就是干净的，
  没必要合成。从第二根手指（POINTER_DOWN）开始才需要剔除 + 重编号。

### 5.8 典型时序：一指按菜单 + 一指玩游戏

这是外挂菜单的典型用法，也是这套状态机设计的目标场景。

```
① 手指 A 落在 ImGui 窗口  → action=0 DOWN
     pointerInImGui[A]=true, imguiPointerCount=1
     MotionEventClick(true, x, y)         // ImGui 收到按下
     return true                          // ★ 吃掉，游戏完全不知道 A 存在

② 手指 B 落在游戏区域      → action=5 POINTER_DOWN（A 还在按）
     不命中 ImGui → addGamePointer(B)     // activeGamePointers=[B]，size=1
                → gameDownTime = uptimeMillis()
                → dispatchGameEvent(e, 0, B, root)
                     size==1 → outAction = 0  → 合成 ACTION_DOWN，pointer id 重编号为 0
     return true

③ 手指移动                → action=2 MOVE
     遍历：pointerInImGui[A] → MotionEventClick(true, xA, yA)   // 拖菜单
     有 game pointer        → dispatchGameMoveEvent → 合成 ACTION_MOVE(id=0) 给游戏
     return true

④ 手指 B 抬起              → action=1 UP（B 是 action pointer）
     pointerInImGui[B]=false, pointerInButton[B]=false
     activeGamePointers 含 B → dispatchGameEvent(e, 1, B, root) → 合成 ACTION_UP
                             → removeGamePointer(B)
     imguiPointerCount==0 && activeGamePointers.isEmpty() → gameDownTime = 0
     return false                          // 放行原事件
```

游戏侧看到的是：**一根从 `gameDownTime` 开始、id 恒为 0、坐标就是 B 的真实坐标的手指**，
从头到尾完全自洽——菜单手指 A 对它不可见。

---

## 6. 输入法桥接：`showDirectInputMethodFromNative`

native 侧（ImGui 里的输入框被点中时）`CallStaticVoidMethod` 调进来，无参数。

### 6.1 守卫链

```java
public static void showDirectInputMethodFromNative() {
    try {
        GLES3JNIView v = display;
        if (v == null) return;                          // 还没挂载
        Context ctx = v.getContext();
        if (!(ctx instanceof Activity)) return;         // 不是 Activity 上下文
        final Activity activity = (Activity) ctx;
        if (activity.isFinishing()) return;             // 正在销毁
        activity.runOnUiThread(new ImGui$3(activity));  // 切主线程
    } catch (Exception e) { e.printStackTrace(); }
}
```

用 `display.getContext()` 反推 Activity，而不是用 `currentActivityRef`——
后者虽然存了 Activity 却只写不读，真正在用的时候反而绕了一圈拿 Context。
（`display` 是静态的，所以这个方法是 static 也能工作。）

### 6.2 UI 线程里做了什么（`ImGui$3.run`）

```java
EditText et = new EditText(activity);
et.setVisibility(0);                                       // VISIBLE
et.setLayoutParams(new ViewGroup.LayoutParams(1, 1));      // ★ 1×1 像素，几乎不可见

// 1) 按 selectedFilterType 回填现有文本，光标放到末尾
String init = "";
switch (selectedFilterType) {
    case 1: init = objectFilterText;     break;
    case 2: init = classFilterText;      break;
    case 3: init = objectNameFilterText; break;
    case 4: init = cardKeyText;          break;
    case 5: init = roleFilterText;       break;
    // 0（未选中）→ 保持空串
}
et.setText(init);
et.setSelection(init.length());

// 2) 挂到 DecorView（比 content 还高一层，避免被裁剪）
ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
decor.addView(et);

// 3) 每次文本变化 → 写回静态字段 + 推给 native
et.addTextChangedListener(new ImGui$3$1());

// 4) 抢焦点 + 拉起输入法
et.requestFocus();
InputMethodManager imm = (InputMethodManager) activity.getSystemService("input_method");
if (imm != null) imm.showSoftInput(et, 1);   // 1 = SHOW_IMPLICIT

// 5) 回车收起
et.setOnKeyListener(new ImGui$3$2());
```

`ImGui$3$1.onTextChanged` 的分发（**这就是「在系统输入法里打字 → ImGui 输入框里出现字」的完整通路**）：

```java
public void onTextChanged(CharSequence s, int a, int b, int c) {
    String text = s.toString();
    switch (ImGui.selectedFilterType) {
        case 1: ImGui.objectFilterText     = text; GLES3JNIView.nativeUpdateFilterText(1, text); return;
        case 2: ImGui.classFilterText      = text; GLES3JNIView.nativeUpdateFilterText(2, text); return;
        case 3: ImGui.objectNameFilterText = text; GLES3JNIView.nativeUpdateFilterText(3, text); break;
        case 4: ImGui.cardKeyText          = text; GLES3JNIView.nativeUpdateFilterText(4, text); break;
        case 5: ImGui.roleFilterText       = text; GLES3JNIView.nativeUpdateFilterText(5, text); break;
        default: return;   // selectedFilterType == 0 时什么都不做
    }
}
```

`ImGui$3$2.onKey`（收起）：

```java
public boolean onKey(View v, int code, KeyEvent ev) {
    if (code == 66 && ev.getAction() == 1) {          // 66 = KEYCODE_ENTER, 1 = ACTION_UP
        new Handler().postDelayed(new ImGui$3$2$1(), 0L);   // 延到下一帧再拆，避免在回调里改视图树
        return true;
    }
    return false;
}
// ImGui$3$2$1.run(): et.clearFocus(); decor.removeView(et);   → 输入法随之收起
```

### 6.3 为什么绕这么一大圈

ImGui 是纯 native 绘制的，没有 Android 文本输入控件，**拿不到系统输入法**。
所以做法是「借壳」：造一个 1×1 的隐形 `EditText` 骗过输入法框架，
让系统 IME 把按键投递给它，再用 `TextWatcher` 把结果抄回 native。
`1×1` 是为了让它存在但不影响画面，`addView` 到 DecorView 是为了不被打断。

注意：**没有做输入法的主动收起**（除了回车）。
如果 native 侧在别的时机想关掉，需要另外的入口。

---

## 7. 静态字段的用途

这 6 个静态字段是 **Java 与 native 之间共享的「过滤条件」寄存器**，
native 侧通过 `GetStaticFieldID` / `SetStaticFieldID` 直接读写。

| 字段 | `selectedFilterType` 编号 | native 侧对应 | 语义 |
|---|---|---|---|
| `objectFilterText` | **1** | `nativeUpdateFilterText(1, s)` | 对象（Object）列表的过滤关键字 |
| `classFilterText` | **2** | `nativeUpdateFilterText(2, s)` | IL2CPP 类名过滤 |
| `objectNameFilterText` | **3** | `nativeUpdateFilterText(3, s)` | 对象名过滤 |
| `cardKeyText` | **4** | `nativeUpdateFilterText(4, s)` | 卡密 / 授权码输入框 |
| `roleFilterText` | **5** | `nativeUpdateFilterText(5, s)` | 角色过滤 |
| `selectedFilterType` | — | `setSelectedFilterType(int)` | 当前正在编辑哪一个（1–5；0 = 无）|

工作方式：

1. 用户在 ImGui 界面里点某个过滤输入框 → native 调 `setSelectedFilterType(n)`。
2. native 调 `showDirectInputMethodFromNative()` → Java 弹出隐形 EditText，
   按 `n` 回填对应字段的现有内容（**这一步让用户能接着上次的输入继续改，
   而不是从头打字**）。
3. 用户打字 → `ImGui$3$1.onTextChanged` 同步写回该字段，并调 `nativeUpdateFilterText(n, text)`。
4. native 侧刷新列表。

`selectedFilterType` 是 6 个字段里**唯一没有 DEX 静态初值**的（`init_raw=None`，
即默认 0）——它靠 `setSelectedFilterType` 在运行时设。其余 5 个初值都是 `""`。

代码里这个 switch **写了两遍**（`showDirectInputMethodFromNative` 里回填一次，
`ImGui$3$1.onTextChanged` 里回写一次），编号含义必须保持一致，改一处就得改另一处。

---

## 8. `GLES3JNIView`：渲染链与 native 调用

### 8.1 继承关系

```
java.lang.Object
 └─ android.view.View
     └─ android.view.SurfaceView
         └─ android.opengl.GLSurfaceView     ← 提供 GL 线程 + EGL 管理
             └─ com.example.imgui.GLES3JNIView
                 implements GLSurfaceView.Renderer   ← 三个渲染回调
```

`GLSurfaceView` 自带一个独立的 **GL 渲染线程**，三个 `Renderer` 回调都在那个线程上跑，
**不在主线程**。所以 native 的 ImGui 代码天然运行在 GL 线程，
触摸事件（主线程）和渲染（GL 线程）之间的同步是 native 侧的责任。

### 8.2 构造：定下 EGL 配置

```java
public GLES3JNIView(Context context) {
    super(context);
    setEGLConfigChooser(8, 8, 8, 8, 16, 0);   // R8 G8 B8 A8, depth 16, stencil 0
    setEGLContextClientVersion(3);            // OpenGL ES 3.0
    setRenderer(this);                        // ★ 必须最后调：会启动 GL 线程
}
```

`setRenderer` 之后 GL 线程就起来了，会依次触发 `onSurfaceCreated` → `onSurfaceChanged` →
循环 `onDrawFrame`。

### 8.3 三个回调 + 生命周期

| 回调 | 调用时机 | 干了什么 | 调到哪个 native |
|---|---|---|---|
| `onSurfaceCreated(GL10, EGLConfig)` | GL 表面创建（含每次重建）| `glClearColor(0,0,0,0)`（全透明）+ `init(getHolder().getSurface())` | `Java_com_example_imgui_GLES3JNIView_init` |
| `onSurfaceChanged(GL10, w, h)` | 尺寸变化（含首次）| `glViewport(0,0,w,h)` + `resize(w,h)` | `Java_com_example_imgui_GLES3JNIView_resize` |
| `onDrawFrame(GL10)` | 每帧 | `glClear(16640)` + `step()` | `Java_com_example_imgui_GLES3JNIView_step` |
| `onDetachedFromWindow()` | View 从窗口摘除 | `super` + `imgui_Shutdown()` | `Java_com_example_imgui_GLES3JNIView_imgui_1Shutdown` |

细节：

- **`16640 = 0x4100 = GL_COLOR_BUFFER_BIT(0x4000) | GL_DEPTH_BUFFER_BIT(0x0100)`**。
  每帧先清成透明，再让 ImGui 画上去——这就是覆盖层「只显示菜单、其余地方看见游戏」的原理。
- **`glClearColor(0,0,0,0)` 的 alpha=0** 必须和 `setFormat(-3)`（TRANSLUCENT）配合，
  两者缺一，透明区就会变黑。
- **`init` 传的是 `android.view.Surface` 对象**（声明为 `Object`）。
  native 侧用 `ANativeWindow_fromSurface` 把 Surface 转成 `ANativeWindow*`，
  这样 ImGui 的 GL 后端就能直接在**这个** Surface 上渲染。
  `.so` 导入表里同时有 `ANativeWindow_fromSurface` 和整套 `glGenVertexArrays` /
  `glBindVertexArray` / `glUniformMatrix4fv` / `glScissor` 等，印证是 GLES3 + ImGui GL3 后端。
- **`onSurfaceCreated` 可能被调用多次**（锁屏、切后台、旋转屏幕都会重建 EGL 表面）。
  `init` 里应做幂等保护，否则会重复初始化 ImGui 上下文；这个保护在 native 侧，Java 侧不做。
- **`onDetachedFromWindow` 是唯一的清理点**：Activity 销毁 / `removeView` 时触发，
  通知 native 释放 ImGui 上下文。注意它**不等于** `onPause`——
  切后台时 GLSurfaceView 会暂停渲染但不会 detach，所以切回来不会重挂。

### 8.4 `fontData` 与 `updateHexInput`

- `static byte[] fontData`：Java 侧**只声明、从不读写**（反汇编确认无 `sget-object`/`sput-object`）。
  它是留给 native 的：native 用 `GetStaticFieldID(..., "fontData", "[B")` +
  `SetStaticObjectField` 把字体图集塞进来，或者反过来读。
  属于「双版本兼容钩子」——本版 Java 用不到，但 native 里可能还留着这条路径。
- `updateHexInput(String)`：native 声明存在、`.so` 导出存在（848 字节），
  但**本版 Java 代码里没有任何调用点**。同样属于更新版本 Java 代码的钩子
  （大概率和 §7 的过滤框是同一套机制，用于十六进制数值输入）。

---

## 9. 移植 / 复现清单

要自己重建这套覆盖层，按顺序需要：

1. **native 侧**
   - `JNI_OnLoad` 里 `RegisterNatives` **逐个**注册 §3 的 11 个方法（`getWindowRect`/`real` 排除）。
     签名务必用 `(Ljava/lang/Object;)V` 和 `Java_..._imgui_1Shutdown`。
   - 实现 `init(Surface)` → `ANativeWindow_fromSurface` → 初始化 ImGui GL3 后端。
   - 实现 `step()` → `ImGui::NewFrame / Render / SwapBuffers`。
   - 实现 `nativeGetImGuiWindowBounds()` 返回 `[x1,y1,x2,y2]×N`。
   - 实现 `nativeGetCircularButtonBounds()` 返回 `[count, cx,cy,r, cx,cy,r, ...]`。
   - 实现 `nativeOnTouchEvent(...)` → 喂 `ImGuiIO::AddMousePosEvent/AddMouseButtonEvent`。
   - 实现 `nativeUpdateFilterText(int, String)` / `updateHexInput(String)` / `MotionEventClick(bool,x,y)`。
   - 自己想清楚**什么时候**调 `setSelectedFilterType` 和 `showDirectInputMethodFromNative`。
2. **Java 侧**：`ImGui.java` + `GLES3JNIView.java` 可直接照抄。
   `gamePointerIdMap` / `currentActivityRef` 可以删掉（死字段）。
   `dispatchGameEvent` **按 §5.5 的修正版写**，别照抄 JADX 输出。
3. **启动**：`DexClassLoader` 加载 dex → 反射拿 Activity → 主线程调
   `setupImGuiViewOnMainThread(activity)`。参考 [loader.c](ArkReCodeHack/replicate/loader.c) 与
   [Boot.java](ArkReCodeHack/replicate/java/src/com/coldbrew/Boot.java)。

**已知坑清单**：

| # | 问题 | 说明 |
|---|---|---|
| 1 | `pointerInImGui` 只有 10 个槽 | 直接以 `getPointerId()` 为下标。Android 的 pointerId 可以到 31，**多指同时按下可能 `ArrayIndexOutOfBoundsException`**。加固版应加 `if (id < MAX_POINTERS)` 判断或用取模。 |
| 2 | `imguiPointerCount` 计数可能失衡 | 只在 DOWN/POINTER_DOWN 加、UP/POINTER_UP 减。若某个 UP 事件的 action pointer 不是当初那个（理论上不会，但输入设备异常时会），计数会漂移，导致后续一直「吃掉」触摸。 |
| 3 | ACTION_CANCEL 只清当前事件里的指针 | 循环只遍历 `pointerCount`，不在本次事件里的 pointer 的布尔标志会残留。 |
| 4 | `dispatchToChildren` 的 `offsetLocation` 还原在 try 内 | `dispatchTouchEvent` 抛异常时坐标不还原，影响后续子 View。 |
| 5 | `dispatchGameEvent` 必须先按 §5.5 修正 | 照抄 JADX 会给第一根游戏手指发两次按下。 |
| 6 | `removeGamePointer` 的 map 维护是空转 | 无读取点，可删。 |
| 7 | 挂载依赖 `android.R.id.content` 是 `FrameLayout` | 不是的话 `ClassCastException`，只会打一行日志，静默失败。 |
| 8 | `showDirectInputMethodFromNative` 无异常收起路径 | 只有回车会移除 EditText；native 侧若中途取消，1×1 的 EditText 会留在 DecorView 上（不可见，但焦点被它占着）。 |

---

## 10. 证据与核对方式

本文所有控制流结论都经过**逐条字节码核对**，不是照抄反编译输出：

- 用 `androguard` 解析 [libArkRe.injected.dex](ArkReCodeHack/build/dex/libArkRe.injected.dex)（16,136 B），
  对 `ImGui` / `GLES3JNIView` 的**全部方法**做反汇编，解析每条 `if-*` / `goto` 的目标地址，
  输出到 [imgui-dex-disasm.txt](ArkReCodeHack/work/imgui-dex-disasm.txt)（1,080 行）。
- 静态字段初值从 DEX `class_data.class_data_item` 的 `static_values` 读取（§2.1 表格）。
- native 符号与大小取自 [so-analysis.txt](ArkReCodeHack/work/so-analysis.txt) 第 240–253 行的导出表。
- 与 JADX 输出不一致的只有 `dispatchGameEvent` 一处（§5.5），
  其余方法（`handleTouchEvent`、`dispatchToChildren`、`addGamePointer`、`removeGamePointer`、
  `initPointerTracking`、两个命中测试、`setupImGuiViewInternal`、
  `setupImGuiViewOnMainThread`、`showDirectInputMethodFromNative`、
  `GLES3JNIView` 全部回调）逐条一致。
