#!/usr/bin/env bash
# 语法/编译验证：用 javac 检查还原出来的源码能否编译。
#
# 分三档验证（依赖不同）：
#   A. com.Riruriru.Sx 的业务类（只依赖 android.jar）
#   B. injected.dex 的 ImGui 类（只依赖 android.jar）
#   C. irene AlGui 框架（依赖 AndroidX，本机无缓存，只做语法解析）
#
# 用法: bash verify_build.sh
set -uo pipefail

SDK="${ANDROID_SDK:-$LOCALAPPDATA/Android/Sdk}"
[ -d "$SDK" ] || SDK="$HOME/AppData/Local/Android/Sdk"
JAR="$SDK/platforms/android-35/android.jar"
JAVAC="${JAVAC:-C:/Program Files/Java/jdk-17/bin/javac.exe}"
HERE="$(cd "$(dirname "$0")" && pwd)"
OUT="$HERE/_verify"

echo "SDK   = $SDK"
echo "jar   = $JAR"
[ -f "$JAR" ] || { echo "找不到 android.jar"; exit 1; }

rm -rf "$OUT"; mkdir -p "$OUT"

# ---------- A. 业务类 ----------
echo
echo "########## A. com.Riruriru.Sx 业务类 ##########"
mapfile -t A < <(find "$HERE/src/main/java/com" -name '*.java' ! -name 'R.java')
echo "文件数: ${#A[@]}"
"$JAVAC" -encoding UTF-8 -source 8 -target 8 -nowarn -proc:none \
    -bootclasspath "$JAR" -d "$OUT/a" "${A[@]}" 2>&1 | head -30
echo "A exit=$?"
ls "$OUT/a/com/Riruriru/Sx/"*.class 2>/dev/null | wc -l | sed 's/^/  生成 class: /'

# ---------- B. ImGui 覆盖层 ----------
echo
echo "########## B. injected.dex 的 ImGui 类 ##########"
B="$HERE/../injected-dex/src/com/example/imgui"
mapfile -t BF < <(find "$B" -name '*.java')
echo "文件数: ${#BF[@]}"
"$JAVAC" -encoding UTF-8 -source 8 -target 8 -nowarn -proc:none \
    -bootclasspath "$JAR" -d "$OUT/b" "${BF[@]}" 2>&1 | head -30
echo "B exit=$?"
ls "$OUT/b/com/example/imgui/"*.class 2>/dev/null | wc -l | sed 's/^/  生成 class: /'

# ---------- C. AlGui 框架（只做语法解析） ----------
echo
echo "########## C. irene AlGui 框架（语法检查）##########"
C="$HERE/src/main/java/irene"
mapfile -t CF < <(find "$C" -name '*.java')
echo "文件数: ${#CF[@]}"
# 用 -proc:only 只做解析，不要求解析符号
"$JAVAC" -encoding UTF-8 -source 8 -target 8 -nowarn -proc:only "${CF[@]}" 2>&1 \
    | grep -v 'package .* does not exist' | grep -v '^import ' | grep -v '\^' | head -20
echo "  （符号缺失属正常：AndroidX 未提供；只看有没有语法错）"

echo
echo "验证产物: $OUT"
