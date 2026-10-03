# libArkRe.so / libCherryTale.so 接口契约

两个 payload 无法还原成原始 C++ 源码（7.6/8.4 MB 优化后 AArch64 机器码），
但**对外接口与行为已完整还原**。这份契约就是"可重构"的依据：
照着它重写一份等价实现，替换掉原 .so 即可。

---

## 一、载荷定位

| | libArkRe.so | libCherryTale.so |
| --- | --- | --- |
| 大小 | 7,587,080 B | 8,446,416 B |
| 架构 | AArch64 (EM_AARCH64) | 同 |
| 类型 | ET_DYN（共享库） | 同 |
| 内部名 | `Tohka MOD` / `Tohka Custom MOD v2.0` | `MyGame MOD v1.0` |
| 目标游戏 | `com.nerversoft.ark.recode` | `com.neversoft.rpg.erolabs` |
| 内嵌 dex 偏移 | 0x5265A0 | 0x5F80C0 |
| 内嵌 dex 大小 | 16,136 B | 16,136 B |
| 导出 Java_* | 13 个 | 13 个（同名） |

两者的 **dex 部分完全相同**（同 16,136 字节、282 字符串、9 个类），
差异在 native 侧的 il2cpp 挂钩与菜单项。

---

## 二、必须导出的符号（13 个，两个 payload 一致）

这是 JNI 层的契约。**注意：没有 `JNI_OnLoad`** —— 靠 JNI 自动绑定或
调用方手动 `RegisterNatives`。

### 2.1 GLES3JNIView（渲染回调，9 个）

```c
// Java 侧签名 → native 符号名
void    Java_com_example_imgui_GLES3JNIView_init(JNIEnv*, jobject, jobject surface);
void    Java_com_example_imgui_GLES3JNIView_resize(JNIEnv*, jobject, jint w, jint h);
void    Java_com_example_imgui_GLES3JNIView_step(JNIEnv*, jobject);
void    Java_com_example_imgui_GLES3JNIView_imgui_1Shutdown(JNIEnv*, jobject);
void    Java_com_example_imgui_GLES3JNIView_MotionEventClick(JNIEnv*, jobject, jboolean, jfloat, jfloat);
void    Java_com_example_imgui_GLES3JNIView_nativeOnTouchEvent(JNIEnv*, jobject,
            jint action, jint pointerCount, jintArray ids,
            jfloatArray xs, jfloatArray ys, jfloatArray pressures, jint);
void    Java_com_example_imgui_GLES3JNIView_nativeUpdateFilterText(JNIEnv*, jobject, jint, jstring);
void    Java_com_example_imgui_GLES3JNIView_updateHexInput(JNIEnv*, jobject, jstring);
jboolean Java_com_example_imgui_GLES3JNIView_isImGuiComponentTouched(JNIEnv*, jobject, jfloat, jfloat);

// .so 里还有但 dex 无声明（不需要实现）：
//   Java_com_example_imgui_GLES3JNIView_getWindowRect   返回 jintArray
//   Java_com_example_imgui_GLES3JNIView_real            返回 void
```

`imgui_1Shutdown` 里的 `_1` 是 JNI 对 `_` 的转义，对应 Java 方法名 `imgui_Shutdown`。

### 2.2 ImGui（菜单查询，2 个）

```c
jfloatArray Java_com_example_imgui_ImGui_nativeGetImGuiWindowBounds(JNIEnv*, jclass);
jfloatArray Java_com_example_imgui_ImGui_nativeGetCircularButtonBounds(JNIEnv*, jclass);
```

返回 `float[4]`（x, y, w, h），供 Java 侧判断触摸点是否落在菜单/悬浮按钮上。

### 2.3 其余 2 个

```
Java_com_example_imgui_GLES3JNIView_getWindowRect    (jintArray)
Java_com_example_imgui_GLES3JNIView_real             (void)
```

这两个是 .so 里的历史遗留导出，dex 里没有对应 native 声明，
注册时会失败（`NoSuchMethodError`），**重构时可以省略**。

---

## 三、运行时依赖的 Java 类（由调用方提供）

payload 不自己注册 JNI，而是假定宿主提供了这两个类：

```java
package com.example.imgui;

public class ImGui {
    public static void setupImGuiViewOnMainThread(Activity activity);   // 入口
    public static void showDirectInputMethodFromNative();               // 输入法桥
    public static void setSelectedFilterType(int type);
    public static boolean isImGuiComponentTouched(float x, float y);
    public static boolean isCircularButtonTouched(float x, float y);
    // 静态字段：cardKeyText / classFilterText / objectFilterText
    //          objectNameFilterText / roleFilterText / selectedFilterType
}

public class GLES3JNIView extends GLSurfaceView implements Renderer {
    // onSurfaceCreated / onSurfaceChanged / onDrawFrame 里回调 native
}
```

完整源码见 `injected-dex/src/com/example/imgui/`。

**调用序列**（宿主职责）：
```
1. dlopen(payload.so)
2. DexClassLoader 加载 payload 内嵌的 injected.dex
3. RegisterNatives 把 13 个 Java_* 绑到上面两个类
4. ImGui.setupImGuiViewOnMainThread(activity)
   → 建 GLES3JNIView → addView 到 Activity 的 android.R.id.content
   → GL 线程回调 native 的 init/resize/step
```

**第 3 步是关键**：原 mod 靠 `System.loadLibrary` 让 linker 自动绑定，
我们用 `dlopen` 时必须自己 `RegisterNatives`，否则菜单不显示。

---

## 四、native 侧功能面（照此重写）

### 4.1 依赖的 il2cpp API（251 个）

payload 通过 `dlopen("libil2cpp.so") + dlsym` 拿到这些 API，用于读写游戏对象。
完整清单见 [module-native-payload.md](module-native-payload.md)。核心分组：

| 分组 | 代表 API | 用途 |
| --- | --- | --- |
| 类与类型 | `il2cpp_class_from_name` / `il2cpp_class_get_type` / `il2cpp_type_get_name` | 按名字找游戏类 |
| 字段读写 | `il2cpp_field_static_get_value` / `il2cpp_field_static_set_value` / `il2cpp_field_get_offset` | 改数值 |
| 对象操作 | `il2cpp_object_new` / `il2cpp_object_unbox` / `il2cpp_value_box` | 造对象 |
| 方法调用 | `il2cpp_runtime_invoke` / `il2cpp_class_get_method_from_name` | 调游戏方法 |
| 字符串 | `il2cpp_string_new` / `il2cpp_string_chars` / `il2cpp_string_length` | 传字符串 |
| 线程 | `il2cpp_thread_attach` / `il2cpp_thread_current` / `il2cpp_thread_detach` | 挂到 GC 线程 |
| GC | `il2cpp_gc_disable` / `il2cpp_stop_gc_world` / `il2cpp_gc_alloc_fixed` | 控 GC |
| 场景 | `il2cpp_domain_get_scenes` / `il2cpp_scene_get_root_count` | 遍历场景对象 |
| 数组 | `il2cpp_array_new` / `il2cpp_array_length` | 数组操作 |

### 4.2 内存写入

payload 有 **代码补丁能力**（用于改游戏函数），从字符串证据看：
```
il2cpp_write_memory
allow_code_patch=true
allow_code_patch
```

### 4.3 MCP 服务（可选组件）

payload 内建一个 HTTP 服务，把 il2cpp 反射能力包装成 JSON-RPC：

```
配置文件：<外部存储>/McpConfig.txt
协议：Streamable HTTP
鉴权：Authorization: Bearer <token>

暴露的方法（从字符串还原）：
  il2cpp_status
  il2cpp_list_classes
  il2cpp_class_info(class="...")
  il2cpp_list_scene_classes
  il2cpp_find_objects(class="...")
  il2cpp_object_info(object="o1", include_fields=true)
  il2cpp_call_method
  il2cpp_runtime_invoke
  il2cpp_get_field / il2cpp_call_method
  il2cpp_stop_gc_world / il2cpp_start_gc_world
  il2cpp_type_get_object / Type.GetType

参数类型 DSL：
  auto / hex / bytes / string / il2cpp_string / u8 / i8 / u16 / i16
  u32 / i32 / u64 / i64 / f32 / f64 / ptr / bool
```

### 4.4 卡密验证

```
配置：<应用私有目录>/files/AuthConfig.txt
字段：kill_on_expire / timeout_sec / title / notice
     （host / appid / appkey / rc4key 由程序内置，不落盘）

运行时状态变量（模块内偏移）：
  libArkRe.so       0x740568
  libCherryTale.so  0x812210
  值：0=未验证  1=已提交  2=通过  3=失败
```

详见 [../ArkReCodeHack/docs/auth-bypass.md](../../docs/auth-bypass.md)。

### 4.5 菜单结构

```
Tohka MOD
├─ AIMBOT              自瞄（垂直偏移、开关、目标选择）
├─ ESP / WALLHACK      方框/骨骼/连线/名字/距离/3D框
├─ GAME FEATURES       冷却/SP消耗/星级/灵魂/技能属性/跳跃/无服务端校验
├─ ROLE                角色批量/刷新/撤销/交换/筛选
├─ OBJECT BROWSER      对象浏览器（类名过滤/对象过滤）
├─ CARD KEY            卡密验证
├─ LOVE TALK / STORY   剧情解锁/全开/全关/体力
├─ AI / MCP            MCP 服务
├─ Custom Camera       自定义相机 / Alpha Bar
└─ DUMP                转储 / 获取全部程序集
```

配置持久化：`/data/data/<包名>/files/UnityHookEsp.txt`

---

## 五、重写建议

### 方案 A：复用原 .so（最省事）

payload 与 host 是**命令行松耦合**（`-pkg X -lib Y`），
只要保持接口不变，直接复用原文件即可。这是当前实际采用的方式。

### 方案 B：重写等价实现

照上面的契约重写，需要：

1. **ImGui 后端**：Dear ImGui 1.90.0 + OpenGL ES 3 后端（`imgui_impl_opengl3`）
2. **JNI 层**：实现 11 个有效导出（13 个减去 2 个遗留）
3. **il2cpp 桥**：`dlopen("libil2cpp.so")` + 按需 `dlsym` 那 251 个 API
4. **菜单层**：按 4.5 的结构实现各板块（这是工作量最大的部分）
5. **dex 层**：直接复用还原出的 `injected-dex/src/`（已编译验证通过）

### 方案 C：只重写 dex 层，native 复用

如果只想改菜单外观/交互，改 `injected-dex/src/` 然后重新编译 dex，
native 侧保持原样。这是改动成本最低的组合。

---

## 六、数据来源

| 内容 | 来源 |
| --- | --- |
| 导出符号 | `work/so-analysis.txt`（节表 + dynsym） |
| JNI 签名 | 运行时错误日志（`NoSuchMethodError` 会列出 dex 真实声明） |
| il2cpp API | `work/libArkRe-strings.txt` grep `il2cpp_` |
| 菜单结构 | 字符串邻接分析（`work/menu-region.txt`） |
| MCP 接口 | 字符串 + JSON 片段 |
| 卡密状态机 | capstone 反汇编（`docs/auth-*.md`） |
