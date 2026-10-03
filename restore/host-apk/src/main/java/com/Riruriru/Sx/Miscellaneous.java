package com.Riruriru.Sx;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/* loaded from: classes3.dex */
public class Miscellaneous {
    /* renamed from: 可逆加密, reason: contains not printable characters */
    public static String m88(String inStr) {
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;
    }

    /* renamed from: 可逆解密, reason: contains not printable characters */
    public static String m89(String inStr) {
        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (a[i] ^ 't');
        }
        String k = new String(a);
        return k;
    }

    /* renamed from: 写出assets资源文件, reason: contains not printable characters */
    public static boolean m87assets(Context context, String outPath, String fileName) {
        File file = new File(outPath);
        if (!file.exists() && !file.mkdirs()) {
            Log.e("--Method--", "copyAssetsSingleFile: cannot create directory.");
            return false;
        }
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            File outFile = new File(file, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            while (true) {
                int byteRead = inputStream.read(buffer);
                if (-1 != byteRead) {
                    fileOutputStream.write(buffer, 0, byteRead);
                } else {
                    inputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void RunShell(String shell) {
        try {
            Runtime.getRuntime().exec(shell, (String[]) null, (File) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* renamed from: 返回桌面, reason: contains not printable characters */
    public static void m92(Context context) {
        Intent mHomeIntent = new Intent("android.intent.action.MAIN");
        mHomeIntent.addCategory("android.intent.category.HOME");
        mHomeIntent.addFlags(270532608);
        context.startActivity(mHomeIntent);
    }

    /* renamed from: 打开MIUI性能模式, reason: contains not printable characters */
    public static void m90MIUI(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.fuelgauge.PowerModeSettings"));
        context.startActivity(intent);
    }

    /* renamed from: 网络检测, reason: contains not printable characters */
    public static boolean m91(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            return info.isConnected();
        }
        return false;
    }

    public static void StatusNavigationColor(Activity activity, int colorResId) {
        try {
            Window window = activity.getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.setStatusBarColor(activity.getResources().getColor(colorResId));
            window.setNavigationBarColor(activity.getResources().getColor(colorResId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
