"""dalvik2java.py v2 — 把反汇编出的 smali 文本还原成可读 Java 源码。

v2 修正了 v1 的三个问题：
  1. 方法头解析：`# ---------- name(desc) (access) ----------`
     之前把 access 的括号误当成签名的一部分。
  2. 寄存器映射：参数寄存器是**最后** N 个（.registers 总数 - 参数个数），
     不是前 N 个。之前全错了。
  3. 类型/参数传递：把 pN 正确对应到形参名。

输入：restore/host-apk/disasm/**/*.smali
输出：restore/host-apk/src/**/*.java
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
DIS = os.path.join(ROOT, "restore", "host-apk", "disasm")
SRC = os.path.join(ROOT, "restore", "host-apk", "src")

PRIM = {"V": "void", "Z": "boolean", "B": "byte", "S": "short",
        "C": "char", "I": "int", "J": "long", "F": "float", "D": "double"}

HEAD_RE = re.compile(r"^# -{10} (.*?) -{10}$")


def jtype(desc):
    if desc is None:
        return "Object"
    d = desc.strip()
    if not d:
        return "Object"
    if d in PRIM:
        return PRIM[d]
    if d.startswith("["):
        return jtype(d[1:]) + "[]"
    if d.startswith("L") and d.endswith(";"):
        return d[1:-1].replace("/", ".")
    return d


def simple(n):
    n = n.replace("$", ".")
    return n.rsplit(".", 1)[-1] if "." in n else n


def parse_params(desc):
    """(Ljava/lang/String;I)V -> (['java.lang.String','int'], 'void')"""
    m = re.match(r"^\((.*)\)(.*)$", desc)
    if not m:
        return [], "void"
    ps, ret = m.group(1), m.group(2)
    out, i = [], 0
    while i < len(ps):
        c = ps[i]
        if c == " " or c == "\t":       # 描述符之间可能有空格，跳过
            i += 1
            continue
        if c == "[":
            j = i
            while j < len(ps) and ps[j] == "[":
                j += 1
            if j < len(ps) and ps[j] == "L":
                j = ps.index(";", j)
            out.append(jtype(ps[i:j + 1]))
            i = j + 1
        elif c == "L":
            j = ps.index(";", i)
            out.append(jtype(ps[i:j + 1]))
            i = j + 1
        else:
            out.append(jtype(c))
            i += 1
    return out, jtype(ret.strip())


def parse_target(target):
    """Ljava/lang/Runtime;->getRuntime()Ljava/lang/Runtime;
    -> (owner描述符, 方法名, 方法描述符)"""
    a = target.find("->")
    if a < 0:
        return None, None, None
    owner = target[:a].strip()
    rest = target[a + 2:]
    b = rest.find("(")
    if b < 0:
        return owner, rest, "()V"
    name = rest[:b]
    # 括号配对找方法描述符结尾
    depth = 0
    q = b
    while q < len(rest):
        if rest[q] == "(":
            depth += 1
        elif rest[q] == ")":
            depth -= 1
            if depth == 0:
                break
        q += 1
    desc = rest[b:q + 1] + rest[q + 1:].strip()
    return owner, name, desc


# ---------------------------------------------------------------- 解析
class Method:
    def __init__(self, name, desc, acc):
        self.name = name
        self.desc = desc
        self.acc = acc
        self.lines = []
        self.nregs = 0


def parse_smali(path):
    text = open(path, encoding="utf-8").read().splitlines()
    cls = sup = None
    fields = []
    methods = []
    cur = None
    for s in text:
        if s.startswith("### ") and cls is None:
            cls = s[4:].split()[0]
            continue
        if s.startswith("### super: "):
            sup = s[len("### super: "):].strip()
            continue
        if s.startswith(".field "):
            parts = s.split()
            if len(parts) >= 4:
                fields.append((parts[1], parts[2], " ".join(parts[3:])))
            elif len(parts) == 3:
                fields.append(("", parts[1], parts[2]))
            continue

        m = HEAD_RE.match(s)
        if m:
            inner = m.group(1)
            # inner 形如: name(params)ret (access)
            # 用括号配对扫描，别用正则（非贪婪会回溯错）
            p = inner.find("(")
            if p < 0:
                cur = None
                continue
            depth = 0
            q = p
            while q < len(inner):
                if inner[q] == "(":
                    depth += 1
                elif inner[q] == ")":
                    depth -= 1
                    if depth == 0:
                        break
                q += 1
            if depth != 0:
                cur = None
                continue
            name = inner[:p]
            params = inner[p:q + 1]
            rest = inner[q + 1:].strip()
            # rest = 返回类型 + 可选 (access)
            acc = ""
            if rest.endswith(")"):
                r2 = rest.rfind("(")
                if r2 >= 0:
                    acc = rest[r2 + 1:-1]
                    rest = rest[:r2].strip()
            ret = rest
            cur = Method(name, params + ret, acc)
            methods.append(cur)
            continue
        if cur is None:
            continue
        t = s.strip()
        if not t or t.startswith("#") or t.startswith("."):
            continue
        if t.startswith("(无字节码)") or t.startswith("(反汇编失败"):
            cur = None
            continue
        p = t.split(None, 1)
        cur.lines.append((p[0], p[1].strip() if len(p) > 1 else ""))
    return cls, sup, fields, methods


# ---------------------------------------------------------------- 寄存器
class RegMap:
    """把 vN 映射到形参名 / 局部变量名。
    参数占寄存器区间的**末尾**：若总共 R 个寄存器、P 个参数（含 this），
    则参数是 v[R-P .. R-1]。"""

    def __init__(self, nregs, params, is_static):
        self.nregs = nregs
        self.pnames = {}
        nparam_slots = len(params) + (0 if is_static else 1)
        base = max(0, nregs - nparam_slots)
        self.param_base = base
        idx = 0
        if not is_static:
            self.pnames[base] = "this"
            idx = 1
        for k, t in enumerate(params):
            self.pnames[base + idx + k] = "p%d" % k

    def name(self, r):
        m = re.match(r"^[vp](\d+)$", r.strip())
        if not m:
            return r.strip()
        i = int(m.group(1))
        if i in self.pnames:
            return self.pnames[i]
        return "v%d" % i


# ---------------------------------------------------------------- 生成
COND = {
    "if-eqz": "==", "if-nez": "!=", "if-eq": "==", "if-ne": "!=",
    "if-lt": "<", "if-ge": ">=", "if-gt": ">", "if-le": "<=",
    "if-ltz": "<", "if-gtz": ">", "if-lez": "<=",
}


def emit(m, params, ret, cname, rm):
    body = []
    P = "        "
    i = 0
    n = len(m.lines)
    while i < n:
        mn, ops = m.lines[i]

        def pk(k=1):
            return m.lines[i + k] if i + k < n else (None, None)

        # 标签定义行（形如 ":cond_0" 或单独一个词且以冒号开头）
        if mn.startswith(":"):
            body.append(P + mn + ":")
            i += 1
            continue

        # 常量
        if mn == "const-string":
            a = [x.strip() for x in ops.split(",", 1)]
            if len(a) >= 2:
                # smali 里字符串已带引号，直接用
                body.append(P + "String %s = %s;" % (rm.name(a[0]), a[1].strip()))
            i += 1
            continue
        if mn in ("const/4", "const/16", "const", "const/high16"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) >= 2:
                body.append(P + "int %s = %s;" % (rm.name(a[0]), a[1]))
            i += 1
            continue
        if mn in ("const-wide/16", "const-wide/32", "const-wide"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) >= 2:
                body.append(P + "long %s = %sL;" % (rm.name(a[0]), a[1]))
            i += 1
            continue
        if mn == "const-class":
            a = [x.strip() for x in ops.split(",")]
            if len(a) >= 2:
                body.append(P + "Class<?> %s = %s.class;" % (rm.name(a[0]), jtype(a[1])))
            i += 1
            continue

        # 对象创建 / 类型转换
        if mn == "new-instance":
            a = [x.strip() for x in ops.split(",")]
            if len(a) >= 2:
                body.append(P + "%s %s;   // new-instance" %
                            (jtype(a[1]), rm.name(a[0])))
            i += 1
            continue
        if mn == "check-cast":
            a = [x.strip() for x in ops.split(",")]
            if len(a) >= 2:
                body.append(P + "%s = (%s) %s;" %
                            (rm.name(a[0]), jtype(a[1]), rm.name(a[0])))
            i += 1
            continue

        # 调用
        if mn.startswith("invoke-"):
            target = ops.split(",")[-1].strip()
            regs = [r.strip() for r in ops.split(",")[:-1]]
            owner_d, mname, mdesc = parse_target(target)
            if owner_d and mname:
                # owner_d 已经是 L...; 形式
                owner = jtype(owner_d if owner_d.startswith("L") else "L" + owner_d + ";")
                _, rt = parse_params(mdesc)
                if mname == "<init>":
                    tgt = rm.name(regs[0]) if regs else "?"
                    args = ", ".join(rm.name(r) for r in regs[1:])
                    body.append(P + "%s = new %s(%s);" % (tgt, simple(owner), args))
                    i += 1
                    continue
                if mn in ("invoke-virtual", "invoke-interface", "invoke-super"):
                    recv = rm.name(regs[0]) if regs else "?"
                    argregs = regs[1:]
                else:
                    recv = simple(owner)
                    argregs = regs
                args = ", ".join(rm.name(r) for r in argregs)
                call = "%s.%s(%s)" % (recv, mname, args)
                nm2, op2 = pk()
                if nm2 == "move-result-object":
                    body.append(P + "Object %s = %s;" % (rm.name(op2), call))
                    i += 2
                    continue
                if nm2 in ("move-result", "move-result-wide"):
                    body.append(P + "%s %s = %s;" % (rt, rm.name(op2), call))
                    i += 2
                    continue
                if rt == "void":
                    body.append(P + call + ";")
                else:
                    body.append(P + "// 丢弃返回值: " + call + ";")
            i += 1
            continue

        # 字段
        if mn.startswith("iget"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 3:
                fname = a[2].split("->")[-1].split(":")[0]
                ft = jtype(a[2].split(":")[-1])
                body.append(P + "%s %s = %s.%s;" %
                            (ft, rm.name(a[0]), rm.name(a[1]), fname))
            i += 1
            continue
        if mn.startswith("iput"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 3:
                fname = a[2].split("->")[-1].split(":")[0]
                body.append(P + "%s.%s = %s;" %
                            (rm.name(a[1]), fname, rm.name(a[0])))
            i += 1
            continue
        if mn.startswith("sget"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 2:
                owner = simple(jtype("L" + a[1].split("->")[0] + ";"))
                fname = a[1].split("->")[-1].split(":")[0]
                ft = jtype(a[1].split(":")[-1])
                body.append(P + "%s %s = %s.%s;" % (ft, rm.name(a[0]), owner, fname))
            i += 1
            continue
        if mn.startswith("sput"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 2:
                owner = simple(jtype("L" + a[1].split("->")[0] + ";"))
                fname = a[1].split("->")[-1].split(":")[0]
                body.append(P + "%s.%s = %s;" % (owner, fname, rm.name(a[0])))
            i += 1
            continue

        # 数组
        if mn == "new-array":
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 3:
                body.append(P + "%s %s = new %s[%s];" %
                            (jtype(a[2]), rm.name(a[0]), jtype(a[2]), rm.name(a[1])))
            i += 1
            continue
        if mn == "array-length":
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 2:
                body.append(P + "int %s = %s.length;" % (rm.name(a[0]), rm.name(a[1])))
            i += 1
            continue
        if mn.startswith("aget"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 3:
                body.append(P + "var %s = %s[%s];" %
                            (rm.name(a[0]), rm.name(a[1]), rm.name(a[2])))
            i += 1
            continue
        if mn.startswith("aput"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 3:
                body.append(P + "%s[%s] = %s;" %
                            (rm.name(a[1]), rm.name(a[2]), rm.name(a[0])))
            i += 1
            continue

        # 跳转
        if mn in COND:
            parts = [x.strip() for x in ops.split(",")]
            last = parts[-1]
            if mn.endswith("z"):
                lhs = rm.name(parts[0])
                body.append(P + "if (%s %s 0) goto %s;" % (lhs, COND[mn], last))
            else:
                body.append(P + "if (%s %s %s) goto %s;" %
                            (rm.name(parts[0]), COND[mn], rm.name(parts[1]), last))
            i += 1
            continue
        if mn.startswith("goto"):
            body.append(P + "goto %s;" % ops.strip())
            i += 1
            continue

        # 返回
        if mn == "return-void":
            body.append(P + "return;")
            i += 1
            continue
        if mn in ("return", "return-object", "return-wide"):
            body.append(P + "return %s;" % rm.name(ops))
            i += 1
            continue

        # 异常
        if mn == "move-exception":
            body.append(P + "catch (Exception %s) {" % rm.name(ops))
            i += 1
            continue
        if mn == "throw":
            body.append(P + "throw %s;" % rm.name(ops))
            i += 1
            continue

        # 算术 / 移动 / 其它：保留为可读注释，不丢失信息
        if mn.startswith("move"):
            a = [x.strip() for x in ops.split(",")]
            if len(a) == 2:
                body.append(P + "var %s = %s;" % (rm.name(a[0]), rm.name(a[1])))
                i += 1
                continue
        if mn in ("nop",):
            i += 1
            continue
        body.append(P + "// %-14s %s" % (mn, ops))
        i += 1

    return body or [P + "// (空)"]


def gen(cls, sup, fields, methods, pkg):
    cname = simple(cls)
    o = ["package %s;" % pkg, "",
         "// 由 replicate/dalvik2java.py 从 smali 还原（语义等价，非原始源码）",
         "// 原始类: %s" % cls]
    if sup and sup != "Ljava/lang/Object;":
        o.append("// 原始父类: %s" % jtype(sup))
    o.append("")
    if fields:
        o.append("    // ========== 字段 ==========")
        for acc, name, typ in fields:
            mod = "public" if "public" in acc else ("protected" if "protected" in acc else "private")
            if "static" in acc:
                mod += " static"
            if "final" in acc:
                mod += " final"
            o.append("    %s %s %s;" % (mod, jtype(typ), name))
        o.append("")

    for m in methods:
        params, ret = parse_params(m.desc)
        is_static = "static" in m.acc
        # 估算寄存器总数：扫描出现的最大编号
        maxr = 0
        for mn, ops in m.lines:
            for tok in re.findall(r"\b[vp](\d+)\b", ops):
                maxr = max(maxr, int(tok))
        nparams = len(params) + (0 if is_static else 1)
        nregs = max(maxr + 1, nparams)
        rm = RegMap(nregs, params, is_static)

        mod = "public" if "public" in m.acc else ("protected" if "protected" in m.acc else "private")
        if is_static:
            mod += " static"
        arglist = ", ".join("%s p%d" % (t, k) for k, t in enumerate(params))
        if "constructor" in m.acc:
            sig = "%s(%s)" % (cname, arglist)
        else:
            sig = "%s %s(%s)" % (ret, m.name, arglist)

        o.append("    // ========== 原始: %s%s  [%s] ==========" % (m.name, m.desc, m.acc))
        o.append("    %s %s {" % (mod, sig))
        if not m.lines:
            o.append("        // (无字节码)")
        else:
            o.extend(emit(m, params, ret, cname, rm))
        o.append("    }")
        o.append("")
    return "\n".join(o) + "\n"


def main():
    os.makedirs(SRC, exist_ok=True)
    n = 0
    rep = []
    for dp, dn, fn in os.walk(DIS):
        for f in sorted(fn):
            if not f.endswith(".smali"):
                continue
            cls, sup, fields, methods = parse_smali(os.path.join(dp, f))
            if not cls:
                rep.append("失败: %s" % f)
                continue
            pkg = cls.rsplit("/", 1)[0].replace("/", ".")
            java = gen(cls, sup, fields, methods, pkg)
            outp = os.path.join(SRC, cls.replace("/", os.sep) + ".java")
            os.makedirs(os.path.dirname(outp), exist_ok=True)
            open(outp, "w", encoding="utf-8").write(java)
            n += 1
            rep.append("%-46s %2d字段 %2d方法" % (cls, len(fields), len(methods)))
    open(os.path.join(SRC, "_report.txt"), "w", encoding="utf-8").write("\n".join(rep) + "\n")
    print("还原 %d 个类 -> %s" % (n, SRC))


if __name__ == "__main__":
    main()
