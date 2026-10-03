# 还原工程 · 全量架构

对辅助软件 **Riruriru Mod 1.1.2** 的完整逆向还原，目标是**可重构**：
拿这份文档 + `src/` 下的源码，能重新编译出功能等价的软件。

---

## 一、软件构成总览

```
Riruriru Mod 1.1.2 (APK)
│
├─ host 层（Android App，Java）
│  ├─ com.Riruriru.Sx          8 个类   ← 业务逻辑（悬浮窗、注入器调用）
│  └─ irene.window.algui      28 个类   ← AlGui 悬浮窗框架（UI 底层）
│
├─ 注入器（assets/64）
│  └─ AArch64 ELF 可执行文件，14120 B   ← ptrace 向游戏进程注入 .so
│
└─ payload 层（assets/*.so，被注入游戏进程）
   ├─ libArkRe.so       7587080 B  ← Ark ReCode 专用（ImGui 菜单 + il2cpp 挂钩）
   │  └─ 内嵌 injected.dex (偏移 0x5265A0, 16136 B)
   │     └─ com.example.imgui.ImGui / GLES3JNIView  2 个类
   └─ libCherryTale.so  8446416 B  ← Cherry Tale 专用
      └─ 内嵌 injected.dex (偏移 0x5F80C0, 16136 B)
```

**关键点：host 负责"把东西送进去"，payload 负责"在游戏里干活"。**
两者通过注入器的命令行 + dlopen 衔接，没有 IPC。

---

## 二、运行链路

```
用户点悬浮窗「启动」
  │
  ├─ FloatContentView.startInjection()
  │    └─ 按 selectedGame 选 payload 与游戏包名
  │
  ├─ FloatContentView.executeInjection()
  │    ├─ Runtime.exec("chmod 777 " + binaryFile)
  │    └─ ProcessBuilder(binary, "-pkg", 游戏包名, "-lib", payload路径)
  │
  └─ assets/64 (注入器)
       ├─ su -c setenforce 0
       ├─ 从 /proc/<pid>/cmdline 找游戏进程
       ├─ ptrace ATTACH
       ├─ 远程 mmap + 写入 .so 路径
       ├─ 远程 dlopen(path, RTLD_NOW)
       └─ DETACH
            │
            └─ payload 在游戏进程内加载
                 ├─ ImGui 后端初始化（OpenGL ES 3）
                 ├─ 用 DexClassLoader 加载 injected.dex
                 ├─ RegisterNatives 绑 13 个 Java_com_example_imgui_*
                 └─ ImGui.setupImGuiViewOnMainThread(Activity)
                      └─ 建 GLES3JNIView 覆盖层 → addView 到 Activity
                           └─ 菜单渲染 + 触摸事件分发
```

---

## 三、各层技术要点

### 3.1 host 层

| 类 | 作用 |
| --- | --- |
| `MainActivity` | 入口，权限申请、加群跳转 |
| `FloatStartService` | 前台服务，拉起悬浮窗 |
| `FloatServiceView` | 悬浮球容器，读日志文件回显 |
| `FloatControlView` | 悬浮球本体（拖动、吸附、显隐动画） |
| `FloatContentView` | **核心面板**，解压 assets、起注入器、回显日志 |
| `Miscellaneous` | 工具方法（shell、加解密、跳转） |
| `Start` | 12 个功能占位（空壳） |
| `irene.*` | AlGui 框架，提供窗口/控件/工具层 |

**关键算法**：`可逆加密`/`可逆解密` 都是 `char ^ 't'`（异或 116）——对称的。

### 3.2 注入器（assets/64）

- AArch64 ELF，导出 `main`（652 字节）
- 导入 `ptrace` / `dlopen` / `mmap` / `system` / `waitpid` 等 39 个符号
- 三种命令行写法都支持：`-pkg X -lib Y` / `-pkg=X -lib=Y` / `-pid N -lib Y`
- 会执行 `su -c setenforce 0` 关掉整机 SELinux

详见 [module-injector.md](module-injector.md)。

### 3.3 payload 层

- AArch64 共享库，导出 13 个 `Java_com_example_imgui_*`
- 内部名 `Tohka MOD` / `Tohka Custom MOD v2.0`
- 引用 345 处 `il2cpp_*` 符号（251 个不同 API）
- 内嵌 DEX 提供 ImGui 的 Java 侧
- 有卡密验证（本地状态机 + 可选服务端）

详见 [module-native-payload.md](module-native-payload.md)。

### 3.4 ImGui 覆盖层

- `GLES3JNIView` 继承 `GLSurfaceView`，自带 Renderer
- 挂到 Activity 的 `android.R.id.content`（FrameLayout）上
- `setZOrderOnTop(true)` + `setFormat(-3)`（TRANSLUCENT）
- 多点触控分流：ImGui 区域内的触摸被吃掉，区域外透传给游戏

详见 [module-imgui-overlay.md](module-imgui-overlay.md)。

---

## 四、可重构性说明

### 已经可以重建的部分

| 组件 | 还原程度 | 依据 |
| --- | --- | --- |
| host 层 8 个业务类 | **源码级** | jadx 反编译，仅方法名重命名 |
| AlGui 框架 28 个类 | **源码级** | 同上 |
| injected.dex 2 个类 | **源码级** | 同上 |
| 注入器 | **源码级** | 已用 C 复刻并实测可用 |
| 资源 | **完整** | res-final 下全量提取 |
| Manifest | **完整** | 可解码重建 |

### 只能做到结构级的部分

| 组件 | 限制 |
| --- | --- |
| `libArkRe.so` / `libCherryTale.so` | 7.6/8.4 MB 的优化后 AArch64 机器码，无法还原成原始 C++ 源码。但**功能面、菜单结构、il2cpp API 用法、字符串表**已完整还原，可据此重写等价实现 |

### 重建的策略

1. **host + 注入器 + dex**：直接编译还原出的源码即可
2. **payload**：按功能矩阵（[module-native-payload.md](module-native-payload.md)）重写，
   或直接复用原 .so（原 .so 与 host 之间是松耦合的，靠命令行参数衔接）

---

## 五、文档索引

见 [_INDEX.md](_INDEX.md)。

---

## 六、复现环境（实测）

```
宿主：Windows + MuMu 模拟器 15.0
设备：Android 15 (SDK 35)，KernelSU 3.2.5
架构：aarch64 内核 / x86_64 userspace / houdini 翻译 ARM 游戏
```

**最容易踩的坑**：`uname -m` 显示 `aarch64` 是假的 —— 看 `/proc/cpuinfo`（Intel）和
`/system/bin/sh` 的 ELF 头（x86_64）才准。这决定了注入器要编 x86_64、payload 要编 AArch64。
