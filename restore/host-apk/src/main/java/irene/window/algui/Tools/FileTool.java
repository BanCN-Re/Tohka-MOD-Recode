package irene.window.algui.Tools;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public class FileTool {
    private static boolean $assertionsDisabled = false;
    private static final String[][] MIME_MapTable;
    public static final String TAG = "FileTool";

    static {
        try {
            Class.forName("irene.window.algui.Tools.FileTool");
            $assertionsDisabled = true;
            MIME_MapTable = new String[][]{new String[]{".3gp", "video/3gpp"}, new String[]{".apk", "application/vnd.android.package-archive"}, new String[]{".asf", "video/x-ms-asf"}, new String[]{".avi", "video/x-msvideo"}, new String[]{".bin", "application/octet-stream"}, new String[]{".bmp", "image/bmp"}, new String[]{".c", "text/plain"}, new String[]{".class", "application/octet-stream"}, new String[]{".conf", "text/plain"}, new String[]{".cpp", "text/plain"}, new String[]{".doc", "application/msword"}, new String[]{".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}, new String[]{".xls", "application/vnd.ms-excel"}, new String[]{".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}, new String[]{".exe", "application/octet-stream"}, new String[]{".gif", "image/gif"}, new String[]{".gtar", "application/x-gtar"}, new String[]{".gz", "application/x-gzip"}, new String[]{".h", "text/plain"}, new String[]{".htm", "text/html"}, new String[]{".html", "text/html"}, new String[]{".jar", "application/java-archive"}, new String[]{".java", "text/plain"}, new String[]{".jpeg", "image/jpeg"}, new String[]{".jpg", "image/jpeg"}, new String[]{".js", "application/x-javascript"}, new String[]{".log", "text/plain"}, new String[]{".m3u", "audio/x-mpegurl"}, new String[]{".m4a", "audio/mp4a-latm"}, new String[]{".m4b", "audio/mp4a-latm"}, new String[]{".m4p", "audio/mp4a-latm"}, new String[]{".m4u", "video/vnd.mpegurl"}, new String[]{".m4v", "video/x-m4v"}, new String[]{".mov", "video/quicktime"}, new String[]{".mp2", "audio/x-mpeg"}, new String[]{".mp3", "audio/x-mpeg"}, new String[]{".mp4", "video/mp4"}, new String[]{".mpc", "application/vnd.mpohun.certificate"}, new String[]{".mpe", "video/mpeg"}, new String[]{".mpeg", "video/mpeg"}, new String[]{".mpg", "video/mpeg"}, new String[]{".mpg4", "video/mp4"}, new String[]{".mpga", "audio/mpeg"}, new String[]{".msg", "application/vnd.ms-outlook"}, new String[]{".ogg", "audio/ogg"}, new String[]{".pdf", "application/pdf"}, new String[]{".png", "image/png"}, new String[]{".pps", "application/vnd.ms-powerpoint"}, new String[]{".ppt", "application/vnd.ms-powerpoint"}, new String[]{".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, new String[]{".prop", "text/plain"}, new String[]{".rc", "text/plain"}, new String[]{".rmvb", "audio/x-pn-realaudio"}, new String[]{".rtf", "application/rtf"}, new String[]{".sh", "text/plain"}, new String[]{".tar", "application/x-tar"}, new String[]{".tgz", "application/x-compressed"}, new String[]{".txt", "text/plain"}, new String[]{".wav", "audio/x-wav"}, new String[]{".wma", "audio/x-ms-wma"}, new String[]{".wmv", "audio/x-ms-wmv"}, new String[]{".wps", "application/vnd.ms-works"}, new String[]{".xml", "text/plain"}, new String[]{".z", "application/x-compress"}, new String[]{".zip", "application/x-zip-compressed"}, new String[]{"", "*/*"}};
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }

    public static String getMIMEType(String str) {
        File file = new File(str);
        String str2 = "";
        if (!file.exists()) {
            Log.e(TAG, "getMIMEType: 文件不存在");
        } else if (!file.isFile()) {
            Log.e(TAG, "getMIMEType: 当前文件类型是目录");
        } else {
            String name = file.getName();
            int lastIndexOf = name.lastIndexOf(".");
            str2 = "*/*";
            if (lastIndexOf >= 0) {
                String lowerCase = name.substring(lastIndexOf, name.length()).toLowerCase(Locale.getDefault());
                if (lowerCase.length() != 0) {
                    for (String[] strArr : MIME_MapTable) {
                        if (lowerCase.equals(strArr[0])) {
                            str2 = strArr[1];
                        }
                    }
                }
            }
        }
        return str2;
    }

    public static String getSDPath() {
        File file = null;
        if (Environment.getExternalStorageState().equals("mounted")) {
            file = Environment.getExternalStorageDirectory();
        }
        if (!$assertionsDisabled && file == null) {
            throw new AssertionError();
        }
        String file2 = file.toString();
        Log.w(TAG, new StringBuffer().append("getSDPath:").append(file2).toString());
        return file2;
    }

    public static String getFileSuffix(String str) {
        Log.w(TAG, new StringBuffer().append("getFileSuffix: fileName::").append(str).toString());
        return str.substring(str.lastIndexOf(".") + 1, str.length());
    }

    public static boolean createFile(String str, String str2) {
        String stringBuffer = new StringBuffer().append(new StringBuffer().append(str).append(File.separator).toString()).append(str2).toString();
        Log.w(TAG, new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("createFile: filePath::").append(stringBuffer).toString()).append("  File.separator ::").toString()).append(File.separator).toString());
        File file = new File(stringBuffer);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
            Log.w(TAG, "createFile: 文件所在目录不存在，创建目录成功");
        }
        if (file.exists()) {
            Log.e(TAG, new StringBuffer().append("新建文件失败：file.exist()=").append(file.exists()).toString());
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createFolder(String str) {
        return new File(str).mkdir();
    }

    public static boolean deleteFile(String str) {
        File file = new File(str);
        if (file.exists()) {
            boolean delete = file.delete();
            Log.w(TAG, new StringBuffer().append(new StringBuffer().append(file.getName()).append("删除结果：").toString()).append(delete).toString());
            return delete;
        }
        Log.w(TAG, "文件删除失败：文件不存在！");
        return false;
    }

    private static boolean deleteFile(File file) {
        if (file.exists()) {
            boolean delete = file.delete();
            Log.w(TAG, new StringBuffer().append(new StringBuffer().append(file.getName()).append("删除结果：").toString()).append(delete).toString());
            return delete;
        }
        Log.w(TAG, "文件删除失败：文件不存在！");
        return false;
    }

    public static boolean deleteFolder(File file) {
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File file2 : listFiles) {
                if (file2.isFile()) {
                    if (!deleteFile(file2)) {
                        return false;
                    }
                } else if (!deleteFolder(file2)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    public static boolean copyFile(String str, String str2) {
        boolean z = false;
        if (!new File(str).exists()) {
            Log.w(TAG, "copyFile: 源文件不存在");
        } else {
            String stringBuffer = new StringBuffer().append(str2).append(str.substring(str.lastIndexOf(File.separator))).toString();
            if (stringBuffer.equals(str)) {
                Log.w(TAG, "copyFile: 源文件路径和目标文件路径重复");
            } else {
                File file = new File(stringBuffer);
                if (file.exists() && file.isFile()) {
                    Log.w(TAG, "copyFile: 目标目录下已有同名文件!");
                } else {
                    new File(str2).mkdirs();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(str);
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        byte[] bArr = new byte[1024];
                        while (true) {
                            int read = fileInputStream.read(bArr);
                            if (read == -1) {
                                break;
                            }
                            fileOutputStream.write(bArr, 0, read);
                        }
                        fileInputStream.close();
                        fileOutputStream.close();
                        z = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (z) {
                        Log.w(TAG, "copyFile: 复制文件成功!");
                    }
                }
            }
        }
        return z;
    }

    public static boolean copyFolder(String str, String str2) {
        Log.w(TAG, "copyFolder: 复制文件夹开始!");
        File file = new File(str);
        boolean z = false;
        if (!file.exists()) {
            Log.w(TAG, "copyFolder: 源文件夹不存在");
        } else {
            getDirName(str);
            if (str2.equals(str)) {
                Log.w(TAG, "copyFolder: 目标文件夹与源文件夹重复");
            } else {
                File file2 = new File(str2);
                if (!file2.exists()) {
                    file2.mkdirs();
                }
                File[] listFiles = file.listFiles();
                Log.i(TAG, new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("copyFolder: -----files,length::").append(listFiles.length).toString()).append("      files::").toString()).append(listFiles).toString());
                if (listFiles.length == 0) {
                    z = true;
                } else {
                    for (File file3 : listFiles) {
                        if (file3.isFile()) {
                            z = copyFile(file3.getAbsolutePath(), str2);
                        } else if (file3.isDirectory()) {
                            z = copyFolder(file3.getAbsolutePath(), str2);
                        }
                        if (!z) {
                            break;
                        }
                    }
                }
                if (z) {
                    Log.w(TAG, "copyFolder: 复制文件夹成功!");
                }
            }
        }
        return z;
    }

    public static boolean renameTo(String str, String str2) {
        if (str.equals(str2)) {
            Log.w(TAG, "文件重命名失败：新旧文件名绝对路径相同！");
            return false;
        }
        boolean renameTo = new File(str).renameTo(new File(str2));
        Log.w(TAG, new StringBuffer().append("文件重命名是否成功：").append(renameTo).toString());
        return renameTo;
    }

    public static boolean renameTo(File file, String str) {
        boolean renameTo = file.renameTo(new File(new StringBuffer().append(new StringBuffer().append(file.getParentFile()).append(File.separator).toString()).append(str).toString()));
        Log.w(TAG, new StringBuffer().append("文件重命名是否成功：").append(renameTo).toString());
        return renameTo;
    }

    public static long getFileSize(String str) {
        File file = new File(str);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        Log.e(TAG, "getFileSize: error!");
        return -1;
    }

    private static long getFileSize(File file) {
        if (file.isFile()) {
            return file.length();
        }
        return -1;
    }

    public static long getFolderSize(File file) {
        long folderSize;
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            long j = 0;
            for (File file2 : listFiles) {
                if (file2.isFile()) {
                    folderSize = getFileSize(file2);
                } else {
                    folderSize = getFolderSize(file2);
                }
                j += folderSize;
            }
            return j;
        }
        return -1;
    }

    public static String formatSize(long j) {
        DecimalFormat decimalFormat = new DecimalFormat("####.00");
        if (j < 1024) {
            return new StringBuffer().append(j).append("Byte").toString();
        }
        if (j < 1048576) {
            return new StringBuffer().append(decimalFormat.format(((float) j) / 1024.0f)).append("KB").toString();
        }
        if (j < 1073741824) {
            return new StringBuffer().append(decimalFormat.format((((float) j) / 1024.0f) / 1024.0f)).append("MB").toString();
        }
        if (j < 1099511627776L) {
            return new StringBuffer().append(decimalFormat.format(((((float) j) / 1024.0f) / 1024.0f) / 1024.0f)).append("GB").toString();
        }
        return "size: error";
    }

    public static String formatTime(long j) {
        return new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault()).format(new Date(j));
    }

    public static File[] getFileList(String str) {
        File file = new File(str);
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                return listFiles;
            }
            return null;
        }
        return null;
    }

    private static String getDirName(String str) {
        if (str.endsWith(File.separator)) {
            str = str.substring(0, str.lastIndexOf(File.separator));
        }
        return str.substring(str.lastIndexOf(File.separator) + 1);
    }

    public static int getFileCount(File file) {
        return file.listFiles().length;
    }

    public static String getFileName(String str) {
        int lastIndexOf = str.lastIndexOf("/");
        int lastIndexOf2 = str.lastIndexOf(".");
        if (lastIndexOf != -1 && lastIndexOf2 != -1) {
            return str.substring(lastIndexOf + 1, lastIndexOf2);
        }
        return null;
    }

    public static String getFileNameWithSuffix(String str) {
        int lastIndexOf = str.lastIndexOf("/");
        if (lastIndexOf != -1) {
            return str.substring(lastIndexOf + 1);
        }
        return null;
    }

    public static String ReadTxtFile(String str) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            File file = new File(str);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "utf-8"));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuffer.append(new StringBuffer().append(readLine).append("\n").toString());
            }
            fileInputStream.close();
        } catch (Exception e) {
        }
        return stringBuffer.toString();
    }

    public static String saveFilePlt(Context context, String str, String str2) {
        try {
            String stringBuffer = new StringBuffer().append(new StringBuffer().append(context.getExternalFilesDir("").getAbsolutePath()).append(File.separator).toString()).append("lensun").toString();
            new File(stringBuffer).mkdirs();
            File file = new File(stringBuffer, new StringBuffer().append(str2).append(".plt").toString());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(str.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
