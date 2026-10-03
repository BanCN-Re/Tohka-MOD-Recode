# 作者与致谢 · CREDITS

## 原始作品

### Tohka MOD（Riruriru Mod）

| 项 | 内容 |
| --- | --- |
| 名称 | Riruriru Mod / Tohka MOD / Tohka Custom MOD |
| 版本 | 1.1.2 |
| 作者 | 哔哩哔哩 **My-血-233** |
| 包名 | `com.Riruriru.Sx` |
| 内部标识 | `Tohka MOD` / `Tohka Custom MOD v2.0` / `MyGame MOD v1.0` |
| 售后群 | QQ 群 938552503（原作者所有，与本项目无关） |
| 卡密购买 | 原 MOD 主界面「购买卡密」按钮（与本项目无关） |

**本项目与原作者没有任何关联。** 原始 MOD 的著作权归作者所有。

### AlGui 悬浮窗框架

| 项 | 内容 |
| --- | --- |
| 名称 | AlGui（艾琳 GUI） |
| 包名 | `irene.window.algui` |
| 作者 | **艾琳**（irene） |
| 作用 | 原 MOD 的悬浮窗 UI 底层框架，28 个类 |
| 组成 | AlGui 主类、预制菜单、气泡通知、数据模型、10 个工具类、4 个自定义 View |

这是原 MOD 使用的第三方框架，**不是**原作者 My-血-233 写的。
本项目对其做了源码级还原，著作权归艾琳所有。

---

## 本项目

| 角色 | 承担者 | 工作内容 |
| --- | --- | --- |
| 逆向 | **GLM 5.3** | DEX / AXML 拆解、native 层汇编分析、卡密状态机定位、注入器行为还原、载荷接口提取 |
| 文档整理与推断 | **Opus 5.5** | 把逆向结果整理成模块文档、推断 jadx 失真点的成因、设计修复策略、撰写架构说明 |
| 还原实现 | **DeepSeek V4.1 Flash** | 源码级还原落地、编写还原工具链、修复全部编译错误、搭可编译工程、构建验证、推送发布 |
| 仓库 | [BanCN-Re/Tohka-MOD-Recode](https://github.com/BanCN-Re/Tohka-MOD-Recode) | |
| 协议 | MIT（仅覆盖还原成果，见 [LICENSE](LICENSE)） | |

### 各环节产出

**GLM 5.3 —— 逆向**
- 用 jadx 1.5.0 + androguard 4.1.4 拆出 host APK 的 36 个自有类与内嵌 injected.dex
- capstone 5.0.7 反汇编 AArch64 载荷，定位卡密授权状态变量
  （`libArkRe.so` 偏移 `0x740568` / `libCherryTale.so` 偏移 `0x812210`）
- 还原 assets/64 注入器的完整行为（112 条字符串、39 个导入符号、三种命令行写法）
- 提取两个载荷的 251 个 il2cpp API 与 13 个 JNI 导出

**Opus 5.5 —— 文档与推断**
- 把逆向产出整理成 6 份模块文档（host 业务 / AlGui 框架 / ImGui 覆盖层 /
  native 载荷 / 注入器 / 资源与清单）
- **推断 jadx 失真点的成因**：`--no-inline-anonymous` 会把匿名类提升成成员类，
  而默认模式会错误地给匿名类补构造参数 —— 这是 260 个编译错误的根源
- 设计「复用原 APK 已编译资源」的构建路线（绕开 aapt2 的参数长度限制）
- 撰写 ARCHITECTURE.md / VERIFY.md / 接口契约

**DeepSeek V4.1 Flash —— 还原实现**
- 搭出完整还原工程（源码树 / 资源 / 清单 / 构建脚本）
- 编写还原与修复工具链（22 个脚本），修复全部编译错误至 0
- 解决构建链上的全部工程问题：
  - aapt2 参数超 Windows 命令行上限 → 改为复用原 APK 的 `resources.arsc`
  - `R.java` 无法用 aapt2 link 生成 → 从资源表反向生成
  - AndroidX 的 `R$*` 类缺失 → 从 jadx 产物提取并合并成嵌套类
  - d8 的类型冲突 → 按类路径去重
- 构建出可安装运行的 APK 并在设备上实测通过

---

## 第三方组件

### 逆向工具

| 工具 | 版本 | 用途 | 协议 |
| --- | --- | --- | --- |
| [jadx](https://github.com/skylot/jadx) | 1.5.0 | DEX → Java 反编译 | Apache-2.0 |
| [androguard](https://github.com/androguard/androguard) | 4.1.4 | DEX/AXML 解析 | Apache-2.0 |
| [Capstone](https://www.capstone-engine.org/) | 5.0.7 | AArch64 反汇编 | BSD-3 |

### 重建工程依赖

| 库 | 版本 |
| --- | --- |
| AndroidX AppCompat | 1.6.1 |
| AndroidX ConstraintLayout | 2.1.4 |
| AndroidX RecyclerView | 1.3.2 |
| AndroidX Concurrent Futures | 1.1.0 |
| Material Components | 1.11.0 |
| AndroidX CardView | 1.0.0 |
| Guava ListenableFuture | 1.0 |
| Kotlin Stdlib | 1.9.0 |

### 载荷内部使用的组件（从字符串与符号还原）

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| [Dear ImGui](https://github.com/ocornut/imgui) | 1.90.0 | 菜单 UI 框架 |
| OpenSSL / BoringSSL | — | 加密（静态链接） |
| Unity IL2CPP | — | 目标游戏的运行时 |

---

## 目标游戏

| 游戏 | 包名 | 权利人 |
| --- | --- | --- |
| Ark ReCode | `com.nerversoft.ark.recode` | 游戏厂商 |
| Cherry Tale | `com.neversoft.rpg.erolabs` | 游戏厂商 |

本项目**不包含**任何游戏本体内容、素材或资源，也**不主张**对游戏的任何权利。

---

## 特别说明

本项目是**独立的技术研究**，与上述任何作者、厂商**均无关联、合作或授权关系**。

如果任何权利人认为本项目侵犯了其权益，请通过 [Issues](../../issues) 联系，
核实后将**立即删除**相关内容。
