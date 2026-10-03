// 最小测试 payload：验证 dlopen 注入链路是否真的работает。
// 编译: aarch64-linux-android21-clang -O2 -shared -o libprobe.so probe.c
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>

#define TAG "probe"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

__attribute__((constructor))
static void on_load(void)
{
    LOGI("=== payload loaded in pid %d ===", getpid());

    /* 读一下自己的名字，证明确实在目标进程里 */
    char buf[256] = {0};
    FILE *f = fopen("/proc/self/cmdline", "r");
    if (f) {
        size_t n = fread(buf, 1, sizeof(buf) - 1, f);
        (void)n;
        fclose(f);
        LOGI("host process cmdline: %s", buf);
    }

    /* 确认目标进程里有没有 libil2cpp */
    f = fopen("/proc/self/maps", "r");
    if (f) {
        char line[512];
        while (fgets(line, sizeof(line), f)) {
            if (strstr(line, "libil2cpp.so")) {
                LOGI("libil2cpp mapping: %s", line);
                break;
            }
        }
        fclose(f);
    }
    LOGI("=== probe done ===");
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    LOGI("JNI_OnLoad vm=%p", vm);
    return JNI_VERSION_1_6;
}
