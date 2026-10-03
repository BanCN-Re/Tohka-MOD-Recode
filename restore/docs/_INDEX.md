# 还原工程 · 文档索引

对辅助软件 **Riruriru Mod 1.1.2** 的全量逆向还原产出。

> 本目录是**逆向还原（restore）**阶段的模块文档，面向「照着重写一份」的需求，
> 与仓库根 `docs/`（**分析取证**阶段的原始报告）分工不同：
> 根 `docs/` 记录「我们查到了什么」，`restore/docs/` 记录「这块该怎么重写」。

工程根：`D:\Work\MuMuP\ArkReCodeHack\`

---

## 先读这两份

| 文档 | 内容 |
| --- | --- |
| [../README.md](../README.md) | 工程总览：成果表、目录结构、构建方式 |
| [../ARCHITECTURE.md](../ARCHITECTURE.md) | 全量架构：三层构成、运行链路、各层技术要点 |

---

## 模块文档（6 份，全部完成）

| 文档 | 覆盖 | 规模 |
| --- | --- | --- |
| [module-host-business.md](module-host-business.md) | `com.Riruriru.Sx` 7 个业务类逐方法还原、注入链、全部硬编码常量、jadx 失真点 | 12.8 KB |
| [module-algui.md](module-algui.md) | `irene.window.algui` 28 个类的 AlGui 悬浮窗框架（API 面、数据模型、菜单系统、工具层） | 60.4 KB / 927 行 |
| [module-imgui-overlay.md](module-imgui-overlay.md) | injected.dex 的 ImGui 覆盖层（触摸分发给重点、视图挂载流程、输入法桥接） | 46.6 KB / 818 行 |
| [module-native-payload.md](module-native-payload.md) | 两个 .so 的功能矩阵、251 个 il2cpp API、菜单结构树、MCP 接口、卡密字段 | 54.8 KB / 1072 行 |
| [module-injector.md](module-injector.md) | assets/64 的完整行为、112 条字符串、39 个导入符号、与复刻的差异、编译坑 | 8.8 KB |
| [module-resources.md](module-resources.md) | 资源清单、AndroidManifest 完整还原、重建 APK 需要什么 | 7.5 KB |

---

## 接口契约

| 文档 | 内容 |
| --- | --- |
| [../payload/INTERFACE.md](../payload/INTERFACE.md) | **payload 的对外契约** —— 13 个必须导出的 JNI 符号、运行时依赖的 Java 类、调用序列、il2cpp API 分组、菜单结构。照着它可重写等价实现 |

---

## 验证报告

| 文档 | 内容 |
| --- | --- |
| [../VERIFY.md](../VERIFY.md) | 验收报告：逐项完成度与证据（编译验证、语法验证、实测输出） |
| [../VERIFY-DIFF.md](../VERIFY-DIFF.md) | 对拍验证：还原源码 vs 原始 dex 的字符串比对（59/59 命中、关键常量 27/27 一致） |

---

## 源码与可编译工程

| 位置 | 内容 |
| --- | --- |
| `../host-apk/src/main/` | **可编译的 Gradle 工程**（878 文件） |
| `../host-apk/src/main/java/com/Riruriru/Sx/` | 8 个业务类（源码级还原） |
| `../host-apk/src/main/java/irene/` | 28 个 AlGui 框架类 |
| `../injected-dex/src/` | 2 个 ImGui 覆盖层类（**完整编译通过**） |
| `../injector/src/` | 注入器 C 源码（复刻 + 实测可用） |

---

## 分析产出（原始数据）

| 位置 | 内容 |
| --- | --- |
| `../host-apk/jadx/` | jadx 1.5.0 反编译输出 |
| `../host-apk/disasm/` | 自研反汇编器输出（交叉验证用） |
| `../host-apk/res-final/` | 资源提取结果（assets / images / layout / manifest / arsc） |
| `../../work/libArkRe-strings.txt` | libArkRe.so 全量字符串（66,342 条） |
| `../../work/libCherryTale-strings.txt` | libCherryTale.so 全量字符串（73,097 条） |
| `../../work/menu-region.txt` | 菜单字符串区（按偏移排列） |
| `../../work/so-analysis.txt` | 三个 .so 的节表 / 导出 / 导入 |

---

## 工具脚本（`../tools/`，22 个）

关键几个：

| 脚本 | 作用 |
| --- | --- |
| `dump_host_disasm.py` | 反汇编 host APK 的 dex |
| `dalvik2java.py` | **自研 smali → Java 还原器**（jadx 失败时的备份路线） |
| `carve_dex.py` | 从 .so 里提取内嵌的 injected.dex |
| `make_buildable.py` | 整理成 Gradle 标准布局 |
| `make_dep_stubs.py` | 从原 APK 生成 AndroidX 依赖桩（仅编译校验用） |
| `verify_diff.py` | 对拍验证（源码 vs 原始 dex） |
| `find_authvar.py` | 定位卡密授权状态变量偏移 |
| `disasm_authcheck.py` | 反汇编卡密判定函数 |

---

## 阅读顺序建议

| 目标 | 顺序 |
| --- | --- |
| 了解这是什么软件 | ARCHITECTURE.md → module-host-business.md |
| 重建 host APK | module-host-business.md → module-resources.md → 编译 `host-apk/src/main/` |
| 改菜单 | module-imgui-overlay.md → module-algui.md → 改 `injected-dex/src/` 重编 dex |
| 重写 payload | payload/INTERFACE.md → module-native-payload.md |
| 理解注入机制 | module-injector.md → `../../docs/injection-findings.md`（实测踩坑记录） |
