#!/usr/bin/env python3
"""修复 jadx 对匿名类的还原瑕疵。

jadx 把
    new View.OnClickListener() {
        @Override public void onClick(View v) { outer.clearMenu(); }
    }
错误地还原成了
    new View.OnClickListener(this) {
        private final AlGui this$0;
        { this.this$0 = this; }                 // ← self-assign，且把 this 传给了构造函数
        @Override public void onClick(View view) {
            this.this$0.clearMenu();            // ← 本该是 AlGui.this.clearMenu()
        }
    }

根因：jadx 把「外层类引用」（AlGui.this）误判成了「匿名类自己的合成字段 this$0」，
并错误地给构造函数补了一个 outer 参数。

修复方式（两条，都按文本模式做）：
  1. 去掉构造函数里多传的 `(this)` / `(外类)` 实参
  2. 删掉 `private final X this$0;` 字段与 `{ this.this$0 = this; }` 初始化块，
     并把 `this.this$0.` 改回 `X.this.`（合法语法，指外层实例）

另外修：
  3. `new View(this, ctx) { ... }` 这种「多传了一个 this」的匿名子类构造
  4. `new CountDownTimer(...)` 参数不匹配（jadx 丢参数）

用法: python tools/fix_jadx_anon.py [--check]
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")


def fix_file(path, dry=False):
    src = open(path, encoding="utf-8").read()
    orig = src
    fixes = []

    # --- 1) 收集本文件里的匿名类合成字段： private final X this$0; ---
    # 形如：
    #     private final AlGui this$0;
    #     {
    #         this.this$0 = this;
    #     }
    pat_field = re.compile(
        r"\n[ \t]*private final ([A-Za-z_$][\w$.]*) this\$0;\s*\n"
        r"[ \t]*\{\s*\n"
        r"[ \t]*this\.this\$0 = this;\s*\n"
        r"[ \t]*\}\s*\n",
        re.M)

    def drop_field(m):
        fixes.append("删除合成字段 this$0（外层类 %s）" % m.group(1))
        return "\n"
    src = pat_field.sub(drop_field, src)

    # --- 2) this.this$0.xxx  ->  X.this.xxx ---
    # 需要知道每个匿名类对应的外层类。用「同名多次」不现实，
    # 但 jadx 只会用当前文件的最外层类名，取类的声明名。
    m = re.search(r"\bclass\s+([A-Za-z_$][\w$]*)\b", src)
    outer = m.group(1) if m else None
    if outer:
        n = len(re.findall(r"this\.this\$0\.", src))
        if n:
            src = src.replace("this.this$0.", "%s.this." % outer)
            fixes.append("this.this$0. -> %s.this.  (%d 处)" % (outer, n))

    # --- 3) 匿名类构造函数里多传的实参 ---
    # new X(this) {   /  new X(this, ctx) {
    # 只处理「参数列表里第一个是 this」的情况，且该类型不是内部类自己的构造
    def drop_ctor_this(m):
        head, args, tail = m.group(1), m.group(2), m.group(3)
        parts = [a.strip() for a in args.split(",")]
        if parts and parts[0] == "this":
            rest = ", ".join(parts[1:])
            fixes.append("去掉匿名类构造多传的 this: %s(%s)" % (head.split("new ")[-1], args))
            return "%s(%s)%s" % (head, rest, tail)
        return m.group(0)

    src = re.sub(r"(new\s+[\w$.<>]+\s*\()([^(){};]*?)(\)\s*\{)", drop_ctor_this, src)

    # --- 4) new View(this, ctx) 之类：匿名子类多传第一个 this ---
    # （与 3 同款，但参数里有逗号且第一个是 this —— 已被 3 覆盖）

    if src != orig and not dry:
        open(path, "w", encoding="utf-8").write(src)
    return fixes


def main():
    dry = "--check" in sys.argv
    total = 0
    for dp, dn, fn in os.walk(JAVA):
        for f in sorted(fn):
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            fx = fix_file(p, dry=dry)
            if fx:
                total += 1
                print("%s%s" % ("[dry] " if dry else "", os.path.relpath(p, JAVA)))
                for x in fx[:6]:
                    print("    - %s" % x)
                if len(fx) > 6:
                    print("    ... 还有 %d 项" % (len(fx) - 6))
    print()
    print("%s %d 个文件" % ("需修改" if dry else "已修改", total))


if __name__ == "__main__":
    main()
