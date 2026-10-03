package irene.window.algui.Tools;

import android.R;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import irene.window.algui.AlGuiData;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class AppTool {
    public static final String TAG = "AppTool";
    private static int index = 335;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager;
        NetworkInfo activeNetworkInfo;
        return (context == null || !AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.ACCESS_NETWORK_STATE") || (connectivityManager = (ConnectivityManager) context.getSystemService("connectivity")) == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null || !activeNetworkInfo.isConnected()) ? false : true;
    }

    public static void backToDesktop(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(270532608);
        context.startActivity(intent);
    }

    public static void showNotificationBar(Context context, String str, String str2) {
        Notification.Builder builder = new Notification.Builder(context, "艾琳QQ3353484607");
        if (str == null) {
            str = "通知";
        }
        Notification.Builder contentTitle = builder.setContentTitle(str);
        if (str2 == null) {
            str2 = "内容";
        }
        Notification build = contentTitle.setContentText(str2).setPriority(2).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.sym_def_app_icon).setLargeIcon(ImageTool.getBase64Image(AlGuiData.getOther_Icon_Hacker2())).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(new NotificationChannel("艾琳QQ3353484607", "游戏外挂稳定中...", 3));
        notificationManager.notify(index, build);
        index++;
    }

    public static boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance == 100;
    }

    public static boolean isRootEnabled() {
        // 还原修正: jadx 在 try/finally 嵌套里丢了 r1 声明，按语义重写
        boolean rooted = false;
        java.lang.Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();
            outputStream.write("echo \"test\" >/dev/null\n".getBytes());
            outputStream.flush();
            outputStream.close();
            rooted = process.waitFor() == 0;
        } catch (Exception e) {
            rooted = false;
        } finally {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        }
        return rooted;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0082 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r1v12, types: [java.lang.Process] */
    /* JADX WARN: Type inference failed for: r1v3, types: [java.io.DataOutputStream] */
    /* JADX WARN: Type inference failed for: r1v8, types: [java.lang.Process] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean getRootPermission(Context context) {
        // 还原修正: jadx 多变量类型推断失败（它自己标了 JADX WARN），按语义重写
        java.lang.Process process = null;
        DataOutputStream dataOutputStream = null;
        try {
            String cmd = new StringBuffer().append("chmod 777 ")
                    .append(context.getPackageCodePath()).toString();
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes(new StringBuffer().append(cmd)
                    .append("\n").toString());
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public boolean isInstalled(Context context, String str) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(0);
        if (installedPackages == null) {
            return false;
        }
        Iterator<PackageInfo> it = installedPackages.iterator();
        while (it.hasNext()) {
            if (it.next().packageName.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "0.0";
        }
    }

    public static String getAppName(Context context) {
        try {
            return context.getResources().getString(((PackageItemInfo) context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo).labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "unKnown";
        }
    }

    public static Drawable getAppIcon(Context context, String str) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getApplicationInfo(str, 0).loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAppPackageName(Context context) {
        return context.getPackageName();
    }

    public static boolean isSystemApp(Context context) {
        return (context.getApplicationInfo().flags & 1) != 0;
    }

    public static String getAppSignature(Context context) {
        try {
            Signature[] signatureArr = context.getPackageManager().getPackageInfo(context.getPackageName(), 64).signatures;
            if (signatureArr.length > 0) {
                return signatureArr[0].toCharsString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "null";
    }

    public static int getAppUid(Context context) {
        return context.getApplicationInfo().uid;
    }

    public static List<String> getSameUidAppNames(Context context) {
        int appUid = getAppUid(context);
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(128);
        ArrayList arrayList = new ArrayList();
        for (ApplicationInfo applicationInfo : installedApplications) {
            if (applicationInfo.uid == appUid && !((PackageItemInfo) applicationInfo).packageName.equals(context.getPackageName())) {
                arrayList.add(applicationInfo.loadLabel(packageManager).toString());
            }
        }
        return arrayList;
    }

    public static String getLauncherActivityName(Context context) {
        Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntentForPackage == null) {
            return null;
        }
        return launchIntentForPackage.getComponent().getClassName();
    }

    public static boolean isServiceRunning(Context context, String str) {
        Iterator<ActivityManager.RunningServiceInfo> it = ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE).iterator();
        boolean z = false;
        while (it.hasNext()) {
            if (str.equals(it.next().service.getClassName())) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isDebugMode(Context context) {
        return (context.getApplicationInfo().flags & 2) != 0;
    }
}
