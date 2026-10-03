#!/usr/bin/env python3
"""精准修复 jadx 的匿名类还原瑕疵。

## 问题模式

jadx 把这种代码：
```java
button.setOnClickListener(new View.OnClickListener() {
    @Override public void onClick(View v) {
        AlGui.this.clearMenu();          // 外层实例引用
    }
});
```
错误还原成了：
```java
button.setOnClickListener(new View.OnClickListener(this) {   // ← 多传了 this
    private final AlGui this$0;                              // ← 合成字段
    {
        this.this$0 = this;                                  // ← 自赋值
    }
    @Override public void onClick(View view) {
        this.this$0.clearMenu();                             // ← 错写成 this.this$0
    }
});
```

根因：jadx 把「外层类实例引用（AlGui.this）」误判为「匿名类自己的合成字段 this$0」，
于是给匿名类的构造补了一个 outer 参数，并生成字段与初始化块。

## 修复

**A. 删除合成字段与初始化块**
```
    private final AlGui this$0;
    {
        this.this$0 = this;
    }
```
→ 整块删掉。

**B. `this.this$0.xxx` → `AlGui.this.xxx`**
外层类名从字段声明里的类型取（不是从文件里的第一个 class！）。

**C. `new X(this)` / `new X(this, ...)` → `new X(...)`**
只对**匿名类**（后面紧跟 `{`）生效；真内部类的构造调用不动。

**D. `new View(this, ctx)` 这类匿名子类**
`new View(this, this.aContext) { ... }` → `new View(this.aContext) { ... }`
即去掉第一个 `this` 实参。

**E. 真内部类的 `new AnonymousClass100000004(this, ...)`**
这种是**真的**要传 outer，**不能动**。判据：类名以 `AnonymousClass` 开头
且该名字在文件里有 `class AnonymousClass... {` 定义。

## 用法
    python tools/fix_jadx_anon2.py --check      # 只看会改什么
    python tools/fix_jadx_anon2.py              # 实际修改
"""
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")

# 合成字段块： private final <Type> this$0;  { this.this$0 = this; }
FIELD_BLOCK = re.compile(
    r"[ \t]*private\s+final\s+([A-Za-z_$][\w$.]*)\s+this\$0\s*;\s*\n"
    r"[ \t]*\{\s*\n"
    r"[ \t]*this\.this\$0\s*=\s*this\s*;\s*\n"
    r"[ \t]*\}\s*\n",
    re.M)


def fix(path, dry=False):
    src = open(path, encoding="utf-8").read()
    orig = src
    log = []

    # 0) 先确定这个文件的「主类名」—— 只有指向主类的合成字段才是 jadx 的误还原
    m = re.search(r"^public\s+class\s+([A-Za-z_$][\w$]*)", src, re.M)
    if not m:
        m = re.search(r"^class\s+([A-Za-z_$][\w$]*)", src, re.M)
    main_cls = m.group(1) if m else None

    # 1) 只删「外层类型 == 主类」的合成字段块
    #    指向 AnonymousClass1000000xx 的是真内部类自己的 this$0，必须保留
    kept_blocks = []

    def drop_block(mm):
        t = mm.group(1)
        if main_cls and t == main_cls:
            kept_blocks.append(t)
            return ""
        return mm.group(0)

    outers = FIELD_BLOCK.findall(src)
    src = FIELD_BLOCK.sub(drop_block, src)
    if kept_blocks:
        log.append("删除 %d 处合成字段 this$0（外层类型 %s）"
                   % (len(kept_blocks), main_cls))
    skipped = [t for t in outers if t != main_cls]
    if skipped:
        log.append("  保留 %d 处真内部类的 this$0（%s）"
                   % (len(skipped), ", ".join(sorted(set(skipped)))))

    # 2) this.this$0.  ->  <MainCls>.this.
    #    注意：真内部类内部的 this.this$0 指向它自己的合成字段，语义正确，不能动。
    #    这里只处理「在匿名类里、指向主类」的情况。
    n = len(re.findall(r"this\.this\$0\.", src))
    if n and main_cls:
        src = src.replace("this.this$0.", "%s.this." % main_cls)
        log.append("this.this$0. -> %s.this.  (%d 处)" % (main_cls, n))

    # 3) 找出文件里定义的真内部类名（这些不能动）
    inner_defs = set(re.findall(r"\bclass\s+(AnonymousClass\w+)\b", src))

    # 4) 匿名类构造多传 this
    #    匹配 new X(...) 后面**紧跟** { 的（即匿名类）
    def strip_this(m):
        # group(1) = "new X("   group(2) = 参数   group(3) = ") {"
        head, args, tail = m.group(1), m.group(2), m.group(3)
        # 参数两侧可能有嵌套括号（我的正则组会连括号一起捕进来），先剥掉
        args = args.strip()
        while args.startswith("(") and args.endswith(")"):
            # 只有配对时才能剥
            depth = 0
            ok = True
            for idx, ch in enumerate(args):
                if ch == "(":
                    depth += 1
                elif ch == ")":
                    depth -= 1
                    if depth == 0 and idx != len(args) - 1:
                        ok = False
                        break
            if not ok:
                break
            args = args[1:-1].strip()

        parts = [a.strip() for a in args.split(",")] if args else []
        if not parts or parts[0] != "this":
            return m.group(0)
        cls = head.split("new")[-1].strip()
        if cls in inner_defs:
            return m.group(0)
        rest = ", ".join(parts[1:])
        log.append("  匿名类构造去 this: new %s(%s) -> (%s)" % (cls, args, rest))
        return "%s%s%s" % (head, rest, tail)

    # new X(args) {   —— 参数里允许嵌套括号（但不含 { } ;）
    src = re.sub(r"(new\s+[A-Za-z_$][\w$.<>]*\s*\()((?:[^(){};]|\([^()]*\))*)(\)\s*\{)",
                 strip_this, src)

    if src != orig and not dry:
        open(path, "w", encoding="utf-8").write(src)
    return log


def main():
    dry = "--check" in sys.argv
    tot_f = tot_fix = 0
    for dp, dn, fn in os.walk(JAVA):
        for f in sorted(fn):
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            log = fix(p, dry)
            if log:
                tot_f += 1
                tot_fix += len(log)
                print("%s%s" % ("[dry] " if dry else "[fix] ",
                                os.path.relpath(p, JAVA)))
                for x in log:
                    print("    " + x)
    print()
    print("%s %d 个文件，%d 项修改" %
          ("将修改" if dry else "已修改", tot_f, tot_fix))


if __name__ == "__main__":
    main()
