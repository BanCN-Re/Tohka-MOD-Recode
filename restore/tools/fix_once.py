#!/usr/bin/env python3
"""最终修复（一次到位）：jadx 匿名类/内部类还原瑕疵的完整处理。

从干净基线（jadx 原始输出）出发，一次做完所有修复，避免多轮脚本互相干扰。

## 修复项

### 1. 匿名类的 this$0（类型 1）
```java
new X(this) / new X() {
    private final AlGui this$0;      // 删
    { this.this$0 = this; }          // 删（this 是匿名类实例，类型不匹配）
    void f() { this.this$0.aContext } // -> AlGui.this.aContext
}
```
判据：初始化块是 `this.this$0 = this;`（自赋值，类型必不兼容）

### 2. 真内部类的 this$0（类型 2）—— 保留
```java
public class AnonymousClass100000004 {
    private final AlGui this$0;
    public AnonymousClass100000004(AlGui alGui) {
        this.this$0 = alGui;          // 参数赋值，类型正确
    }
}
```
判据：初始化块是 `this.this$0 = <标识符>;` 且不是 `this`
→ 整类保留；只把构造调用里的第一个实参留着（它确实是外层实例）

### 3. val$ 捕获变量
字段 `private final T val$name;` + 赋值 `this.val$name = <arg>;`
→ 删字段与赋值，把 `this.val$name` 换成 `<arg>`（按初始化块的位置映射）

### 4. 匿名类构造参数
`new X(args) {` → `new X() {`（真内部类不动）

### 5. 零散
- `android.R.style.Animation.X` → `Animation_X`
- `setAppCacheEnabled` 调用删除

## 处理顺序（重要）
  1. val$ 捕获（要先读实参名）
  2. 类型 1 的 this$0（删字段 + 改引用 + 去参数）
  3. 类型 2 的 this$0（什么都不做）
  4. 剩余匿名类去参数
  5. 零散

## 用法
    python tools/fix_once.py --check
    python tools/fix_once.py
"""
import os
import re
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
HOST = os.path.join(os.path.dirname(HERE), "host-apk")
JAVA = os.path.join(HOST, "src", "main", "java")
BACKUP = os.path.join(HOST, "_src-backup-before-fix")


def match_brace(src, start):
    depth = 0
    i = start
    n = len(src)
    while i < n:
        c = src[i]
        if c == '"':
            i += 1
            while i < n and src[i] != '"':
                if src[i] == "\\":
                    i += 1
                i += 1
        elif c == "'":
            i += 1
            while i < n and src[i] != "'":
                if src[i] == "\\":
                    i += 1
                i += 1
        elif c == "/" and i + 1 < n and src[i + 1] == "/":
            while i < n and src[i] != "\n":
                i += 1
        elif c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def main_class(src):
    m = re.search(r"^public\s+(?:final\s+)?class\s+([A-Za-z_$][\w$]*)", src, re.M)
    if not m:
        m = re.search(r"^class\s+([A-Za-z_$][\w$]*)", src, re.M)
    return m.group(1) if m else None


# ---------------------------------------------------------------- 1. val$
def step_val_capture(src, log):
    if "val$" not in src:
        return src
    total = 0
    for _ in range(2000):
        hit = None
        for m in re.finditer(r"new\s+([A-Za-z_$][\w$.<>]*)\s*\(([^()]*)\)\s*\{", src):
            args = [a.strip() for a in m.group(2).split(",") if a.strip()]
            if not args or not all(re.match(r"^[A-Za-z_$][\w$]*$", a) for a in args):
                continue
            bo = src.find("{", m.end() - 1)
            if bo < 0:
                continue
            bc = match_brace(src, bo)
            if bc < 0:
                continue
            body = src[bo + 1:bc]
            if "val$" not in body:
                continue
            hit = (args, bo, bc, body)
            break
        if not hit:
            break
        args, bo, bc, body = hit

        assigns = re.findall(r"this\.val\$([\w$]+)\s*=\s*([A-Za-z_$][\w$]*)\s*;", body)
        mp = {}
        if len(assigns) == len(args):
            for i, (f, _) in enumerate(assigns):
                mp[f] = args[i]
        else:
            for f, a in assigns:
                mp[f] = a

        nb = re.sub(r"(?:[A-Za-z_$][\w$]*\.)?this\.val\$([\w$]+)",
                    lambda mm: mp.get(mm.group(1), mm.group(1)), body)
        nb = re.sub(r"[ \t]*private\s+final\s+[\w$.<>\[\]]+\s+val\$[\w$]+\s*;[ \t]*\n",
                    "", nb)
        nb = re.sub(r"[ \t]*this\.val\$[\w$]+\s*=\s*[A-Za-z_$][\w$]*\s*;[ \t]*\n",
                    "", nb)
        total += len(mp)
        src = src[:bo + 1] + nb + src[bc:]

    if total:
        log.append("  val$ 捕获映射 %d 个" % total)
    return src


# ---------------------------------------------------------------- 2. this$0
def step_this0(src, log):
    """处理匿名类的 this$0 自赋值块；真内部类的保留。"""
    mc = main_class(src)
    if not mc:
        return src

    # 找出所有「 private final <T> this$0; { this.this$0 = this; } 」块
    # 但要限定在匿名类范围内 —— 用行级匹配 + 上下文判断
    pat_self = re.compile(
        r"[ \t]*private\s+final\s+" + re.escape(mc) + r"\s+this\$0\s*;\s*\n"
        r"[ \t]*\{\s*\n"
        r"[ \t]*this\.this\$0\s*=\s*this\s*;\s*\n"
        r"[ \t]*\}\s*\n")
    n_self = len(pat_self.findall(src))
    if n_self:
        src = pat_self.sub("", src)
        log.append("  删 this$0 自赋值块 %d 处" % n_self)

    # 引用改： this.this$0. -> Mc.this.
    n_ref = len(re.findall(r"this\.this\$0\.", src))
    if n_ref:
        src = src.replace("this.this$0.", "%s.this." % mc)
        log.append("  this.this$0. -> %s.this. (%d 处)" % (mc, n_ref))
    return src


# ---------------------------------------------------------------- 3. 去参数
def step_drop_args(src, log):
    inner_defs = set(re.findall(r"\bclass\s+(AnonymousClass\w+)\b", src))
    cnt = [0]

    def rep(m):
        cls, args = m.group(1), m.group(2).strip()
        if not args or cls in inner_defs:
            return m.group(0)
        parts = [a.strip() for a in args.split(",") if a.strip()]
        if not all(re.match(r"^[A-Za-z_$][\w$]*$", p) for p in parts):
            return m.group(0)
        cnt[0] += 1
        return "new %s() {" % cls

    src = re.sub(r"new\s+([A-Za-z_$][\w$.<>]*)\s*\(([^()]*)\)\s*\{", rep, src)
    if cnt[0]:
        log.append("  去掉匿名类构造参数 %d 个" % cnt[0])
    return src


# ---------------------------------------------------------------- 4. 零散
def step_misc(src, log):
    if "android.R.style.Animation." in src:
        c = len(re.findall(r"android\.R\.style\.Animation\.(\w+)", src))
        src = re.sub(r"android\.R\.style\.Animation\.(\w+)",
                     lambda m: "android.R.style.Animation_%s" % m.group(1), src)
        log.append("  Animation.X -> Animation_X (%d)" % c)
    if "setAppCacheEnabled" in src:
        c = len(re.findall(r"[ \t]*[\w.$]+\.setAppCacheEnabled\([^)]*\);[ \t]*\n", src))
        src = re.sub(r"[ \t]*[\w.$]+\.setAppCacheEnabled\([^)]*\);[ \t]*\n", "", src)
        if c:
            log.append("  删 setAppCacheEnabled (%d)" % c)
    return src


def main():
    dry = "--check" in sys.argv
    if not dry and os.path.isdir(BACKUP):
        shutil.rmtree(JAVA, ignore_errors=True)
        shutil.copytree(BACKUP, JAVA)
        print("已恢复干净基线")

    tf = 0
    for dp, dn, fn in os.walk(JAVA):
        for f in sorted(fn):
            if not f.endswith(".java"):
                continue
            p = os.path.join(dp, f)
            s0 = open(p, encoding="utf-8").read()
            s = s0
            log = []
            s = step_val_capture(s, log)
            s = step_this0(s, log)
            s = step_drop_args(s, log)
            s = step_misc(s, log)
            if s != s0:
                tf += 1
                print("%s%s" % ("[dry] " if dry else "[fix] ",
                                os.path.relpath(p, JAVA)))
                for x in log:
                    print(x)
                if not dry:
                    open(p, "w", encoding="utf-8").write(s)
    print()
    print("%s %d 个文件" % ("将修改" if dry else "已修改", tf))


if __name__ == "__main__":
    main()
