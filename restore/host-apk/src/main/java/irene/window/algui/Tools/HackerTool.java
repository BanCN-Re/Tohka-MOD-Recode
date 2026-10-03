package irene.window.algui.Tools;

import android.content.Context;
import java.io.File;

/* loaded from: classes.dex */
public class HackerTool {
    public static final String TAG = "HackerTool";

    public static void shell(String str) {
        try {
            Runtime.getRuntime().exec(str, (String[]) null, (File) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void linuxHackerFile(Context context, String str) {
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append(context.getApplicationInfo().nativeLibraryDir).toString()).append("/").toString()).append(str).toString());
        shell(new StringBuffer().append(new StringBuffer().append(context.getApplicationInfo().nativeLibraryDir).append("/").toString()).append(str).toString());
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append(context.getCacheDir()).toString()).append("/").toString()).append(str).toString());
        shell(new StringBuffer().append(new StringBuffer().append(context.getCacheDir()).append("/").toString()).append(str).toString());
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append("/data/data/").toString()).append(context.getPackageName()).toString()).append("/lib/").toString()).append(str).toString());
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("/data/data/").append(context.getPackageName()).toString()).append("/lib/").toString()).append(str).toString());
    }

    public static void linuxHackerFile(Context context, String str, String str2, boolean z) {
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append(context.getApplicationInfo().nativeLibraryDir).toString()).append("/").toString()).append(z ? str : str2).toString());
        shell(new StringBuffer().append(new StringBuffer().append(context.getApplicationInfo().nativeLibraryDir).append("/").toString()).append(z ? str : str2).toString());
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append(context.getCacheDir()).toString()).append("/").toString()).append(z ? str : str2).toString());
        shell(new StringBuffer().append(new StringBuffer().append(context.getCacheDir()).append("/").toString()).append(z ? str : str2).toString());
        shell(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("chmod 777 ").append("/data/data/").toString()).append(context.getPackageName()).toString()).append("/lib/").toString()).append(z ? str : str2).toString());
        StringBuffer append = new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("/data/data/").append(context.getPackageName()).toString()).append("/lib/").toString());
        if (!z) {
            str = str2;
        }
        shell(append.append(str).toString());
    }
}
