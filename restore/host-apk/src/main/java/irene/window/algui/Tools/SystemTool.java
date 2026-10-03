package irene.window.algui.Tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.core.os.EnvironmentCompat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

/* loaded from: classes.dex */
public class SystemTool {
    public static final String TAG = "SystemTool";

    public static boolean isOpenNetwork(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isConnected();
        }
        return false;
    }

    public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name")) || "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER) || "Xiaomi".equalsIgnoreCase(Build.BRAND);
    }

    private static String getSystemProperty(String str) {
        try {
            try {
                return (String) Class.forName("android.os.SystemProperties").getMethod("get", Class.forName("java.lang.String")).invoke(null, str);
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return "";
        }
    }

    public static boolean OpenMIUIPerformanceMode(Context context) {
        if (!isMIUI()) {
            return false;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.fuelgauge.PowerModeSettings"));
        context.startActivity(intent);
        return true;
    }

    public static boolean inspectRootPermission() {
        // 还原修正: jadx 丢失了变量声明（原 smali 里是 String[] xxx = ...）
        String[] paths = new String[]{"/system/bin/", "/system/xbin/"};
        return false;
    }

    public static String getSELinuxMode() {
        if (Build.VERSION.SDK_INT >= 26) {
            String property = System.getProperty("selinux.mode", "");
            if ("enforcing".equals(property)) {
                return "强制模式";
            }
            if ("permissive".equals(property)) {
                return "宽容模式";
            }
        } else {
            String legacySELinuxMode = getLegacySELinuxMode();
            if ("Enforcing".equals(legacySELinuxMode)) {
                return "强制模式";
            }
            if ("Permissive".equals(legacySELinuxMode)) {
                return "宽容模式";
            }
        }
        return "";
    }

    private static String getLegacySELinuxMode() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("getenforce").getInputStream()));
            String readLine = bufferedReader.readLine();
            bufferedReader.close();
            return readLine;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isSELinuxPermissive() {
        String sELinuxStatus = getSELinuxStatus();
        return sELinuxStatus != null && sELinuxStatus.equals("Permissive");
    }

    private static String getSELinuxStatus() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/sys/fs/selinux/enforce"));
            String readLine = bufferedReader.readLine();
            bufferedReader.close();
            return readLine.equals("1") ? "Enforcing" : "Permissive";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getKernelVersion() {
        String str = "";
        try {
            Process start = new ProcessBuilder(new String[0]).command("/system/bin/uname", "-a").redirectErrorStream(true).start();
            start.waitFor();
            InputStream inputStream = start.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                str = new StringBuffer().append(str).append(new StringBuffer().append(readLine).append("\n").toString()).toString();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
        return str;
    }

    public static String getNetworkStatus(Context context) {
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.ACCESS_NETWORK_STATE")) {
            return "程序清单xml文件中没有声明android.permission.ACCESS_NETWORK_STATE权限！";
        }
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            switch (activeNetworkInfo.getType()) {
                case 0:
                    switch (activeNetworkInfo.getSubtype()) {
                        case 1:
                            return "2G GPRS Connected";
                        case 2:
                            return "2G EDGE Connected";
                        case 3:
                            return "3G UMTS Connected";
                        case 4:
                            return "2G CDMA Connected";
                        case 5:
                            return "3G EVDO_0 Connected";
                        case 6:
                            return "3G EVDO_A Connected";
                        case 7:
                            return "2G 1xRTT Connected";
                        case 8:
                            return "3G HSDPA Connected";
                        case 9:
                            return "3G HSUPA Connected";
                        case 10:
                            return "3G HSPA Connected";
                        case 11:
                            return "2G IDEN Connected";
                        case 12:
                            return "3G EVDO_B Connected";
                        case 13:
                            return "4G LTE Connected";
                        case 14:
                            return "3G EHRPD Connected";
                        case 15:
                            return "3G HSPAP Connected";
                        case 16:
                        case 17:
                        case 18:
                        case 19:
                        default:
                            return "Unknown Network Connected";
                        case 20:
                            return "5G NR Connected";
                    }
                case 1:
                    return "Wifi Connected";
                default:
                    return "Unknown Network Connected";
            }
        }
        return "No Network Connection";
    }

    public static int getBatteryLevel(Context context) {
        Intent registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        return (int) ((registerReceiver.getIntExtra("level", -1) / registerReceiver.getIntExtra("scale", -1)) * 100);
    }

    public static String formatMemorySize(long j) {
        if (j <= 0) {
            return "0 B";
        }
        double d = j;
        double d2 = 1024;
        int log10 = (int) (Math.log10(d) / Math.log10(d2));
        return String.format("%.2f %s", new Double(d / Math.pow(d2, log10)), new String[]{"B", "KB", "MB", "GB", "TB"}[log10]);
    }

    public static String getTotalMemory(Context context, boolean z) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        if (z) {
            return formatMemorySize(memoryInfo.totalMem);
        }
        return new StringBuffer().append(memoryInfo.totalMem).append("").toString();
    }

    public static String getMemoryUsage(Context context, boolean z) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(memoryInfo);
        if (z) {
            return formatMemorySize(memoryInfo.availMem);
        }
        return new StringBuffer().append(memoryInfo.availMem).append("").toString();
    }

    public static String getTotalStorage(boolean z) {
        long totalSpace = Environment.getDataDirectory().getTotalSpace();
        if (z) {
            return formatMemorySize(totalSpace);
        }
        return new StringBuffer().append(totalSpace).append("").toString();
    }

    public static String getAvailableStorage(boolean z) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSizeLong = statFs.getBlockSizeLong();
            long availableBlocksLong = statFs.getAvailableBlocksLong();
            if (z) {
                return formatMemorySize(blockSizeLong * availableBlocksLong);
            }
            return new StringBuffer().append(blockSizeLong * availableBlocksLong).append("").toString();
        }
        return "null";
    }

    public static String getScreenResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        return new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(i).append("x").toString()).append(displayMetrics.heightPixels).toString()).append("px").toString();
    }

    public static int getRealScreenDP(Context context) {
        // 还原修正: jadx 丢了局部变量（原为 DisplayMetrics dm = ...）
        android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float f = dm.density;
        return Math.min((int) (dm.widthPixels / f), (int) (dm.heightPixels / f));
    }

    public static String printSystemInfo() {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
        StringBuilder sb = new StringBuilder();
        sb.append("_______  系统信息  ").append(format).append(" ______________");
        sb.append("\nID                 :").append(Build.ID);
        sb.append("\nBRAND              :").append(Build.BRAND);
        sb.append("\nMODEL              :").append(Build.MODEL);
        sb.append("\nRELEASE            :").append(Build.VERSION.RELEASE);
        sb.append("\nSDK                :").append(Build.VERSION.SDK);
        sb.append("\n_______ OTHER _______");
        sb.append("\nBOARD              :").append(Build.BOARD);
        sb.append("\nPRODUCT            :").append(Build.PRODUCT);
        sb.append("\nDEVICE             :").append(Build.DEVICE);
        sb.append("\nFINGERPRINT        :").append(Build.FINGERPRINT);
        sb.append("\nHOST               :").append(Build.HOST);
        sb.append("\nTAGS               :").append(Build.TAGS);
        sb.append("\nTYPE               :").append(Build.TYPE);
        sb.append("\nTIME               :").append(Build.TIME);
        sb.append("\nINCREMENTAL        :").append(Build.VERSION.INCREMENTAL);
        sb.append("\n_______ CUPCAKE-3 _______");
        sb.append("\nDISPLAY            :").append(Build.DISPLAY);
        sb.append("\n_______ DONUT-4 _______");
        sb.append("\nSDK_INT            :").append(Build.VERSION.SDK_INT);
        sb.append("\nMANUFACTURER       :").append(Build.MANUFACTURER);
        sb.append("\nBOOTLOADER         :").append(Build.BOOTLOADER);
        sb.append("\nCPU_ABI            :").append(Build.CPU_ABI);
        sb.append("\nCPU_ABI2           :").append(Build.CPU_ABI2);
        sb.append("\nHARDWARE           :").append(Build.HARDWARE);
        sb.append("\nUNKNOWN            :").append(EnvironmentCompat.MEDIA_UNKNOWN);
        sb.append("\nCODENAME           :").append(Build.VERSION.CODENAME);
        sb.append("\n_______ GINGERBREAD-9 _______");
        sb.append("\nSERIAL             :").append(Build.SERIAL);
        return sb.toString();
    }

    public static String getIPAddress(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == 0) {
                try {
                    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (networkInterfaces.hasMoreElements()) {
                        Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress nextElement = inetAddresses.nextElement();
                            if (!nextElement.isLoopbackAddress() && (nextElement instanceof Inet4Address)) {
                                return nextElement.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (activeNetworkInfo.getType() == 1) {
                return intIP2StringIP(((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getIpAddress());
            }
        }
        return null;
    }

    private static String intIP2StringIP(int i) {
        return new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(i & 255).append(".").toString()).append((i >> 8) & 255).toString()).append(".").toString()).append((i >> 16) & 255).toString()).append(".").toString()).append((i >> 24) & 255).toString();
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static boolean isHarmonyOs() {
        try {
            Class<?> cls = Class.forName("com.huawei.system.BuildEx");
            return "Harmony".equalsIgnoreCase(cls.getMethod("getOsBrand", new Class[0]).invoke(cls, new Object[0]).toString());
        } catch (Throwable th) {
            return false;
        }
    }

    public static String getHarmonyVersion() {
        return getProp("hw_sc.build.platform.version", "");
    }

    private static String getProp(String str, String str2) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            try {
                String str3 = (String) cls.getDeclaredMethod("get", Class.forName("java.lang.String")).invoke(cls, str);
                return TextUtils.isEmpty(str3) ? str2 : str3;
            } catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return str2;
        }
    }

    public static String getHarmonyDisplayVersion() {
        return Build.DISPLAY;
    }
}
