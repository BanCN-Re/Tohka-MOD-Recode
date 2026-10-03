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

| 角色 | 说明 |
| --- | --- |
| 逆向还原 | [**BanCN**](https://github.com/BanCN-Re) |
| 仓库 | [BanCN-Re/Tohka-MOD-Recode](https://github.com/BanCN-Re/Tohka-MOD-Recode) |
| 协议 | MIT（仅覆盖还原成果，见 [LICENSE](LICENSE)） |

### 还原工作内容

- **反编译**：jadx 1.5.0 还原 host APK（36 个自有类）与内嵌 injected.dex（2 个类）
- **交叉验证**：自研 smali→Java 还原器（`tools/dalvik2java.py`）独立复现，与 jadx 结果比对
- **汇编级分析**：capstone 5.0.7 逆向注入器、卡密验证状态机、载荷关键函数
- **资源还原**：843 个资源文件、AndroidManifest 完整解码、二进制 AXML 转文本
- **接口契约**：还原两个 native 载荷的 13 个 JNI 导出、251 个 il2cpp API 用法
- **可编译工程**：搭出 Gradle 工程并修复 jadx 还原瑕疵，构建出可安装 APK
- **文档**：12 份文档、约 4000 行

---

## 第三方组件

本项目在还原与重建过程中使用了以下开源工具与库，在此致谢：

### 逆向工具

| 工具 | 版本 | 用途 | 协议 |
| --- | --- | --- | --- |
| [jadx](https://github.com/skylot/jadx) | 1.5.0 | DEX → Java 反编译 | Apache-2.0 |
| [androguard](https://github.com/androguard/androguard) | 4.1.4 | DEX/AXML 解析 | Apache-2.0 |
| [Capstone](https://www.capstone-engine.org/) | 5.0.7 | AArch64 反汇编 | BSD-3 |
| [apktool](https://apktool.org/) 生态 | — | 资源处理参考 | Apache-2.0 |

### 重建工程依赖

| 库 | 版本 | 用途 |
| --- | --- | --- |
| AndroidX AppCompat | 1.6.1 | 基础兼容层 |
| AndroidX ConstraintLayout | 2.1.4 | 布局 |
| AndroidX RecyclerView | 1.3.2 | 列表 |
| Material Components | 1.11.0 | Material 控件 |
| AndroidX CardView | 1.0.0 | 卡片 |

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
