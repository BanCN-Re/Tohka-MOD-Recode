#!/usr/bin/env python3
"""Extract and group il2cpp_* API references from both payload string tables."""
import re, collections, json

ARK = r"D:\Work\MuMuP\ArkReCodeHack\work\libArkRe-strings.txt"
CHE = r"D:\Work\MuMuP\ArkReCodeHack\work\libCherryTale-strings.txt"

API = re.compile(r"il2cpp_[A-Za-z0-9_]+")

def load(path):
    rows = []
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue
            sec, s = (line.split("\t", 1) + [""])[:2] if "\t" in line else ("", line)
            rows.append((sec, s))
    return rows

def apis(path):
    """Return dict api -> set(sections) and total mention count."""
    out = collections.defaultdict(set)
    n = 0
    for sec, s in load(path):
        for m in API.findall(s):
            out[m].add(sec)
            n += 1
    return out, n

def bare(name):
    """strip :(true) / : %p style wrappers already excluded by regex"""
    return name

# Grouping by API family, derived from il2cpp C API header layout
GROUPS = [
    ("Runtime / 初始化 (Runtime init)", lambda a: a in {
        "il2cpp_init", "il2cpp_init_utf16", "il2cpp_shutdown", "il2cpp_set_config",
        "il2cpp_set_config_utf16", "il2cpp_set_config_dir", "il2cpp_set_data_dir",
        "il2cpp_set_temp_dir", "il2cpp_set_commandline_arguments",
        "il2cpp_set_commandline_arguments_utf16", "il2cpp_set_memory_callbacks",
        "il2cpp_set_find_plugin_callback", "il2cpp_register_log_callback",
        "il2cpp_register_debugger_agent_transport", "il2cpp_debugger_set_agent_options",
        "il2cpp_set_default_thread_affinity", "il2cpp_override_stack_backtrace",
        "il2cpp_unity_install_unitytls_interface", "il2cpp_runtime_unhandled_exception_policy_set",
        "il2cpp_unhandled_exception", "il2cpp_is_debugger_attached", "il2cpp_is_vm_thread",
        "il2cpp_stats_get_value", "il2cpp_allocation_granularity",
        "il2cpp_object_header_size", "il2cpp_array_object_header_size",
        "il2cpp_offset_of_array_length_in_array_object_header",
        "il2cpp_ready"}),
    ("Domain / Assembly / Image", lambda a: a.startswith("il2cpp_domain_")
        or a.startswith("il2cpp_assembly_") or a.startswith("il2cpp_image_")
        or a.startswith("il2cpp_get_corlib")),
    ("Class 反射", lambda a: a.startswith("il2cpp_class_")),
    ("Type 反射", lambda a: a.startswith("il2cpp_type_")),
    ("Method 反射与调用", lambda a: a.startswith("il2cpp_method_")
        or a in {"il2cpp_runtime_invoke", "il2cpp_runtime_invoke_convert_args",
                 "il2cpp_runtime_object_init", "il2cpp_runtime_object_init_exception",
                 "il2cpp_runtime_class_init", "il2cpp_resolve_icall",
                 "il2cpp_add_internal_call", "il2cpp_object_new", "il2cpp_value_box",
                 "il2cpp_method_get_from_reflection", "il2cpp_class_get_method_from_name",
                 "il2cpp_debug_get_method_info"}),
    ("Field / Property 读写", lambda a: a.startswith("il2cpp_field_")
        or a.startswith("il2cpp_property_") or a.startswith("il2cpp_class_get_field")
        or a.startswith("il2cpp_class_get_propert")),
    ("Object / String / Array", lambda a: a.startswith("il2cpp_object_")
        or a.startswith("il2cpp_string_") or a.startswith("il2cpp_array_")
        or a in {"il2cpp_gchandle_new", "il2cpp_gchandle_new_weakref",
                 "il2cpp_gchandle_get_target", "il2cpp_gchandle_free",
                 "il2cpp_gchandle_foreach_get_target"}),
    ("GC / 内存", lambda a: a.startswith("il2cpp_gc") or a.startswith("il2cpp_gchandle")
        or a in {"il2cpp_alloc", "il2cpp_free", "il2cpp_gc_alloc_fixed",
                 "il2cpp_gc_free_fixed", "il2cpp_gc_wbarrier_set_field"}),
    ("Exception / 线程 / Monitor", lambda a: a.startswith("il2cpp_exception_")
        or a.startswith("il2cpp_thread_") or a.startswith("il2cpp_current_thread_")
        or a.startswith("il2cpp_monitor_")
        or a in {"il2cpp_raise_exception", "il2cpp_get_exception_argument_null",
                 "il2cpp_format_stack_trace", "il2cpp_custom_attrs_construct",
                 "il2cpp_custom_attrs_free", "il2cpp_custom_attrs_get_attr"}),
    ("Dump / 调试辅助", lambda a: a in {"il2cpp_dump_image", "il2cpp_dump_class"}),
]

def group_of(a):
    for g, pred in GROUPS:
        try:
            if pred(a):
                return g
        except Exception:
            pass
    return "其它 / 未分类"

if __name__ == "__main__":
    a_ark, n_ark = apis(ARK)
    a_che, n_che = apis(CHE)
    print("== il2cpp API ==")
    print("ARK: %d unique, %d mentions" % (len(a_ark), n_ark))
    print("CHE: %d unique, %d mentions" % (len(a_che), n_che))
    both = set(a_ark) & set(a_che)
    print("shared: %d ; ark-only: %s ; che-only: %s"
          % (len(both), sorted(set(a_ark) - set(a_che)), sorted(set(a_che) - set(a_ark))))
    print()
    allapi = set(a_ark) | set(a_che)
    buckets = collections.defaultdict(list)
    for a in sorted(allapi):
        buckets[group_of(a)].append(a)
    for g, _ in GROUPS + [("其它 / 未分类", None)]:
        lst = buckets.get(g, [])
        if not lst:
            continue
        print("--- %s (%d) ---" % (g, len(lst)))
        print(", ".join(lst))
        print()
    # emit machine-readable
    with open(r"D:\Work\MuMuP\ArkReCodeHack\restore\tools\il2cpp_apis.json", "w", encoding="utf-8") as f:
        json.dump({"ark": sorted(a_ark), "che": sorted(a_che),
                   "groups": {g: buckets.get(g, []) for g, _ in GROUPS + [("其它 / 未分类", None)]}},
                  f, ensure_ascii=False, indent=1)
