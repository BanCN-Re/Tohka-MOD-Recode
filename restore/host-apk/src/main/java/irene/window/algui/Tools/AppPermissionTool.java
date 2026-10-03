package irene.window.algui.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.location.LocationRequestCompat;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class AppPermissionTool {
    public static final String TAG = "AppPermissionTool";
    private static int index = 0;

    public static void initPermission(Context context, Activity activity) {
        boolean z;
        if (context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            z = true;
        } else {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.RESTART_PACKAGES") != 0) {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND") != 0) {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS") != 0) {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.SYSTEM_ALERT_WINDOW") != 0) {
            z = false;
        }
        if (!z) {
            activity.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.RECORD_AUDIO", "android.permission.CAMERA", "android.permission.READ_CALENDAR", "android.permission.CALL_PHONE", "android.permission.SEND_SMS", "android.permission.READ_CONTACTS", "android.permission.BODY_SENSORS", "android.permission.READ_CALL_LOG"}, 0);
            activity.requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO", "android.permission.RESTART_PACKAGES", "android.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND", "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.WRITE_EXTERNAL_STORAGE"}, LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY);
        }
        if (Settings.canDrawOverlays(context)) {
            return;
        }
        activity.startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(new StringBuffer().append("package:").append(context.getPackageName()).toString())), 0);
    }

    public static void storePermission(Context context, Activity activity) {
        boolean z;
        if (context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            z = true;
        } else {
            z = false;
        }
        if (context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0 ? z : false) {
            return;
        }
        activity.requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY);
    }

    public static String[] getAllPermissionsFromManifest(Context context) {
        String[] strArr;
        ArrayList arrayList = new ArrayList();
        try {
            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(((Activity) context).getComponentName(), 128);
            if (activityInfo != null && ((ComponentInfo) activityInfo).applicationInfo != null && (strArr = context.getPackageManager().getPackageInfo(((PackageItemInfo) activityInfo).packageName, 4096).requestedPermissions) != null && strArr.length > 0) {
                for (String str : strArr) {
                    arrayList.add(str);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String[]) arrayList.toArray(new String[0]);
    }

    public static void batchApplyPermission(Context context, String... strArr) {
        Activity activity = (Activity) context;
        if (context.checkSelfPermission("android.permission.REQUEST_INSTALL_PACKAGES") != 0) {
            activity.requestPermissions(new String[]{"android.permission.REQUEST_INSTALL_PACKAGES"}, index);
        }
        for (String str : strArr) {
            if (str != null && context.checkSelfPermission(str) != 0) {
                index++;
                if (str.equals("android.permission.SYSTEM_ALERT_WINDOW")) {
                    floatingWindowPermission(context);
                } else {
                    activity.requestPermissions(new String[]{str}, index);
                }
            }
        }
        index++;
    }

    public static void floatingWindowPermission(Context context) {
        if (context == null || Settings.canDrawOverlays(context)) {
            return;
        }
        context.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(new StringBuffer().append("package:").append(context.getPackageName()).toString())));
    }

    public static boolean checkOverlayPermission(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            return false;
        }
        return true;
    }

    public static boolean isAndroidManifestPermissionExist(Context context, String str) {
        try {
            String[] strArr = context.getPackageManager().getPackageInfo(context.getPackageName(), 4096).requestedPermissions;
            if (strArr == null) {
                return false;
            }
            for (String str2 : strArr) {
                if (str2.equals(str)) {
                    return true;
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
