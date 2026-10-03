# Native Payload 架构与功能面还原

对象：

| 文件 | 大小 | 架构 | 专用游戏 |
| --- | --- | --- | --- |
| `work/assets/libArkRe.so` | 7,587,080 B | AArch64 ELF (DYN) | Ark ReCode |
| `work/assets/libCherryTale.so` | 8,446,416 B | AArch64 ELF (DYN) | Cherry Tale（樱景物语） |

方法：**只做字符串与结构还原，不反汇编机器码**。结论来自
`work/libArkRe-strings.txt`（66,342 条）与 `work/libCherryTale-strings.txt`（73,097 条），
以及直接用正则扫 `.rodata` / `.dynstr` / `.data` 原始字节。复现脚本在 `restore/tools/np_*.py`。

---

## ⚠️ 0. 对既有字符串表的重要修正

**`work/*-strings.txt` 是 ASCII-only 导出，把全部中文 UI 文案丢掉了。**

实测：这两个 txt 里 `ord(c) > 127` 的行数 = **0**。但直接扫 `.rodata` 原始字节后，
用 UTF-8 解码可以恢复出**大量中文界面文案**：

| payload | `.rodata` 中 CJK 串（去重） | 真实界面文案 | 尾部伪汉字噪声 |
| --- | --- | --- | --- |
| `libArkRe.so` | 918 | **720**（`< 0x74000`） | 198 |
| `libCherryTale.so` | 844 | **646**（`< 0x100000`） | 198 |
| **两边共有的界面文案** | — | **574** | — |

> 边界很干净：ARK 最后一条真实文案在 `0x7322D`，噪声从 `0x7402C` 开始；
> CHE 最后一条在 `0xFF83F`，噪声从 `0x100610` 开始。两边的噪声串**完全相同**
> （`F噄`、`EH袀`、`噀ba4`、`痤z`…），是 `.rodata` 尾部的压缩/加密数据被
> UTF-8 解码撞出来的，不参与统计。
>
> 差异：ARK 独有 146 条，CHE 独有 72 条（清单见 §6.1）。

也就是说：**界面是中文的**，之前的报告（含 `docs/reverse-report.md` 的菜单树）
只看到了英文配置键 / 板块名，漏掉了中文标题。本文档的菜单树已按中文重做。

复现：

```powershell
& $py restore\tools\np_cjk.py       # 列出全部非 ASCII 串（含所在节与偏移）
& $py restore\tools\np_cjkmap.py ark # 按偏移列出 ARK 全部中文
& $py restore\tools\np_rdpair.py     # CHE 属性键 <-> 中文标签配对
```

---

## 1. 结论速览

1. 两个 payload 是**同一套框架的两次编译**，共享同一份中文 UI、同一套配置键、同一份 il2cpp 包装层。
2. il2cpp API 清单 **251 个名字，两边逐字相同**；13 个 JNI 导出**逐字相同**。
3. `.data` 节（4.4 万条串）两边**完全一致**（共有 44,299，各自独有 0 / 4）——同源最硬的证据。
4. 差异集中在**游戏专用逻辑**：ARK = 技能/关卡/角色/爱情通讯；CHE = 战斗属性 Hook + 卡密服务端配置。
5. 菜单标题两边的英文/中文都列在 `.rodata`，但**偏移不同**（CHE 的 `.rodata` 起点是 `0xED950`，
   ARK 是 `0x608A0`，差约 `0x8CFB0`），说明是**分别编译、布局重排**。

### 节级同源度

| 节 | ARK 字符串数 | CHE 字符串数 | 共有 | ARK 独有 | CHE 独有 |
| --- | --- | --- | --- | --- | --- |
| `.dynstr` | 355 | 7,102 | **355** | 0 | 6,747 |
| `.rodata` | 18,243 | 18,288 | 18,076 | 167 | 212 |
| `.data` | 44,299 | 44,303 | **44,299** | 0 | 4 |

- ARK 的 `.dynstr` 是 CHE 的**严格子集**（ARK 独有 = 0）。CHE 多出的 6,747 个是导出符号
  外泄（见 §5.2）。ARK 编译时用了 `-fvisibility=hidden`，**功能没少**。
- `.rodata` 各约 180 万可打印字节，差异仅 ~1%。

---

## 2. 功能矩阵

计数口径：`.rodata` 去重后按前缀 / 精确名匹配，模块互斥（表中顺序优先）。

### 2.1 游戏功能模块

| 模块 | 判定模式（grep） | ARK | CHE | 归属 |
| --- | --- | --- | --- | --- |
| ESP / 透视 | `^esp` | 16 | 16 | **共有** |
| AIMBOT / 自瞄 | `^(aim\|selfaim\|aimbot)` | 7 | 7 | **共有** |
| OBJECT BROWSER | `^(object_\|objectFilter\|objectNameFilter\|objects$)` | 4 | 4 | **共有** |
| FLOATING ICON 悬浮球 | `^icon_` | 8 | 8 | **共有** |
| 卡密键盘 | `^kb(back\|case\|clear\|digit\|done)$` | 5 | 5 | **共有** |
| CARD KEY 授权 UI | `^(authkb\|authcard\|authclear\|authime\|authverify\|authcopy\|cardKeyText\|authorization$\|##authwindow\|##cardkey)` | 9 | 10 | 共有（CHE 多 `authcopy`） |
| Il2CppMCP 服务 | `(^\|/)(McpConfig\|mcp_dump\|mcp_stdio)` | 2 | 2 | **共有** |
| GAME FEATURES（技能/关卡） | `^gfeat_` | 15 | **0** | **ARK 特有** |
| LOVE TALK / 爱情通讯 | `^lovetalk_` | 7 | **0** | **ARK 特有** |
| ROLE / 角色（批量替换） | `^(role_\|roleFilter\|roleOccupant)` | 11 | 1 | ARK 大幅扩展 |
| **ROLEDATA 战斗属性** | `^(rd_\|roledata_enable)` | **0** | **44** | **CHE 特有** |

> 该模块 = **43 个 `rd_*` 键 + `roledata_enable`**。若用宽松正则 `rd_[a-z0-9]+`
> 扫原始字节会多出 `rd_acquire` / `rd_release` / `rd_abort` 三条**假阳性**——
> 它们来自 `__cxa_guard_acquire` / `__cxa_guard_release` / `__cxa_guard_abort`。
> 复现时须加词边界（`np_rdpair.py` 已是正确写法）。

### 2.2 框架 / 基础设施

| 模块 | 字符串证据 | ARK | CHE | 归属 |
| --- | --- | --- | --- | --- |
| JNI 导出面 | `Java_com_example_imgui_*` | 13 | 13 | **逐字相同** |
| Dear ImGui 内核 | `Dear ImGui 1.90.0 (19000)` | 一致 | 一致 | 共有 |
| il2cpp 反射 API 名 | `il2cpp_*` | **251** | **251** | **逐字相同** |
| 中文 UI 文案 | 见 §3 | 720 | 646 | **574 共有** |
| Il2CppMCP 服务端 | `Il2CppMCP`、`jsonrpc`、`tools/call` | 一致 | 一致 | 共有 |
| 悬浮球 / 触控 | `MultiTouchManager initialized` | 一致 | 一致 | 共有 |
| 图像解码 | `bad png sig`、`bad BMP`、`bad DQT table` | 一致 | 一致 | 共有 |
| 翻译接口 | `https://tr.iass.top/translate_a/single?...` | 1 | 1 | 共有 |
| 静态 OpenSSL | `AES_encrypt`、`X509_*` | 内嵌 | 内嵌 | 共有 |
| 打包器 | `XzUnpacker_IsStreamWasFinished` | 有 | 有 | 共有 |

### 2.3 逐字相同的 13 个 JNI 导出

```
Java_com_example_imgui_GLES3JNIView_MotionEventClick
Java_com_example_imgui_GLES3JNIView_getWindowRect
Java_com_example_imgui_GLES3JNIView_imgui_1Shutdown
Java_com_example_imgui_GLES3JNIView_init
Java_com_example_imgui_GLES3JNIView_isImGuiComponentTouched
Java_com_example_imgui_GLES3JNIView_nativeOnTouchEvent
Java_com_example_imgui_GLES3JNIView_nativeUpdateFilterText
Java_com_example_imgui_GLES3JNIView_real
Java_com_example_imgui_GLES3JNIView_resize
Java_com_example_imgui_GLES3JNIView_step
Java_com_example_imgui_GLES3JNIView_updateHexInput
Java_com_example_imgui_ImGui_nativeGetCircularButtonBounds
Java_com_example_imgui_ImGui_nativeGetImGuiWindowBounds
```

---

## 3. 菜单结构树

> 板块标题在 `.rodata` 中连续排布，按偏移可还原顺序。下表给**实测偏移**（文件偏移）。

### 3.1 顶层板块

| 板块 | 中文标题 | ARK 偏移 | CHE 偏移 | 归属 |
| --- | --- | --- | --- | --- |
| 功能 | **功能** | `0x6207D` | `0xEF034` | 共有 |
| — | `Tohka MOD` / `Tohka Custom MOD v2.0` | `0x6158F` / `0x638EE` | — | **ARK 特有** |
| — | `MyGame MOD` / `MyGame MOD v1.0` | — | `0xF7D6F` / `0xF495D` | **CHE 特有** |
| AIMBOT | **自瞄** / `AIMBOT` | `0x61599` | `0xEE60D` | 共有 |
| ESP | **透视** / `ESP / WALLHACK` | `0x64B07` / `0x6BA13` | `0xF85C1` | 共有 |
| 游戏功能 | **游戏功能** / `GAME FEATURES` | `0x7131F` / `0x6BA22` | `0xF85D0`（空） | 共有壳，ARK 有内容 |
| 通讯 | **通讯** / `LOVE TALK / STORY` | `0x67D84` / `0x64B0E` | — | **ARK 特有** |
| 对象 | **对象** / `OBJECT BROWSER` | `0x6F6BA` / `0x6A8C1` | `0xF74F8` | 共有 |
| AI 助手 | **AI 助手** / `AI ASSISTANT` | `0x6BA43` / `0x6B198` | `0xF7D7F` | 共有 |
| 卡密 | **卡密** / `CARD KEY` | `0x7093C` / `0x6BA0A` | `0xF85B8` | 共有 |
| 设置 | **设置** / `SETTINGS` | `0x625E8` / `0x6CD79` | `0xF97E7` | 共有 |
| 相机 | **选择相机** / `Custom Camera` | `0x6E956` / `0x6BA70` | `0xF860B` | 共有 |
| Il2Cpp | **Il2Cpp 调试信息** / `Il2Cpp API` | `0x672C8` / `0x6F6D9` | `0xFC05D` | 共有 |
| 悬浮图标 | **悬浮图标** / `FLOATING ICON` | `0x615A0` / `0x625EF` | `0xEF590` | 共有 |
| 战斗属性 | `战斗属性`（ARK 是单页标签） | `0x6E963` | — | ARK 有标签、CHE 有内容 |
| DUMP | `DUMP` | `0x64B99` | `0xF18C3` | 共有 |
| Alpha Bar | `Alpha Bar` | `0x6F963` | `0xFC2E7` | 共有 |
| PvP | `PvP` | — | `0xF3539`、`0xFB10D` | **CHE 特有** |

### 3.2 共有板块的配置键

`AIMBOT 自瞄`（7 项，含中文标签）：

```
配置键: aim_offset  aimbot  aimoffset  aimself  aimselfobj  aimtarget  aimtargetobj
中文:   自瞄设置  开启自瞄  选择自身  设置自身对象  设置目标对象
        选择目标  目标选择  自瞄参数  自瞄垂直偏移  垂直偏移
        尚未选择自身类  尚未选择目标类  自瞄
```

`ESP / 透视`（16 项）：

```
配置键: esp esp3d esp_3d_box esp_box esp_count esp_distance esp_line
        esp_name espbone espbox espcount espdist espline espname espocc espon
中文:   透视设置  开启透视绘制  开启绘制  绘制开关  绘制颜色
        显示方框  显示射线  显示骨骼  显示距离  显示名称  显示数量  显示外圈描边
        3D 方框  方框  骨骼  射线  距离  位置
        方框颜色选择  射线颜色选择  名称颜色选择  ##方框颜色选择  ##射线颜色选择  ##名称颜色选择
        方框颜色: 0,255,0,255   射线颜色: 0,255,255,255   名称颜色: 255,255,0,255
```

`OBJECT BROWSER 对象`（4 项）：

```
配置键: object  objectFilterText  objectNameFilterText  object_handles
中文:   对象列表  显示/隐藏对象列表  对象与类名  对象数量:  对象名称=
        对象全部选择  对象全部取消  取消全选  对象自动全选 [ON] / [OFF]
        对象句柄（如 o1）或十六进制地址  未命名对象  对象过滤: false
```

`FLOATING ICON 悬浮图标`（8 项，**图标名清单**）：

```
icon_alpha  icon_center  icon_minimize  icon_pos_x
icon_pos_y  icon_reset_pos  icon_ring  icon_size
中文: 图标外观  图标  图标大小  不透明度  最小化  最小化为悬浮图标
      横向位置  纵向位置  位置  重置到右上角  移动图标到屏幕中央
      悬浮图标位置已重置  窗口标题栏右侧的 [—] 按钮同样可以最小化；点击悬浮图标即可还原菜单，直接拖动图标可以调整位置。
      图标纹理加载成功！尺寸 %dx%d    图标纹理加载失败！（最小化图标将使用默认齿轮）
```

`CARD KEY 卡密键盘`（5 项）：

```
kbback  kbcase  kbclear  kbdigit  kbdone
中文: 退格  大写  小写  清空  完成  字母  数字  键盘  内置键盘  系统输入法  收起键盘
```

菜单通用控件（`##` 前缀，共有 34 个）：

```
##authwindow ##button ##cardbody ##cardkey ##cell ##closebtn ##combotext
##current ##hex ##hsv ##iconbtn ##input ##k ##key ##mainwindow ##mcp
##mcplog ##minbtn ##modicon ##nav ##original ##picker ##preview ##previewing_picker
##q ##reopen ##reopenbtn ##rgb ##selectable ##slider ##sliderf ##swatch
##titlebar_drag ##toggle
```

ARK 另有 `##allheroes`、`##owned`（**ARK 特有**）。
CHE 另有中文命名的 `##名称颜色选择`、`##方框颜色选择`、`##射线颜色选择`（这些在 ARK 中也有）。

**开发者模式**（共有）：

```
（连续点击标题栏 7 次可切换开发者模式）      0x62677
设置  应用更改  取消更改  配置已保存  已还原为保存的配置  还原上次  没有可还原的记录
```

### 3.3 ARK 特有板块

**游戏功能 / GAME FEATURES**（`gfeat_*` 15 项），中文标签：

| 配置键 | 中文标签 | 作用 |
| --- | --- | --- |
| `gfeat_cd` / `gfeat_cd_val` | 技能冷却修改 / 冷却值 | 技能无冷却 |
| `gfeat_costsp` / `gfeat_costsp_val` | 消耗 SP 修改 / 消耗值 | 技能不消耗 SP |
| `gfeat_sp` / `gfeat_sp_val` | 修改 SP 值 / SP 值 / SP 修改 | SP 修改 |
| `gfeat_soul` / `gfeat_soul_val` | 修改灵魂值 / 灵魂值 / 灵魂值修改 | 灵魂值修改 |
| `gfeat_star` / `gfeat_star_val` | 关卡星数 / 星星全开 | 关卡星数 |
| `gfeat_skillprop` / `gfeat_skillprop_val` | 技能属性 / 修改技能属性值 / 属性值 | 技能属性 |
| `gfeat_getstar` | 图鉴条件视为达成 | 直接拿星 |
| `gfeat_jump` | 一键跳关（直接判定胜利）/ 一键跳关 | 跳关 |
| `gfeat_noserver` | 跳过服务器战斗验证 / 解锁所有关卡 | 无服务端校验 |

配套游戏符号（ARK 独有）：

```
SkillData.get_CoolDown 0x61EFB   SkillData.get_Soul    0x67051
SkillData.get_SP       0x6F583   SkillData.get_CostSP  0x6FD18
SkillData.GetBattleRoleSkillProperty 0x682C3
SceneData.IsGetStar 0x682E8      SceneData.get_StarCount 0x67A2A
SceneData.get_IsNeedServerBattle 0x67064
BattleData.IsNowCampWin 0x651D3  BattleData.get_IsGameOver 0x69EE5
```

中文状态提示（ARK 独有）：

```
技能类进入战斗后生效，关卡类在关卡界面生效。          0x731A2
联机关卡的结果由服务器判定，                       0x711BA
普通关卡在本地结算，功能可正常使用。                0x6AF5A
功能在这里不生效，建议正常游玩。                   0x705A0
关卡类型  联机关卡  普通关卡  本地战斗  功能不可用  重登后会恢复原状，需要时再打开即可。
```

**通讯 / LOVE TALK STORY**（`lovetalk_*` 7 项）：

```
配置键: lovetalk_all_on  lovetalk_all_off  lovetalk_dialogs
        lovetalk_drama   lovetalk_energy   lovetalk_master  lovetalk_pass
中文:   爱情通讯解锁  解锁全部通讯内容  全部对话可读  全部开启  全部关闭
        对话不消耗体力  无需前置条件  通讯
日志:   [通讯] 总开关 -> %s
        [通讯] 开始挂钩爱情通讯/剧情解锁...
        [通讯] 挂钩完成: %d/%d      [通讯] 挂钩成功: %s @ %p    [通讯] 挂钩失败: %s @ %p
        [通讯] 对话解锁 -> %s       [通讯] get_LineDramaStaticData = %p
        [通讯] 未找到方法: %s（游戏版本可能不同）
        [通讯] PreconditionID 偏移 = 0x%zx / [通讯] PreconditionID 偏移解析失败，沿用默认 0x%zx
```

配套游戏符号（ARK 独有，`0x64B0E` 起）：

```
LineDialogStaticData.get_IsNeedEnergy       LineCollectionInfoPanel.CheckPassCondition
LineRoleDramaInfo.IsFinish / get_HasPrecondition        LineRewardUI.get_IsUnlock
LineDramaStaticData      PreconditionID      IsFinish
```

**角色系统 / ROLE**（ARK 11 项，中文界面完整）：

```
配置键: role_batch  role_batch_cancel  role_batch_ok  role_diag
        role_filter  role_filter_clear  role_refresh  role_swap  role_undo
        roleFilterText  roleOccupant

中文:   角色替换  批量补全  批量补全中…  确认批量补全  执行替换  刷新角色列表  搜索角色
        我的角色 %zu   全部英雄 %zu（绿色=已拥有）   个 / 全英雄   个槽位
        源: %s    目标: %s   源角色为空  源角色无效  目标角色无效  未选择
        将用未拥有英雄依次覆盖现有槽位（仅本地外观）   只改变外观，重登后恢复。
        批量补全未拥有角色（覆盖现有槽位）             批量补全完成：覆盖
        请先刷新，并在左右两侧各选一个角色             请先进入大厅，再点刷新
        未读到拥有角色（进入大厅后再点刷新）           点上方刷新角色列表
        一致性自检  一致性自检（诊断用）  自检: 共  , 不一致  检测异常  刷新异常
        等待中，进入大厅后可用   正在准备，稍后再试   正在准备，进入一次战斗后可用
        等待中  已连接  未连接账号  已加入网络房间  在房间中（未取得服务器地址）
        槽位已变化  ，重开角色界面即可看到
日志:   [角色] 模块初始化: 全英雄=%p 本地化=%p     [角色] GC API: gchandle_new=%p free=%p
        [角色] 全英雄列表 _size=%d 超过数组容量 %d，已收敛
        [角色] 替换槽位 %d: StaticID %s -> %s (保留 _id=%s)
        [角色] 目标技能数(%zu) 多于槽位数(%d)，已截断
        [角色自检] 槽位%d _id=%s StaticID=%s
        [RoleThread] 已排队 / 队列已满(%zu)，丢弃任务 / 任务异常(%s) / 任务未知异常
```

ARK 独有的游戏类：

```
RoleManager  RoleData  RoleDataContainer  RoleStaticData  Roles  RoleContainer
AccountData  AccountManager  LocalizationText  NetObject  NetObjectRoomControl
SkillContainer  SkillData  SkillIDs  Skills  Game
GetAllHeroStaticDataList  FireDataUpdateEvent  ProcessSyncServerBaseResult
```

**关卡检测**（ARK 独有）：

```
[检测] 初始化: IsNetBattle=%p                          0x6DE87
[检测] 网络对战=%s 关卡=%s 房间=%s 依据=%s             0x670EA
游戏判定为网络战斗 (IsNetBattle=true)                  0x66991
(IsNetBattle=true)  get_IsNetBattle
```

### 3.4 CHE 特有：ROLEDATA 战斗属性面板

**43 个 `rd_*` 键 + `roledata_enable`**，配中文标签。中文分组标题：

```
基础属性          0x0FC6A8     高级战斗属性       0x0FB142
元素属性          0x0F4ECF     伤害加成与减免      0x0F6AD5
恢复与速度        0x0F4EE4     抗性:             0x0F4EDC
攻击:             0x0FB12A     开启角色属性修改     0x0EFC76
```

> 注意：`战斗属性` 这个**标签** ARK 也有（`0x6E963`），但 ARK **没有任何 `rd_*` 键**、
> 也没有 `Actor.Update` Hook。所以是**功能**为 CHE 特有，不是字面量唯一。

| 组 | 配置键 | 中文标签 |
| --- | --- | --- |
| 基础 | `rd_hp` `rd_hprcv` | 总生命值 / 生命恢复 |
| | `rd_phyatk` `rd_phydef` | 物理攻击 / 物理防御 |
| | `rd_magatk` `rd_magdef` | 魔法攻击 / 魔法防御 |
| | `rd_basedmg` | 基础伤害 |
| 穿透 | `rd_phydefpen` `rd_phydefpenrst` | 物理穿透 / 物理穿透抵抗 |
| | `rd_magdefpen` `rd_magdefpenrst` | 魔法穿透 / 魔法穿透抵抗 |
| 减免 | `rd_phyddc` `rd_magddc` `rd_dmgrdc` | 物理伤害减免 / 魔法伤害减免 / 总伤害减免 |
| | `rd_critdmgrdc` | 暴击伤害减免 |
| | `rd_blk` `rd_blkrdc` | 格挡 / 格挡减免 |
| | `rd_ddg` | 闪避 |
| 增幅 | `rd_dmgamp` `rd_dmgampgrowth` | 总伤害增幅 / 成长伤害增幅 |
| | `rd_dmgrdcgrowth` | 成长伤害减免 |
| | `rd_crit` `rd_critamp` | 暴击 / 暴击增幅 |
| | `rd_hit` | 命中 |
| | `rd_healamp` `rd_manaamp` | 治疗增幅 / 法力增幅 |
| 恢复/速度 | `rd_mprcv` | 法力恢复 |
| | `rd_atkspeed` | 攻击速度 |
| | `rd_cdhaste` | 冷却加速 |
| 元素攻 | `rd_fireatk` `rd_wateratk` `rd_windatk` `rd_lightatk` `rd_darkatk` `rd_earthatk` | 火/水/风/光/暗/地属性攻击 |
| 元素抗 | `rd_fireres` `rd_waterres` `rd_windres` `rd_lightres` `rd_darkres` `rd_earthres` | 火/水/风/光/暗/地属性抗性 |
| PvP | `rd_pvpdmgamp` `rd_pvpddc` | PvP伤害增幅 / PvP伤害减免 |
| 开关 | `roledata_enable` | 开启角色属性修改 |

写入目标是**总属性 setter**（`0xFC69C` 起）：

```
set_TotalHP  set_TotalPhyAtk  set_TotalPhyDef
set_TotalMagAtk  set_TotalMagDef  set_TotalBaseDmg
```

Hook 挂接点与日志（`0xFA7A9` 起）：

```
[RoleDataHook] 开始初始化...                     0x0FF805
[RoleDataHook] 初始化完成，挂钩数: %d             0x0F2616
[RoleDataHook] Actor.Update 挂钩成功 @ %p         0x0FA7A9
[RoleDataHook] Actor.Update 挂钩失败, ret=%d      0x0F1756
[RoleDataHook] 找不到 Actor.Update 方法           0x0F460C
[RoleDataHook] 角色属性修改: %s                   0x0F4639
[RoleDataHook] Actor.GetBattleTeam = %p          0x0FE798
[RoleDataHook] Actor.GetMemberData = %p          0x0FA781
已挂钩 %d 个功能                                  0x0F73D4
挂钩初始化中或失败，请检查游戏版本是否匹配          0x0F965C
```

相关类：`RoleDataBase`(`0xF0629`)、`Actor`(`0xFB84B`)、`Update`(`0xFEF0C`)、
`GetBattleTeam`(`0xF7B1B`)、`GetMemberData`(`0xF45FE`)、`PvP`(`0xF3539`)、`CLNT`、`SRVR`。

**免责声明**（CHE 独有，说明这是模板化商用 Mod）：

```
工具仅供合法调试，禁止用于游戏作弊。                0x0F3043
使用者需遵守当地法律，违规后果自负。                0x0F307A
[功能] ExampleFeature 初始化（模板示例，默认不挂钩任何方法）  0x0F960A
```

> ASCII 串表里的 `] ExampleFeature `（`0xF9613`）是上面那条中文串的**子串**，
> 不是独立字符串。这也从侧面说明 ASCII-only 导出会产生"看起来像独立条目"的碎片。

> **路线差异**：ARK 走"**改游戏数据**"（技能/关卡/角色/剧情字段）；
> CHE 走"**Hook Actor.Update 直接写战斗属性**"。两者互不重叠。

---

## 4. MCP 服务接口定义

### 4.1 传输与端点

| 项 | 字符串证据 | ARK 偏移 |
| --- | --- | --- |
| 产品名 | `Il2CppMCP` | `0x61E01`, `0x64F4C` |
| 中文标题 | `<h1>Il2CppMCP 调试服务</h1>` | `0x69DFC` |
| HTML 标题 | `<title>Il2Cpp MCP</title><style>` | `0x720AA` |
| 主端点 | `/mcp` | `0x6F489` |
| 路由 | `POST /mcp            MCP(JSON-RPC) 主入口` | `0x66E9E` |
| 路由 | `GET  /tools          工具列表` | `0x62412` |
| 路由 | `GET  /status         服务状态` | `0x703A2` |
| 路由 | `GET  /logs?n=100     最近日志` | `0x6789B` |
| 路由 | `POST /call/<tool>    直接调用工具（body 为参数 JSON）` | `0x6FB50` |
| 帮助页 | `<p>调试接口: <code>/status</code> <code>/tools</code> <code>/logs?n=100</code>` | `0x6E675` |
| 传输 | `Streamable HTTP` | `0x719FC` |
| 传输 | `该服务不提供 SSE 流；请使用 POST /mcp（Streamable HTTP）` | `0x719C9` |
| 本机地址 | ` http://127.0.0.1:8917/mcp` | `0x6C9A2` |
| 默认端口 | ` 8917` | `0x64939` |
| 绑定 | `0.0.0.0` / `127.0.0.1` / `只监听 127.0.0.1##mcp` | `0x647A5` / `0x70ED0` / `0x6C8FB` |
| 生成 URL | `: http://%s:%u/mcp%s` / `: http://127.0.0.1:%u/mcp` | `0x6228A` / `0x62C66` |
| WiFi 提示 | `USB 方式地址: http://127.0.0.1:%u/mcp` | `0x62C56` |
| 客户端配置 | `  "mcpServers": {` | `0x657ED` |
| stdio 桥 | ` tools/mcp_stdio_bridge.py ` | `0x6CA56` |

### 4.2 JSON-RPC / MCP 协议方法

| 方法 | 证据 | 偏移 |
| --- | --- | --- |
| `initialize` | `[+] MCP initialize (client=%s, protocol=%s)` | `0x69D92` |
| `tools/list` | `tools` + `il2cpp_status` | `0x6F4B8` / `0x6F513` |
| `tools/call` | `tools/call` | `0x67969` |
| `resources/list` | `resources/list` | `0x63FA7` |
| `resources/templates/list` | 同名字面量 | `0x6F4FA` |
| `prompts/list` | 同名字面量 | `0x6362F` |
| `prompts/get` | 同名字面量 | `0x62469` |
| `jsonrpc` | 同名字面量 | `0x67974` |
| 会话 | `session_id` `0x6B70B`、`sessionId`、`Mcp-Session-Id: ` | — |
| 协议版本 | `protocolVersion` | `0x65F8F` |
| 客户端信息 | `clientInfo` `0x63F9C`、`capabilities` `0x63F8F` | — |
| 服务端信息 | `serverInfo` | `0x6F4BE` |
| 错误标志 | `isError` | `0x70443` |

### 4.3 工具清单（tool 名 = `il2cpp_<verb>`）

从 `.rodata` **裸名**（无 `:(true)` / `: %p` 包装）提取，两边**完全一致**：

| # | 工具 | 偏移 | 帮助文本（中文） |
| --- | --- | --- | --- |
| 1 | `il2cpp_status` | `0x6C771` | 查看 MCP 服务与 il2cpp 运行时状态…调试前建议先调用一次。 |
| 2 | `il2cpp_list_classes` | `0x716A0` | 列出某个程序集里的类（支持命名空间与类名子串过滤、分页）。 |
| 3 | `il2cpp_list_images` | `0x70D62` | 列出游戏加载的所有程序集（assembly/image）。 |
| 4 | `il2cpp_list_scene_classes` | `0x72560` | 统计当前场景里存在的 UnityEngine.Object 派生类及其实例数量。 |
| 5 | `il2cpp_class_info` | `0x6D208` | 查看一个类的详细信息：父类、接口、字段（含偏移/类型/静态标记）、方法… |
| 6 | `il2cpp_find_objects` | `0x6EC4C` | 查找某个类的所有实例，返回对象句柄（o1/o2...）与地址。 |
| 7 | `il2cpp_object_info` | `0x6A560` | 查看某个对象实例的详细信息：类、程序集、名字、存活状态、世界坐标… |
| 8 | `il2cpp_find_method` | `0x6FAC9` | 按方法名（或完整签名）在全局或某个类里搜索方法。 |
| 9 | `il2cpp_get_field` | `0x6DA34` | 读取字段值。实例字段传 object；静态字段传 class 并设 static=true。 |
| 10 | `il2cpp_set_field` | `0x6B51E` | 写入字段值（修改游戏数据）。值类型按类型自动转换。 |
| 11 | `il2cpp_call_method` | `0x68093` | 调用 il2cpp 托管方法。mode=invoke（默认）/ mode=direct。 |
| 12 | `il2cpp_read_memory` | `0x6B534` | 读取任意进程内存（先查 /proc/self/maps，非法地址返回错误而不是崩溃）。 |
| 13 | `il2cpp_write_memory` | `0x62B54` | 写入任意进程内存。默认只允许写可写映射。 |
| 14 | `il2cpp_dump_class` | `0x69C35` | 导出一个类的 C# 风格结构文本（字段+偏移+方法签名）。 |
| 15 | `il2cpp_dump_image` | `0x666C4` | 把一个程序集的所有类导出成 C# 文本文件。 |
| 16 | `il2cpp_start_gc_world` | `0x617F1` | GC 相关操作（配合 stop）。 |
| 17 | `il2cpp_stop_gc_world` | `0x64470` | GC 相关操作：info/collect/disable/enable。 |
| 18 | `il2cpp_handles` | `0x66E4B` | 查看或释放对象句柄表。 |
| 19 | `il2cpp_api_resolved` | `0x6E4C5` | — |
| 20 | `il2cpp_ready` | `0x65046` | — |

内嵌帮助页把它们编号列出（最直接的证据）：

```
1) il2cpp_status 确认 il2cpp 已就绪；                                    0x6C8A3
2) il2cpp_list_scene_classes 查看场景中的类，或 il2cpp_list_classes…     0x7044B
3) il2cpp_class_info 读取字段偏移与方法签名；                            0x657B0
4) il2cpp_find_objects 获取实例句柄（形如 o1）…                          0x681D6
5) il2cpp_call_method 调用游戏方法，il2cpp_set_field 修改数据…           0x7278A
```

**推荐流程**（AI 提示词，`0x6FCFB`、`0x71822`）：

```
典型流程: il2cpp_list_scene_classes 看场景里有哪些类 -> il2cpp_find_objects 拿实例句柄(oN)
       -> il2cpp_object_info/il2cpp_get_field 看数据 -> il2cpp_call_method 调方法。
       所有工具默认在 Unity 主线程执行。
1. il2cpp_class_info(class="              0x6C04E
2. il2cpp_find_objects(class="            0x65F9F
3. il2cpp_object_info(object="o1", include_fields=true)   0x66F43
4. 用 il2cpp_get_field / il2cpp_call_method 进一步验证你的推断；   0x6A67B
```

**工具参数名**（JSON 字段）：

```
name  handle  method  object  image  namespace  namespaze  fields  properties
class  address  length  offset  ptr  value  values  args  arguments
return_type  params  action  filter  hex  raw  mode  count  total
include_static_values  include_fields  include_position  name_only
```

**格式枚举**（中文帮助 `0x67704`、`0x71742`）：

```
hex/bytes/string/u8/i8/u16/i16/u32/i32/u64/i64/f32/f64/ptr/bool，默认 hex
auto/hex/bytes/string/il2cpp_string/u8/i8/u16/i16/u32/i32/u64/i64/f32/f64/ptr/bool
```

### 4.4 鉴权与策略开关

| 项 | 字符串证据 | 偏移 |
| --- | --- | --- |
| 令牌头（小写） | `x-mcp-token` | `0x71A0F` |
| 令牌头（会话） | `Mcp-Session-Id: ` | 行 2603 |
| Bearer | `Authorization: Bearer <token>` | `0x647D4` |
| Query 传参 | ` ?token=<token>` | `0x647F5` |
| CORS 允许头 | `Access-Control-Allow-Headers: Content-Type, Authorization, Mcp-Session-Id, X-Mcp-Token, Accept, MCP-Protocol-Version` | `0x61C8A` |
| CORS 暴露头 | `Access-Control-Expose-Headers: Mcp-Session-Id` | `0x62291` |
| 缓存 | `Cache-Control: no-store` | `0x62446` |
| 会话生成 | `il2cpp-mcp-%08x%08x` | `0x6AE87` |
| 令牌说明 | `令牌留空表示不校验；填写后客户端需带 Authorization: Bearer <token>` | `0x62C80` |
| 未授权文案 | `未授权：请提供正确的 token（Authorization: Bearer <token> 或 ?token=<token>）` | `0x647AD` |
| 只读模式 | `read_only` `0x6507D`、`read_only_mode` `0x69C56`、`readonly` `0x7036E`、`只读模式（禁止调用方法/写内存）##mcp` `0x61EB1`、`服务处于只读模式，已拒绝执行` `0x6F39C` | — |
| 只读开关 | ` allow_readonly=true ` `0x63EE9` / `0x6BEB6` | — |
| 代码补丁 | `allow_code_patch` `0x63F13` / `0x6D32C`、` allow_code_patch=true` `0x6BED4`、`允许修改只读代码页##mcp` `0x679A1` | — |
| 只读代码页提示 | `）。如需修改只读代码页，请设置 allow_readonly=true 并在配置里开启 allow_code_patch。` | `0x63EBC` |
| GC 停止 | `allow_gc_stop` `0x69C65`、` allow_gc_stop: true` `0x6475E`、`允许暂停 GC 全堆扫描(liveness)##mcp` `0x6DD14` | — |
| GC 说明 | `liveness 模式会暂停 GC（stop_gc_world），需要在配置里开启 allow_gc_stop（或直接修改 McpConfig.txt 里的 allow_gc_stop: true）。` | `0x646E0` |
| 主线程超时 | `main_thread_timeout_ms: ` `0x6F45D`、`主线程超时(ms)##mcp` `0x6A70C`、`超时后直接执行##mcp` `0x6826F` | — |
| 直接回退 | `allow_direct_fallback: ` | `0x6A5E1` |

HTTP 状态码（服务端完整实现）：`Bad Request` `0x61D01`、`Unauthorized` `0x61D0D`、
`Forbidden` `0x62292`、`Not Found` `0x6B63F`、`Method Not Allowed` `0x6568C`、
`Not Acceptable` `0x6B649`、`No Content` `0x6C043`、`Payload Too Large` `0x6C874`、
`Internal Server Error` `0x6B658`、`Service Unavailable` `0x6EDA9`。
中文对应：`请求体过大` `0x62402`、`请求头过大` `0x65F66`、`未知路径` `0x688FF`、
`不支持的 HTTP 方法` `0x688E6`。

### 4.5 状态字段与导出目录

```
uptime_ms  active_connections  last_tool_ms  last_error  tool_calls
pending_jobs  running  requests  errors  endpoints  messages  counters
```

状态页 HTML 行（中文）：

```
<tr><td>运行状态</td><td class="      0x6797C
<tr><td>工具数量</td><td>             0x65FEA
<tr><td>工具调用</td><td>             0x6FFF
<tr><td>错误数</td><td>               0x650D3
<tr><td>请求数</td><td>               0x6A6EA
<tr><td>需要令牌</td><td>             0x72813
<tr><td>只读模式</td><td>             0x6C06B
<tr><td>最近调用</td><td>             0x68248
```

运行统计（中文）：

```
请求: %llu    工具调用: %llu    错误: %llu    连接: %d                 0x6494E
对象句柄: %d    主线程任务: %llu    待处理: %llu    上次泵送: %lld ms 前   0x6498F
最近工具: %s (%llu ms)     最近错误: %s     共 %llu 条
```

Dump 落地目录：

```
/data/local/tmp/mcp_dump          0x69C84
/sdcard/Android/data/<pkg>/files/mcp_dump/     0x67AE7
/mcp_dump                         0x6B5E5
/files/mcp_dump                   0x65053
// dumped by Il2CppMCP from       0x6A5B2
已dump至/storage/emulated/0/Android/data/包名/   0x64B66
达到 64MB 单文件上限，已提前结束导出              0x6DAD1
```

### 4.6 客户端接入说明（中文帮助，完整）

```
1) 手机与电脑在同一 WiFi：直接用上面的 http 地址；                              0x6C914
2) 只有 USB：在电脑执行 adb forward tcp:8917 tcp:8917，然后用 http://127.0.0.1:8917/mcp；  0x6C959
3) Claude Desktop / Cursor / Cherry Studio 等支持 HTTP MCP 的客户端，直接填 URL 即可；     0x6C9C0
4) 只支持 stdio 的客户端，用工程里的 tools/mcp_stdio_bridge.py 做转发；         0x6CA25
5) 浏览器打开 http://<地址> 可以看到服务状态页，/tools 查看工具列表，/logs 查看日志。     0x6CA7E
电脑与手机连同一 WiFi，或先执行: adb forward tcp:%u tcp:%u                    0x70FE4
```

**用户版构建开关**（重要）：

```
[+] 用户版构建：调试页面与 MCP 服务已禁用          0x65199
当前为用户模式：AI/MCP 服务未启动                 0x6E927
[!] MCP 服务未启用（可在 %s/McpConfig.txt 里把 enabled 改成 true，或在菜单里开启）  0x73100
```

---

## 5. 卡密验证配置项与字段

### 5.1 配置文件与键

配置文件名 **`AuthConfig.txt`**（运行时从外部存储读）：

| 键 | ARK 偏移 | CHE 偏移 | 说明 |
| --- | --- | --- | --- |
| `kill_on_expire` | `0x679E5` | `0xF45A5` | 到期后杀进程 |
| `timeout_sec` | `0x62484` | `0xEF442` | 网络超时 |
| `title` | `0x6AEA9` | `0xF7AF7` | 窗口标题（如 `MyGame MOD`） |
| `notice` | `0x6828A` | `0xF4E6C` | 公告文本 |
| `enabled` | `0x636A4` 等 12 处 | `0xF058D` 等 15 处 | 总开关 |
| `host` | 注释 `0x66061` | `host: ` `0xEF43B` | 服务端地址 |
| `appid` | 注释 `0x6606A` | `appid: ` `0xFE1F5` | 应用 ID |
| `appkey` | 注释 `0x66072` | `appkey: ` `0xF0E60` | 应用密钥 |
| `rc4key` | 注释 `0x6607B` | `rc4key: ` `0xFA753` | RC4 密钥 |

模板注释（两边都有，中文）：

```
# 卡密验证配置（修改后重启游戏生效）        0x6CB01
# host / appid / appkey / rc4key 由程序内置，不在此处明文保存   0x66061
```

**CHE 额外把服务端配置打进了日志/标签**（ARK 没有这一组）：

```
wy.llua.cn                        0xF2DB7     <-- 验证服务器域名（仅 CHE 明文可见）
: host=%s appid=%s enabled=%d     0xFE754
[+] 已加载卡密配置: host=%s appid=%s enabled=%d   0xFE73B
# enabled=false 可完全关闭卡密验证    0xF6A65
[+] 已加载卡密配置: enabled=%d        0x73168（ARK 版本，只打 enabled）
```

对照 `evidence/AuthConfig.txt` 实测内容，键名完全吻合：

```ini
# 卡密验证配置（修改后重启游戏生效）
# host / appid / appkey / rc4key 由程序内置，不在此处明文保存
kill_on_expire: true
timeout_sec: 8
title: MyGame MOD
notice:
```

### 5.2 授权 UI 字段与流程

| 字段 / 动作 | 证据 | ARK 偏移 | 中文 |
| --- | --- | --- | --- |
| 卡密输入框 | `cardKeyText` | `0x6D712`（`.data` 副本 `0x5297F0`） | 点击输入框或下方按钮输入卡密 |
| 输入缓冲 | `##cardkey` | `0x72CE8` | — |
| 授权窗口 | `##authwindow` | — | 卡密授权 / 卡密验证 / 卡密验证 / CARD KEY |
| 主卡 | `maincard` | `0x6BA54` | 卡密键盘 |
| 键盘 | `authkb` | `0x6C3C6` | 内置键盘 / 系统输入法 |
| 退格 | `kbback` | `0x6325B` | 退格 |
| 完成 | `kbdone` | `0x63262` | 完成 |
| 清空 | `kbclear` | `0x61721` | 清空 / 清空卡密 |
| 大小写 | `kbcase` | `0x72CF2` | 大写 / 小写 |
| 数字 | `kbdigit` | `0x6A284` | 数字 |
| 提交验证 | `authverify` | `0x6539B` | 验证并进入 / 请输入卡密后点击「验证并进入」。 |
| 机器码 | `authime` / `imei` | `0x64C26` / `0x6703B` | 机器码: |
| 复制机器码 | `authcopy`（**仅 CHE**） | CHE `0xF754B` | 复制机器码 / 已复制机器码 |
| 清空 | `authclear` | `0x6CDB9` | — |
| 授权状态 | `authorization` | `0x70EEB` | 等待验证 / 验证中... / 验证通过 |
| 取消/应用配置 | `cancelcfg` / `applycfg` | `0x63204` / `0x6A240` | 应用更改 / 取消更改 |

### 5.3 验证状态机（中文日志，两边共有）

```
正在连接卡密服务器验证，请稍候...               0x616A1
检测到已保存的卡密，自动验证中...               0x71405
正在验证卡密...                              0x68A08
请输入卡密                                   0x71ACE
卡密错误                                     0x6517A
卡密验证失败                                  0x71067
网络异常                                     0x73195
网络请求失败:                                 0x67A0C
[-] 卡密请求失败: %s                          0x6E75A
[-] 卡密响应不是合法 JSON: %s                  0x689E2
[!] 响应 RC4 解密失败，按明文解析                0x65865
数据加密失败                                  0x6B741
[+] 卡密验证通过, vip=%lld (%s)                0x65154
验证成功，到期时间                             0x636CE
验证成功：永久授权                             0x6E775
永久授权                                     0x704E1
[+] 已设置到期守护: %lld 秒后自动退出            0x6A75E
[-] 卡密已到期，结束进程                        0x660AE
卡密验证已在 AuthConfig.txt 中关闭，直接进入菜单   0x6F0BB
如果确认卡密无误仍然报错，请检查网络后重试。        0x6C3CD
```

### 5.4 设备指纹：**ARK 有，CHE 没有**

ARK 通过 Android `Settings.Secure` 取 `android_id` 派生机器码：

```
android/provider/Settings$Secure        getContentResolver
getString                               android_id
()Landroid/content/ContentResolver;
(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;
[Auth] 已获取 Android ID 作为设备种子（长度 %zu）        0x6435E
[Auth] 未能获取 Android ID，机器码将回退为本地随机生成    0x7094A
[+] 机器码(设备派生): %s                              0x7285A
[+] 生成机器码: %s                                    0x6DDE0
无法生成机器码：机器码目录不可写                          0x63FD5
[+] 卡密模块初始化完成, files=%s (%s), enabled=%d, markcode=%s   0x6CB38
&markcode=   0x67040        &t=   0x6704B
cba9759578a143174e333b5     0x6CB80
```

CHE 的对应文案是 `机器码:` / `复制机器码` / `已复制机器码` / `第一次使用请输入卡密` /
`未启用验证` / `售后 / 交流请联系卡密商`，**没有 Android ID 采集路径**。

---

## 6. 两个 payload 的差异清单

### 6.1 游戏专用部分

**ARK 特有（中文 146 条 + 英文 167 条）**：

| 主题 | 证据 |
| --- | --- |
| 品牌 | `Tohka MOD`、`Tohka Custom MOD v2.0`、`售后 / 交流 QQ 群 938552503`、`卡密购买客服 QQ 2647103221  WX Miepo_08` |
| 技能机制 | `gfeat_cd(_val)`、`gfeat_costsp(_val)`、`gfeat_sp(_val)`、`gfeat_soul(_val)`、`gfeat_star(_val)`、`gfeat_skillprop(_val)`、`gfeat_getstar`、`gfeat_jump`、`gfeat_noserver`；中文 `技能冷却修改`/`技能无冷却`/`技能不消耗 SP`/`修改 SP 值`/`修改灵魂值`/`关卡星数`/`一键跳关`/`跳过服务器战斗验证`/`解锁所有关卡`/`星星全开` |
| 关卡检测 | `[检测] 网络对战=%s 关卡=%s 房间=%s 依据=%s`、`游戏判定为网络战斗 (IsNetBattle=true)`、`SceneData.get_IsNeedServerBattle` |
| 爱情通讯 | `LOVE TALK / STORY`、`lovetalk_*` 7 项、`爱情通讯解锁`、`解锁全部通讯内容`、`对话不消耗体力`、`LineDialogStaticData`、`LineCollectionInfoPanel`、`LineRewardUI`、`LineRoleDramaInfo`、`CheckPassCondition`、`PreconditionID` |
| 角色系统 | `RoleManager`、`RoleData`、`RoleDataContainer`、`RoleStaticData`、`Roles`、`AccountData`、`AccountManager`、`LocalizationText`、`NetObject`、`NetObjectRoomControl`、`SkillContainer`、`SkillData`、`SkillIDs`、`Skills`、`GetAllHeroStaticDataList`、`role_batch*`、`role_diag`、`role_filter*`、`role_refresh`、`role_swap`、`role_undo`、`##allheroes`、`##owned`；中文 `角色替换`/`全部英雄 %zu（绿色=已拥有）`/`批量补全`/`一致性自检` |
| 其他 | `combat_tab0/1`、`nbd_refresh`、`self_class`、`[Auth] `、`[RoleThread] `、`android_id`、`/sdcard/数据.txt`、`已成功导出数据至/sdcard/数据.txt` |

**CHE 特有（中文 72 条 + 英文 212 条）**：

| 主题 | 证据 |
| --- | --- |
| 品牌 | `MyGame MOD`、`MyGame MOD v1.0`、`ModMode`、`wy.llua.cn`、`售后 / 交流请联系卡密商` |
| **战斗属性面板** | 43 个 `rd_*` + `roledata_enable`（见 §3.4）；中文分组 `基础属性`/`高级战斗属性`/`元素属性`/`伤害加成与减免`/`恢复与速度`；全部属性中文名 |
| Hook 框架 | `[RoleDataHook] *` 全套日志、`RoleDataBase`、`Actor`、`Update`、`GetBattleTeam`、`GetMemberData`、`PvP`、`CLNT`、`SRVR`、`set_TotalHP`/`set_TotalPhyAtk`/…/`set_TotalBaseDmg` |
| 模板化痕迹 | `[功能] ExampleFeature 初始化（模板示例，默认不挂钩任何方法）`、`] ExampleFeature `、`工具仅供合法调试，禁止用于游戏作弊。`、`使用者需遵守当地法律，违规后果自负。` |
| 卡密差异 | `authcopy` `复制机器码` `已复制机器码` `第一次使用请输入卡密` `未启用验证`、`appid: `、`appkey: `、`rc4key: `、`host: `、`# enabled=false 可完全关闭卡密验证`、`: host=%s appid=%s enabled=%d` |
| 日志 | `/data/user/0/com.ztgame.bob/ndk.log`（**另一宿主包名残留，说明这套 payload 被复用过**）、`=== Log Started at `、` ===` |
| libc++ 符号 | `N10__cxxabiv1*`、`NSt6__ndk1*`、`St1[0-9]*`、`PK[acdeghijmnostvwxy]`、`PD[hinsu]` 等 RTTI 名 |

### 6.2 `.dynstr` 差异：编译选项，不是功能

CHE 多导出 **6,747** 个符号，全部是**静态库符号外泄**：

| 类别 | 数量 |
| --- | --- |
| `_Z*`（C++ mangled） | 1,935 |
| 明文 C 符号 | 4,812（含 OpenSSL 族约 3,430） |

样例：`AES_encrypt`、`ASN1_INTEGER_get`、`X509_verify_cert`、`CMAC_Final`、
`CTLOG_STORE_new`、`ChaCha20_ctr32`、`Camellia_Ekeygen`、`ASYNC_start_job`。

**结论**：ARK 用了 `-fvisibility=hidden` 把静态库符号隐藏，CHE 没有。
实际代码路径不受影响——`il2cpp_*` 251 个、13 个 JNI 导出、MCP 全部中文文案两边一致即为佐证。

### 6.3 体量差异

| 项 | ARK | CHE | 差 |
| --- | --- | --- | --- |
| 文件大小 | 7,587,080 | 8,446,416 | +859,336 |
| `.rodata` 起点 | `0x608A0` | `0xED950` | +0x8CFB0 |
| `.dynstr` 大小 | 5,675 | 199,342 | **+193,667** |
| `.data` 大小 | 2,184,560 | 2,184,984 | +424 |

文件大小差的 ~86 万字节里 `.dynstr` 只占 19 万；其余是符号表 / 重定位 / 哈希表
以及 CHE 多出的属性 Hook 代码。`.data` 只差 424 字节——**再次印证"同一框架、同一配置表"**。

---

## 7. 运行时加载与注入（共有）

| 项 | 证据 | 偏移 |
| --- | --- | --- |
| JavaVM 定位 | `[JavaVM] Success with library: %s` | `0x61E??` |
| JavaVM 失败 | `[JavaVM] Failed to obtain JavaVM from any library!` | 行 2068 |
| JavaVM 已初始化 | `[JavaVM] JavaVM already initialized: %p` | 行 1476 |
| JNI_GetCreatedJavaVMs | 同名字面量 | `0xEF566`(CHE) |
| ART | `/libart.so` | `0x61986` |
| ClassLoader | `java/lang/ClassLoader` | `0x61894` |
| 动态加载 dex | `dalvik/system/DexClassLoader` | `0x61642` |
| 内嵌 dex 入口 | `com/example/imgui/ImGui` | `0x62069` |
| il2cpp 基址 | `il2cpp_base: %lx` / `Fallback il2cpp_base: %lx` | `0x6B415` / `0x6AAE9` |
| 基址失败 | `Failed to get il2cpp base address using xdl` | `0x61852` |
| API 解析 | `Failed to initialize il2cpp api` | `0x68654` |
| API 解析成功 | `[+] il2cpp API 解析完成: 成功 %d 个, 缺失 %d 个, base=%p, core=%s` | `0x70CCC` |
| API 就绪 | `[+] il2cpp 已就绪(MCP)，可解析函数 %d 个` | `0x69E5D` |
| 重试 | `[*] 尝试重新解析 il2cpp API ...` | `0x6F317` |
| 未加载 | `[-] 打开 libil2cpp.so 失败，il2cpp 尚未加载` | `0x701E4` |
| 偏移解析 | `Resolved %s via offset: 0x%lX` | `0x61902` |
| JNI 注册 | `Native methods registered` | `0x71436` |
| 注册失败 | `Failed to register GLES3JNIView methods` / `ImGui methods` / `native methods` | `0x66B64` / `0x66BA4` / `0x67E28` |
| 主线程 | `主线程任务泵已关闭，拒绝在后台线程执行 il2cpp 调用` | `0x645C4` |
| 主线程超时 | `[!] %s 等待主线程 %dms 超时，改由后台线程直接执行` | `0x6BD82` |
| 版本分支 | `Version greater than 2018.3` / `Version less than 2018.3` | `0x66D18` / `0x700AA` |
| UnityHook | `/files/UnityHookEsp.txt` | `0x61577` |
| CHE 专用 | `libUnityHookTool.so` | CHE `.dynstr` 行 7099 |

---

## 8. il2cpp API 全清单（251 个，两边逐字相同）

> 提取方式：`il2cpp_[A-Za-z0-9_]+` 正则扫全部字符串后去重。
> ARK 与 CHE 的集合**完全相等**（`ark-only = che-only = ∅`）。
> 含 `il2cpp_` 的 `.rodata` 串 339 行，解出 251 个唯一 API 名。
> 分组按 il2cpp C API 头文件布局。机器可读版：`restore/tools/il2cpp_apis.json`。

### 8.1 Runtime 初始化（28）

```
il2cpp_init                          il2cpp_init_utf16
il2cpp_shutdown                      il2cpp_set_config
il2cpp_set_config_utf16              il2cpp_set_config_dir
il2cpp_set_data_dir                  il2cpp_set_temp_dir
il2cpp_set_commandline_arguments     il2cpp_set_commandline_arguments_utf16
il2cpp_set_memory_callbacks          il2cpp_set_find_plugin_callback
il2cpp_set_default_thread_affinity   il2cpp_register_log_callback
il2cpp_register_debugger_agent_transport
il2cpp_debugger_set_agent_options    il2cpp_override_stack_backtrace
il2cpp_unity_install_unitytls_interface
il2cpp_runtime_unhandled_exception_policy_set
il2cpp_unhandled_exception           il2cpp_is_debugger_attached
il2cpp_is_vm_thread                  il2cpp_stats_get_value
il2cpp_allocation_granularity        il2cpp_object_header_size
il2cpp_array_object_header_size      il2cpp_offset_of_array_length_in_array_object_header
il2cpp_ready
```

### 8.2 Domain / Assembly / Image（11）

```
il2cpp_domain_get                    il2cpp_domain_get_assemblies
il2cpp_domain_assembly_open          il2cpp_assembly_get_image
il2cpp_image_get_assembly            il2cpp_image_get_class
il2cpp_image_get_class_count         il2cpp_image_get_entry_point
il2cpp_image_get_filename            il2cpp_image_get_name
il2cpp_get_corlib
```

### 8.3 Class 反射（49）

```
il2cpp_class_from_name               il2cpp_class_from_type
il2cpp_class_from_il2cpp_type        il2cpp_class_from_system_type
il2cpp_class_get_name                il2cpp_class_get_namespace
il2cpp_class_get_assemblyname        il2cpp_class_get_image
il2cpp_class_get_parent              il2cpp_class_get_declaring_type
il2cpp_class_get_nested_types        il2cpp_class_get_interfaces
il2cpp_class_get_type                il2cpp_class_get_type_token
il2cpp_class_get_flags               il2cpp_class_get_rank
il2cpp_class_get_element_class       il2cpp_class_get_bitmap
il2cpp_class_get_bitmap_size         il2cpp_class_get_data_size
il2cpp_class_get_static_field_data   il2cpp_class_get_userdata_offset
il2cpp_class_set_userdata            il2cpp_class_get_events
il2cpp_class_get_fields              il2cpp_class_get_field_from_name
il2cpp_class_get_methods             il2cpp_class_get_method_from_name
il2cpp_class_get_properties          il2cpp_class_get_property_from_name
il2cpp_class_num_fields              il2cpp_class_instance_size
il2cpp_class_value_size              il2cpp_class_array_element_size
il2cpp_class_enum_basetype           il2cpp_class_is_enum
il2cpp_class_is_valuetype            il2cpp_class_is_blittable
il2cpp_class_is_abstract             il2cpp_class_is_interface
il2cpp_class_is_generic              il2cpp_class_is_inflated
il2cpp_class_is_subclass_of          il2cpp_class_is_assignable_from
il2cpp_class_has_parent              il2cpp_class_has_attribute
il2cpp_class_has_references          il2cpp_class_for_each
il2cpp_class_info
```

### 8.4 Type 反射（11）

```
il2cpp_type_get_name                 il2cpp_type_get_name_chunked
il2cpp_type_get_assembly_qualified_name
il2cpp_type_get_object               il2cpp_type_get_type
il2cpp_type_get_attrs                il2cpp_type_get_class_or_element_class
il2cpp_type_is_byref                 il2cpp_type_is_pointer_type
il2cpp_type_is_static                il2cpp_type_equals
```

### 8.5 Method 反射与调用（25）

```
il2cpp_method_get_name               il2cpp_method_get_param
il2cpp_method_get_param_name         il2cpp_method_get_param_count
il2cpp_method_get_return_type        il2cpp_method_get_class
il2cpp_method_get_declaring_type     il2cpp_method_get_object
il2cpp_method_get_flags              il2cpp_method_get_token
il2cpp_method_get_from_reflection    il2cpp_method_is_instance
il2cpp_method_is_generic             il2cpp_method_is_inflated
il2cpp_method_has_attribute          il2cpp_debug_get_method_info
il2cpp_runtime_invoke                il2cpp_runtime_invoke_convert_args
il2cpp_runtime_object_init           il2cpp_runtime_object_init_exception
il2cpp_runtime_class_init            il2cpp_resolve_icall
il2cpp_add_internal_call             il2cpp_object_new
il2cpp_value_box
```

### 8.6 Field / Property 读写（18）

```
il2cpp_field_get_name                il2cpp_field_get_type
il2cpp_field_get_parent              il2cpp_field_get_offset
il2cpp_field_get_flags               il2cpp_field_has_attribute
il2cpp_field_is_literal              il2cpp_field_get_value
il2cpp_field_get_value_object        il2cpp_field_set_value
il2cpp_field_set_value_object        il2cpp_field_static_get_value
il2cpp_field_static_set_value        il2cpp_property_get_name
il2cpp_property_get_flags            il2cpp_property_get_parent
il2cpp_property_get_get_method       il2cpp_property_get_set_method
```

### 8.7 Object / String / Array / GCHandle（26）

```
il2cpp_object_get_class              il2cpp_object_get_size
il2cpp_object_get_virtual_method     il2cpp_object_unbox
il2cpp_object_info                   il2cpp_string_new
il2cpp_string_new_len                il2cpp_string_new_utf16
il2cpp_string_new_wrapper            il2cpp_string_chars
il2cpp_string_length                 il2cpp_string_intern
il2cpp_string_is_interned            il2cpp_array_new
il2cpp_array_new_specific            il2cpp_array_new_full
il2cpp_array_class_get               il2cpp_array_element_size
il2cpp_array_length                  il2cpp_array_get_byte_length
il2cpp_bounded_array_class_get       il2cpp_gchandle_new
il2cpp_gchandle_new_weakref          il2cpp_gchandle_get_target
il2cpp_gchandle_free                 il2cpp_gchandle_foreach_get_target
```

### 8.8 GC / 内存（22）

```
il2cpp_gc_collect                    il2cpp_gc_collect_a_little
il2cpp_gc_start_incremental_collection
il2cpp_gc_disable                    il2cpp_gc_enable
il2cpp_gc_is_disabled                il2cpp_gc_is_incremental
il2cpp_gc_get_heap_size              il2cpp_gc_get_used_size
il2cpp_gc_get_max_time_slice_ns      il2cpp_gc_set_max_time_slice_ns
il2cpp_gc_set_mode                   il2cpp_gc_alloc_fixed
il2cpp_gc_free_fixed                 il2cpp_gc_foreach_heap
il2cpp_gc_has_strict_wbarriers       il2cpp_gc_wbarrier_set_field
il2cpp_gc_set_external_allocation_tracker
il2cpp_gc_set_external_wbarrier_tracker
il2cpp_alloc                         il2cpp_free
il2cpp_gc
```

### 8.9 Exception / Thread / Monitor（26）

```
il2cpp_raise_exception               il2cpp_unhandled_exception
il2cpp_exception_from_name_msg       il2cpp_get_exception_argument_null
il2cpp_format_exception              il2cpp_format_stack_trace
il2cpp_native_stack_trace            il2cpp_thread_attach
il2cpp_thread_detach                 il2cpp_thread_current
il2cpp_thread_get_all_attached_threads
il2cpp_thread_get_frame_at           il2cpp_thread_get_stack_depth
il2cpp_thread_get_top_frame          il2cpp_thread_walk_frame_stack
il2cpp_current_thread_get_frame_at   il2cpp_current_thread_get_stack_depth
il2cpp_current_thread_get_top_frame  il2cpp_current_thread_walk_frame_stack
il2cpp_monitor_enter                 il2cpp_monitor_exit
il2cpp_monitor_try_enter             il2cpp_monitor_try_wait
il2cpp_monitor_wait                  il2cpp_monitor_pulse
il2cpp_monitor_pulse_all
```

### 8.10 Custom Attributes（7）

```
il2cpp_custom_attrs_from_class       il2cpp_custom_attrs_from_method
il2cpp_custom_attrs_get_attr         il2cpp_custom_attrs_has_attr
il2cpp_custom_attrs_construct        il2cpp_custom_attrs_free
il2cpp_debug_guide
```

### 8.11 Dump / 调试辅助（2）

```
il2cpp_dump_class                    il2cpp_dump_image
```

### 8.12 Unity Liveness / 内存快照（8）

```
il2cpp_unity_liveness_allocate_struct
il2cpp_unity_liveness_calculation_from_root
il2cpp_unity_liveness_calculation_from_statics
il2cpp_unity_liveness_finalize
il2cpp_unity_liveness_free_struct
il2cpp_capture_memory_snapshot       il2cpp_free_captured_memory_snapshot
il2cpp_stats_dump_to_file
```

### 8.13 内存读写 / 内部辅助（8）

```
il2cpp_read_memory    il2cpp_write_memory    il2cpp_get_field
il2cpp_set_field      il2cpp_call_method     il2cpp_find_method
il2cpp_find_objects   il2cpp_handles
```

### 8.14 列表类工具入口（3）

```
il2cpp_list_classes   il2cpp_list_images    il2cpp_list_scene_classes
```

### 8.15 非 API 的 `il2cpp_*` 标记串（日志/分支用，非函数名）

```
il2cpp_base   il2cpp_api_resolved   il2cpp_status
il2cpp_handles   il2cpp_string   il2cpp_gc   il2cpp_debug_guide
```

**计数核对**：28+11+49+11+25+18+26+22+26+7+2+8+8+3 = **244**，
加上 §8.15 中不与前面重复的标记串，去重后为 **251**。

---

## 9. 复现方式

```powershell
$py = "C:\Users\BanCN\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe"
$env:PYTHONIOENCODING = "utf-8"

& $py restore\tools\np_cjk.py       # ★ 非 ASCII 串（既有 txt 漏掉的部分）
& $py restore\tools\np_cjkmap.py ark # ★ 全部中文（按偏移）
& $py restore\tools\np_cjkmap.py arkokonly
& $py restore\tools\np_cjkmap.py cheonly
& $py restore\tools\np_rdpair.py    # ★ CHE 属性键 <-> 中文标签
& $py restore\tools\np_survey.py    # 节级同源度
& $py restore\tools\np_il2cpp.py    # 251 个 API 分组
& $py restore\tools\np_modules.py   # 功能矩阵计数
& $py restore\tools\np_diff.py      # 菜单标题 / ##id / 配置键 diff
& $py restore\tools\np_menu.py ark  # 板块锚点定位
& $py restore\tools\np_range.py work\libArkRe-strings.txt 1870 2120
```

产物：`cjk.json`、`cjk_map.json`、`il2cpp_apis.json`、`modules.json`、`matrix.json`。

---

## 10. 证据局限

- **未反汇编机器码**（按要求）。功能描述止于**字符串与符号层面**；
  函数地址级结论引用现有 `docs/auth-*.md`（那批报告做了定点反汇编）。
- 偏移均为**文件偏移**，非虚拟地址。ARK / CHE 的 `.rodata` 起点不同，
  **不能跨文件直接比偏移**，只能比字符串内容。
- **既有 `work/*-strings.txt` 不可信**：ASCII-only，丢掉全部中文。
  本文档的中文结论全部来自重新扫描原始 `.rodata`。
- CJK 提取会有少量误报：`.rodata` 尾部（ARK `≥0x74000`、CHE `≥0x100000`）是压缩/加密数据，
  UTF-8 解码会撞出零星"伪汉字"（`F噄`、`EH袀`、`噀ba4`、`痤z`…），两边**各 198 条且内容相同**。
  本文档的界面文案全部取自边界之前的区域：
  **ARK `0x608A0`–`0x74000`（720 条）、CHE `0xED950`–`0x100000`（646 条）**。
- `.data` 节约 2.8 万条是随机二进制噪声被可打印串扫描误收，不计入矩阵。
- `AuthConfig.txt` 的 `host`/`appid`/`appkey`/`rc4key` **明文不在 payload 里**
  （只有 CHE 泄露域名 `wy.llua.cn`），需运行时抓包或内存 dump 才能取得完整值。
