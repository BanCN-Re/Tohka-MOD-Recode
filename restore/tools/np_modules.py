#!/usr/bin/env python3
"""Classify payload strings into functional modules with counts + evidence."""
import re, collections, json, sys

ARK = r"D:\Work\MuMuP\ArkReCodeHack\work\libArkRe-strings.txt"
CHE = r"D:\Work\MuMuP\ArkReCodeHack\work\libCherryTale-strings.txt"

def load(path):
    rows = []
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue
            sec, s = line.split("\t", 1) if "\t" in line else ("", line)
            rows.append((sec, s))
    return rows

def uniq(rows):
    seen, out = set(), []
    for sec, s in rows:
        if s in seen:
            continue
        seen.add(s); out.append((sec, s))
    return out

# ---- module rules: (module, predicate, note) ----
R = lambda pat: (lambda s: re.search(pat, s) is not None)

MODULES = [
    ("M1 ImGui 后端 / JNI 桥",
     R(r"^Java_com_example_imgui|GLES3JNIView|ImGuiView|setupImGuiView|imgui_log|com/example/imgui"), ""),
    ("M2 Dear ImGui 1.90.0 控件与渲染内核",
     R(r"Dear ImGui|ImGui::|##(slider|toggle|iconbtn|popup|previewing|current|original|mcp|allheroes|authwindow|cardkey)"
       r"|NewFrame\(\)|NavUpdate|Frag_UV|ProjMtx|Out_Color|#%02X%02X%02X%02X|Texture$|// Methods|// RVA:|// Image |// Dll :"), ""),
    ("M3 il2cpp 反射 API 解析层",
     R(r"^il2cpp_|il2cpp_api_resolved|Failed to initialize il2cpp api|Fallback il2cpp_base|il2cpp_base: %lx"
       r"|Resolved %s via offset|Required liveness functions"), ""),
    ("M4 ESP / 透视绘制",
     R(r"^(esp|espbox|espbone|espline|espname|espdist|esp3d|esp_3d_box|esp_distance|esp_name|espline|espcount|icon_size|scale|icon_center)"
       r"|bone|Bone|GetBoneTransform|wallhack|WallHack|ESP"), ""),
    ("M5 AIMBOT 自瞄",
     R(r"^(selfaimobj|aimtarget|aimtargetobj|aim_offset|aimself|aimselfobj|aimbot)|AIMBOT|aim_"), ""),
    ("M6 GAME FEATURES (游戏机制修改)",
     R(r"^gfeat_|get_IsGameOver|get_active|get_RoleContainer|IsFinish|PreconditionID"), ""),
    ("M7 ROLE / 角色批量管理",
     R(r"^role_|roleFilter|Roles$|RoleData|RoleManager|RoleContainer|RoleStaticData|RoleDataContainer|\[RoleThread\]"), ""),
    ("M8 OBJECT BROWSER 对象浏览器",
     R(r"^object_|objectFilterText|object_handles|FindObjects|FindObjectsOfType|Copy as|Reset order"), ""),
    ("M9 CARD KEY / 卡密授权 UI",
     R(r"cardKey|cardkey|authkb|authime|authclear|authcard|authverify|authorization|##authwindow|markcode|kbback|kbdone"), ""),
    ("M10 LOVE TALK / 剧情 (LineDialog)",
     R(r"^lovetalk_|LineDialog|LineCollectionInfoPanel|CheckPassCondition|drama|dialog"), ""),
    ("M11 Il2CppMCP 服务端 (HTTP/JSON-RPC)",
     R(r"MCP|mcp|jsonrpc|McpConfig|tools/list|tools/call|resources/list|prompts/list|sessionId|clientInfo"
       r"|Mcp-Session-Id|Mcp-Protocol-Version|X-Mcp-Token|tool_calls|stop_gc_world|start_gc_world|allow_gc_stop"
       r"|read_only|uptime_ms|active_connections|last_tool_ms|token_required|kill_on_expire"), ""),
    ("M12 Custom Camera 自定相机",
     R(r"Main Camera|Current Camera|Custom Camera|Alpha Bar|camera|Camera"), ""),
    ("M13 DUMP 类/镜像导出",
     R(r"il2cpp_dump|dump_dir|dump done|Start dumping|Get all assemblies|/files/mcp_dump|/mcp_dump"), ""),
    ("M14 配置持久化 (ModConfig / .txt)",
     R(r"ModConfig|ModShift|TohkaModMode|Tohka MOD|Tohka Custom MOD|cancelcfg|/McpConfig|Config\.txt|config"), ""),
    ("M15 网络 / HTTP 客户端 (libcurl)",
     R(r"curl|libcurl|SOCKS5|Host: |User-Agent|Transfer-Encoding|chunked|Proxy-|CURLOPT"), ""),
    ("M16 翻译 (iass.top)",
     R(r"iass\.top|translate_a|sl=auto|tl=zh-CN"), ""),
    ("M17 静态链接 OpenSSL / BoringSSL",
     R(r"^(SSL|TLS|X509|ASN1|PEM|EVP|BIO|BN_|EC_|RSA_|SHA|MD5|AES|DES|RC4|CRYPTO|OPENSSL|OBJ_|PKCS|OCSP|CMS|ERR_)"
       r"|openssl|crypto/|ssl/|cipher|digest"), ""),
    ("M18 静态链接 libunwind / C++ demangler / STL",
     R(r"libunwind|DW_CFA|itanium_demangle|_ZN|std::|basic_string|allocator|typeinfo for|vtable for"
       r"|operator new|operator delete|__cxa_guard|terminate_handler"), ""),
    ("M19 运行时加载 / 注入 (dlopen, JavaVM, art)",
     R(r"libart\.so|JavaVM|libil2cpp|dlopen|dlsym|/proc/self/cmdline|proc/self/cmdline|libUnityHookTool"
       r"|java/lang/ClassLoader|Success with library"), ""),
    ("M20 图像解码 (PNG/JPEG/GIF 解码器)",
     R(r"bad png sig|bad BMP|bad DC huff|multiple IHDR|tRNS|bad component|IHDR|deflate|inflate|jpeg|\.png|\.jpg"), ""),
    ("M21 触控 / 输入 (MultiTouch)",
     R(r"MultiTouch|Touch point added|MouseX|nativeOnTouchEvent|GamepadRStick|Keypad|MouseRight"), ""),
    ("M22 XZ / 压缩",
     R(r"XzUnpacker|Xz|xz_|LZMA"), ""),
]

def classify(strings):
    buckets = collections.defaultdict(list)
    for sec, s in strings:
        for name, pred, _ in MODULES:
            try:
                if pred(s):
                    buckets[name].append(s)
                    break
            except Exception:
                pass
    return buckets

if __name__ == "__main__":
    ark_rows = load(ARK); che_rows = load(CHE)
    ark = uniq(ark_rows); che = uniq(che_rows)
    ba = classify(ark); bc = classify(che)
    print("%-42s %8s %8s" % ("module", "ARK", "CHE"))
    print("-" * 62)
    for name, _, _ in MODULES:
        print("%-42s %8d %8d" % (name, len(ba.get(name, [])), len(bc.get(name, []))))
    un_a = [s for _, s in ark if not any(p(s) for _, p, _ in MODULES)]
    un_c = [s for _, s in che if not any(p(s) for _, p, _ in MODULES)]
    print("%-42s %8d %8d" % ("(未分类)", len(un_a), len(un_c)))
    print()
    print("total uniq: ARK=%d CHE=%d" % (len(ark), len(che)))
    with open(r"D:\Work\MuMuP\ArkReCodeHack\restore\tools\modules.json", "w", encoding="utf-8") as f:
        json.dump({k: sorted(set(v)) for k, v in ba.items()}, f, ensure_ascii=False, indent=1)
