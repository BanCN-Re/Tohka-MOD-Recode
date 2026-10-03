package com.Riruriru.Sx;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/* loaded from: classes3.dex */
public class FloatServiceView extends Service {
    private static final String FILE_PATH = "/sdcard/测试/3/笔记.txt";
    private Thread fileReadThread;
    private FloatControlView floatControlView;
    private Context mContext;
    private TextView permissionStatus;
    private WindowManager wManager;
    private WindowManager.LayoutParams wParams;

    @Override // android.app.Service
    public IBinder onBind(Intent Intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        initView();
        this.floatControlView = new FloatControlView(this.mContext);
        if (this.floatControlView != null) {
            this.floatControlView.showView();
        }
        startFileReadThread();
    }

    private void initView() {
        this.wManager = (WindowManager) this.mContext.getSystemService("window");
        this.wParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 26) {
            this.wParams.type = 2038;
        } else {
            this.wParams.type = 2003;
        }
        this.wParams.flags = 1848;
        this.wParams.gravity = 53;
        this.wParams.x = 60;
        this.wParams.y = 30;
        this.wParams.width = -2;
        this.wParams.height = -2;
        this.wParams.format = 1;
        this.permissionStatus = new TextView(this.mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        this.permissionStatus.setLayoutParams(params);
        this.permissionStatus.setTextSize(10.0f);
        this.permissionStatus.setTextColor(SupportMenu.CATEGORY_MASK);
        this.permissionStatus.setTypeface(Typeface.defaultFromStyle(1));
        this.wManager.addView(this.permissionStatus, this.wParams);
    }

    private void startFileReadThread() {
        this.fileReadThread = new Thread(new Runnable() { // from class: com.Riruriru.Sx.FloatServiceView.1
            @Override // java.lang.Runnable
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String content = FloatServiceView.this.readFileContent(FloatServiceView.FILE_PATH);
                        FloatServiceView.this.updateTextView(content);
                        Thread.sleep(1000L);
                    } catch (IOException e) {
                        FloatServiceView.this.updateTextView("读取文件内容时出错: " + e.getMessage());
                    } catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        this.fileReadThread.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        File file = new File(filePath);
        if (file.exists() && file.isFile() && ContextCompat.checkSelfPermission(this.mContext, "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                String line = br.readLine();
                if (line != null) {
                    content.append(line);
                }
                br.close();
            } catch (Throwable th) {
                try {
                    br.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } else {
            content.append("十香 Bilibili:MY-血-233");
        }
        return content.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTextView(final String content) {
        new Handler(this.mContext.getMainLooper()).post(new Runnable() { // from class: com.Riruriru.Sx.FloatServiceView.2
            @Override // java.lang.Runnable
            public void run() {
                FloatServiceView.this.permissionStatus.setText(content);
            }
        });
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        if (this.floatControlView != null) {
            this.floatControlView.clearView();
        }
        if (this.fileReadThread != null && this.fileReadThread.isAlive()) {
            this.fileReadThread.interrupt();
        }
        if (this.permissionStatus != null) {
            this.wManager.removeView(this.permissionStatus);
        }
    }
}
