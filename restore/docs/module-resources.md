# 模块还原 · 资源与清单

host APK 的资源提取结果与 AndroidManifest 完整还原，供重建 APK 使用。

---

## 一、AndroidManifest.xml（完整还原）

**来源**：原包解码（`restore/host-apk/src/main/AndroidManifest.xml`，3040 B）

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    android:sharedUserId="com.Riruriru.Sx"      ← 关键：与配套游戏共享 UID
    android:versionCode="1"
    android:versionName="1.1.2"
    android:compileSdkVersion="36"
    package="com.Riruriru.Sx">

  <uses-sdk android:minSdkVersion="23" android:targetSdkVersion="27"/>

  <!-- 权限（7 条 + 1 条自定义） -->
  <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>   ← 悬浮窗必需
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
  <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>

  <permission android:name="com.Riruriru.Sx.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
              android:protectionLevel="0x00000002"/>
  <uses-permission android:name="com.Riruriru.Sx.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"/>

  <application
      android:label="@7F0E001C"
      android:icon="@7F070093"
      android:debuggable="true"                  ← 调试标志打开
      android:extractNativeLibs="false"
      android:appComponentFactory="androidx.core.app.CoreComponentFactory"
      android:requestLegacyExternalStorage="true">

    <!-- 唯一入口 -->
    <activity android:name="com.Riruriru.Sx.MainActivity"
              android:theme="@7F0F000B" android:label="@7F0E001C"
              android:exported="true">
      <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
      </intent-filter>
    </activity>

    <!-- 前台服务（不导出） -->
    <service android:name="com.Riruriru.Sx.FloatServiceView" android:exported="false"/>

    <!-- AndroidX 自动生成的（重建时由 androidx.startup 提供） -->
    <provider android:name="androidx.startup.InitializationProvider" .../>
    <receiver android:name="androidx.profileinstaller.ProfileInstallReceiver" .../>
  </application>
</manifest>
```

### 要点

| 属性 | 值 | 说明 |
| --- | --- | --- |
| `sharedUserId` | `com.Riruriru.Sx` | **与配套游戏共享 UID**（这是核心设计） |
| `targetSdkVersion` | 27 | 低 target 让 `MANAGE_EXTERNAL_STORAGE` 等老权限好拿 |
| `debuggable` | true | 打开调试 |
| `extractNativeLibs` | false | .so 不从 APK 解压，直接 mmap |
| `requestLegacyExternalStorage` | true | 用旧式外部存储访问 |
| 组件数 | 1 activity + 1 service | 极简 |

**注意**：`sharedUserId` 是安装期属性，改动后无法覆盖安装，必须先卸载。

---

## 二、assets（3 个，全部是核心载荷）

| 文件 | 大小 | 作用 |
| --- | --- | --- |
| `64` | 14,120 B | AArch64 ptrace 注入器（ELF 可执行文件） |
| `libArkRe.so` | 7,587,080 B | Ark ReCode 专用 payload |
| `libCherryTale.so` | 8,446,416 B | Cherry Tale 专用 payload |

**这三个是 APK 的绝大部分体积**（约 16 MB / 16 MB）。

它们**不能压缩**（要保证 `extractNativeLibs=false` 时能直接使用），
重建时 `build.gradle` 里要写：

```gradle
aaptOptions { noCompress 'so', 'dex' }
```

---

## 三、自有图片资源

| 文件 | 大小 | 用途 |
| --- | --- | --- |
| `drawable/sx.jpg` | **1,331,458 B** | 主视觉背景图（1366×768 左右） |
| `drawable/srzx.jpg` | **382,369 B** | 面板背景图（代码里按名字 `srzx` 查找） |
| `drawable-xxxhdpi-v4/ic_launcher.png` | 40,509 B | 应用图标（xxxhdpi） |
| `drawable-xxhdpi-v4/ic_launcher.png` | 26,327 B | 图标 |
| `drawable/ic_launcher.png` | 13,867 B | 图标（默认） |
| `drawable-xhdpi-v4/ic_launcher.png` | 13,867 B | 图标 |
| `drawable-hdpi-v4/ic_launcher.png` | 8,741 B | 图标 |
| `drawable-mdpi-v4/ic_launcher.png` | 4,607 B | 图标 |

**关键**：`FloatContentView` 里用
`getResources().getIdentifier("srzx", "drawable", getPackageName())`
在运行时按**名字**查找背景图。重建时 `srzx.jpg` 这个名字不能改，
否则面板背景会退化成纯黑。

其余 202 张 png 都是 AndroidX / Material 的库资源（`abc_*` / `ic_call_*` 等），
重建时由依赖自动提供，不需要手动提取。

---

## 四、布局与 XML

| 目录 | 数量 | 说明 |
| --- | --- | --- |
| `res/layout*/` | 109 | 其中自有布局极少（面板是纯代码构建的），其余是 AndroidX 的 |
| `res/xml/` | 2 | 自有 XML 配置 |

**重要发现**：`FloatContentView` 的界面是**纯 Java 代码构建**的
（`initView()` 里 new 各种 View 再 addView），**没有用布局 XML**。
所以重建时布局 XML 基本不需要，主要靠代码。

---

## 五、资源目录结构

```
restore/host-apk/res-final/
├─ assets/                 3 个载荷（核心）
│  ├─ 64
│  ├─ libArkRe.so
│  └─ libCherryTale.so
├─ images/               210 个图片
│  ├─ drawable/
│  │  ├─ sx.jpg            主视觉
│  │  └─ srzx.jpg          面板背景（名字不能改）
│  ├─ drawable-*/          各密度 ic_launcher
│  └─ drawable-*/*.9.png   AndroidX 的九宫格
├─ layout/               109 个布局 XML
├─ xml/                    2 个
├─ AndroidManifest.xml     二进制原件
└─ resources.arsc          资源表
```

`restore/host-apk/src/main/res/` 下是**完整的 838 个资源**（已按 Gradle 布局排好）。

---

## 六、重建 APK 需要什么

### 6.1 必需

| 项 | 来源 |
| --- | --- |
| 源码 | `src/main/java/`（36 个类，已还原） |
| Manifest | `src/main/AndroidManifest.xml` |
| assets 三个载荷 | `res-final/assets/` |
| `srzx.jpg` + `sx.jpg` | `res-final/images/drawable/` |
| ic_launcher 各密度 | `res-final/images/drawable-*/` |
| AndroidX 依赖 | Gradle 自动拉（appcompat / constraintlayout / material / cardview） |

### 6.2 不需要手动提供

- 其余 202 张 png 与 100+ 布局 XML —— 都是 AndroidX/Material 库资源
- `resources.arsc` —— 由 aapt2 在构建时生成
- `R.java` —— 由 aapt2 生成

### 6.3 构建注意事项

```gradle
android {
    namespace 'com.Riruriru.Sx'
    defaultConfig {
        minSdk 23
        targetSdk 27        // 与原包一致
    }
    aaptOptions {
        noCompress 'so', 'dex'    // 载荷不能被压缩
    }
}
```

**签名**：要与配套游戏一致才能共享 UID。原包用的是 `DEBUG.RSA`（调试签名），
所以重建时用任意 debug keystore 即可，但**必须与游戏的签名相同**。

---

## 七、版本信息汇总

| 项 | 值 |
| --- | --- |
| 包名 | `com.Riruriru.Sx` |
| versionCode / versionName | 1 / `1.1.2` |
| minSdk / targetSdk | 23 / 27 |
| compileSdk | 36 |
| sharedUserId | `com.Riruriru.Sx` |
| 签名 | v1 `META-INF/DEBUG.RSA` + v2 + v3 |
| 编译产物 | `classes.dex` 10,472,036 B / `classes2.dex` 491,748 B / `classes3.dex` 36,628 B |
| 自有代码所在 | **classes3.dex**（业务）+ classes2.dex（R 类） |
