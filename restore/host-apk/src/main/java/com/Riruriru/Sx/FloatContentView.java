package com.Riruriru.Sx;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import com.google.android.material.card.MaterialCardViewHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes3.dex */
public class FloatContentView extends PopupWindow {
    private File arkReSoFile;
    private File binaryFile;
    private File cherryTaleSoFile;
    private List<Switch> colorSwitches;
    private TextView gameSelectionText;
    private boolean isShowing;
    private Context mContext;
    private ScrollView scrollViewLog;
    private int selectedGame;
    private TextView tvLogOutput;
    public WindowManager wManager;
    public WindowManager.LayoutParams wParams;

    public void clearView() {
    }

    public FloatContentView(Context context) {
        super(context);
        this.isShowing = false;
        this.selectedGame = 0;
        this.colorSwitches = new ArrayList();
        this.mContext = context;
        initView();
        load();
        setupAssets();
    }

    private void setupAssets() {
        new Thread(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    FloatContentView.this.binaryFile = new File(FloatContentView.this.mContext.getFilesDir(), "64");
                    FloatContentView.this.extractAssetFile("64", FloatContentView.this.binaryFile);
                    FloatContentView.this.arkReSoFile = new File(FloatContentView.this.mContext.getFilesDir(), "libArkRe.so");
                    FloatContentView.this.extractAssetFile("libArkRe.so", FloatContentView.this.arkReSoFile);
                    FloatContentView.this.cherryTaleSoFile = new File(FloatContentView.this.mContext.getFilesDir(), "libCherryTale.so");
                    FloatContentView.this.extractAssetFile("libCherryTale.so", FloatContentView.this.cherryTaleSoFile);
                    if (FloatContentView.this.binaryFile.exists()) {
                        FloatContentView.this.setFilePermissions(FloatContentView.this.binaryFile);
                        Runtime.getRuntime().exec("sh -c chmod 777 " + FloatContentView.this.binaryFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void extractAssetFile(String assetFileName, File targetFile) {
        if (!targetFile.exists() || targetFile.length() == 0) {
            try {
                InputStream in = this.mContext.getAssets().open(assetFileName);
                OutputStream out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[8192];
                while (true) {
                    int bytesRead = in.read(buffer);
                    if (bytesRead != -1) {
                        out.write(buffer, 0, bytesRead);
                    } else {
                        in.close();
                        out.close();
                        return;
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    private void extractAssetFile(String assetFileName, File targetFile, String displayName) {
        if (!targetFile.exists() || targetFile.length() == 0) {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                try {
                    try {
                        in = this.mContext.getAssets().open(assetFileName);
                        out = new FileOutputStream(targetFile);
                        byte[] buffer = new byte[8192];
                        long totalBytes = 0;
                        while (true) {
                            int bytesRead = in.read(buffer);
                            if (bytesRead == -1) {
                                break;
                            }
                            out.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        appendLog("✓ " + displayName + "解压完成 (" + totalBytes + " 字节)");
                        if (in != null) {
                            in.close();
                        }
                        out.close();
                        return;
                    } catch (IOException e) {
                        appendLog("✗ " + displayName + "解压失败: " + e.getMessage());
                        if (in != null) {
                            in.close();
                        }
                        if (out != null) {
                            out.close();
                            return;
                        }
                        return;
                    }
                } catch (Throwable th) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e2) {
                            throw th;
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    throw th;
                }
            } catch (IOException e3) {
                return;
            }
        }
        appendLog("✓ " + displayName + "已存在，跳过解压");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFilePermissions(File file) {
        if (file.exists()) {
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);
        }
    }

    private void load() {
        int i;
        this.wManager = (WindowManager) this.mContext.getSystemService("window");
        this.wParams = new WindowManager.LayoutParams();
        WindowManager.LayoutParams layoutParams = this.wParams;
        if (Build.VERSION.SDK_INT >= 26) {
            i = 2038;
        } else {
            i = 2003;
        }
        layoutParams.type = i;
        this.wParams.flags = 1848;
    }

    private void writeToFile(String content) {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "1.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
            showToast("数据已修改");
        } catch (IOException e) {
            showToast("失败: " + e.getMessage());
        }
    }

    private void initColorChangingSwitch(final Switch sw) {
        this.colorSwitches.add(sw);
        Runnable colorRunnable = new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.2
            private int r = 255;
            private int g = 0;
            private int b = 0;
            private int step = 1;

            @Override // java.lang.Runnable
            public void run() {
                sw.setTextColor(Color.rgb(this.r, this.g, this.b));
                if (this.step == 1) {
                    this.r--;
                    this.g++;
                    if (this.g == 255) {
                        this.step = 2;
                    }
                } else if (this.step == 2) {
                    this.g--;
                    this.b++;
                    if (this.b == 255) {
                        this.step = 3;
                    }
                } else if (this.step == 3) {
                    this.b--;
                    this.r++;
                    if (this.r == 255) {
                        this.step = 1;
                    }
                }
                sw.postDelayed(this, 10L);
            }
        };
        sw.post(colorRunnable);
    }

    /* loaded from: classes3.dex */
    public class CustomSwitch extends Switch {
        public CustomSwitch(Context context) {
            super(context);
            initStyle();
        }

        public CustomSwitch(Context context, AttributeSet attrs) {
            super(context, attrs);
            initStyle();
        }

        public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initStyle();
        }

        private void initStyle() {
            setThumbDrawable(createThumbDrawable());
            setTrackDrawable(createTrackDrawable());
            setPadding(20, 20, 20, 20);
        }

        private Drawable createThumbDrawable() {
            GradientDrawable thumbDrawable = new GradientDrawable();
            thumbDrawable.setShape(1);
            thumbDrawable.setColor(-1856508746);
            thumbDrawable.setSize(50, 50);
            return thumbDrawable;
        }

        private Drawable createTrackDrawable() {
            GradientDrawable trackDrawable = new GradientDrawable();
            trackDrawable.setShape(0);
            trackDrawable.setColor(-16776961);
            trackDrawable.setCornerRadius(60.0f);
            trackDrawable.setSize(150, 50);
            return trackDrawable;
        }
    }

    private void initView() {
        LinearLayout linearLayout = new LinearLayout(this.mContext);
        ViewGroup.LayoutParams mainParams = new LinearLayout.LayoutParams(-1, -1);
        linearLayout.setLayoutParams(mainParams);
        int resId = this.mContext.getResources().getIdentifier("srzx", "drawable", this.mContext.getPackageName());
        if (resId != 0) {
            linearLayout.setBackgroundResource(resId);
        } else {
            linearLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        }
        LinearLayout linearLayout2 = new LinearLayout(this.mContext);
        ViewGroup.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(-1, -1);
        linearLayout2.setLayoutParams(mainLayoutParams);
        linearLayout2.setOrientation(1);
        linearLayout.addView(linearLayout2);
        linearLayout.setElevation(10.0f);
        LinearLayout linearLayout3 = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        linearLayout3.setLayoutParams(titleLayoutParams);
        linearLayout3.setPadding(30, 30, 30, 30);
        linearLayout2.addView(linearLayout3);
        TextView title = new TextView(this.mContext);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-2, -2);
        title.setLayoutParams(titleParams);
        title.setText("Riruriru Mod");
        titleParams.gravity = 17;
        title.setTextSize(14.0f);
        title.setTextColor(-1);
        title.setTypeface(Typeface.defaultFromStyle(1));
        linearLayout3.addView(title);
        ScrollView scrollView = new ScrollView(this.mContext);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(-1, -2);
        scrollParams.bottomMargin = 10;
        scrollView.setLayoutParams(scrollParams);
        linearLayout2.addView(scrollView);
        LinearLayout linearLayout4 = new LinearLayout(this.mContext);
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        linearLayout4.setLayoutParams(layoutParams);
        linearLayout4.setOrientation(1);
        linearLayout4.setPadding(20, 0, 20, 20);
        scrollView.addView(linearLayout4);
        LinearLayout linearLayout5 = new LinearLayout(this.mContext);
        ViewGroup.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(-1, -2);
        linearLayout5.setLayoutParams(layoutParams1);
        linearLayout5.setOrientation(1);
        linearLayout5.setPadding(20, 20, 20, 20);
        linearLayout4.addView(linearLayout5);
        TextView labelGame = new TextView(this.mContext);
        labelGame.setText("选择游戏:");
        labelGame.setTextSize(12.0f);
        labelGame.setTextColor(-1);
        labelGame.setPadding(0, 0, 0, 5);
        labelGame.setTypeface(Typeface.defaultFromStyle(1));
        linearLayout5.addView(labelGame);
        LinearLayout linearLayout6 = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams gameSelectionParams = new LinearLayout.LayoutParams(-1, -2);
        gameSelectionParams.setMargins(0, 10, 0, 20);
        linearLayout6.setLayoutParams(gameSelectionParams);
        linearLayout6.setOrientation(0);
        linearLayout6.setGravity(17);
        GradientDrawable gameButtonBg = new GradientDrawable();
        gameButtonBg.setColor(1711276032);
        gameButtonBg.setCornerRadius(15.0f);
        linearLayout6.setBackground(gameButtonBg);
        linearLayout6.setPadding(20, 15, 20, 15);
        this.gameSelectionText = new TextView(this.mContext);
        this.gameSelectionText.setText("樱景物语");
        this.gameSelectionText.setTextSize(12.0f);
        this.gameSelectionText.setTextColor(-1);
        this.gameSelectionText.setPadding(10, 0, 10, 0);
        TextView switchGameButton = new TextView(this.mContext);
        switchGameButton.setText("切换游戏");
        switchGameButton.setTextSize(12.0f);
        switchGameButton.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        switchGameButton.setPadding(15, 8, 15, 8);
        GradientDrawable switchButtonBg = new GradientDrawable();
        switchButtonBg.setColor(-1996488705);
        switchButtonBg.setCornerRadius(10.0f);
        switchGameButton.setBackground(switchButtonBg);
        switchGameButton.setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.FloatContentView.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FloatContentView.this.selectedGame = (FloatContentView.this.selectedGame + 1) % 2;
                FloatContentView.this.gameSelectionText.setText(FloatContentView.this.selectedGame == 0 ? "樱景物语" : "星陨计划");
            }
        });
        linearLayout6.addView(this.gameSelectionText);
        linearLayout6.addView(switchGameButton);
        linearLayout5.addView(linearLayout6);
        LinearLayout injectButtonLayout = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams injectButtonParams = new LinearLayout.LayoutParams(-1, 80);
        injectButtonLayout.setLayoutParams(injectButtonParams);
        injectButtonLayout.setOrientation(0);
        GradientDrawable injectButtonBg = new GradientDrawable();
        injectButtonBg.setColor(-1442814260);
        injectButtonBg.setCornerRadius(40.0f);
        injectButtonLayout.setBackground(injectButtonBg);
        injectButtonLayout.setGravity(17);
        final TextView injectButtonText = new TextView(this.mContext);
        injectButtonText.setText("开始启动");
        injectButtonText.setTextSize(14.0f);
        injectButtonText.setTextColor(-1);
        injectButtonText.setTypeface(Typeface.defaultFromStyle(1));
        injectButtonLayout.addView(injectButtonText);
        injectButtonLayout.setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.FloatContentView.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FloatContentView.this.startInjection(injectButtonText);
            }
        });
        linearLayout5.addView(injectButtonLayout);
        LinearLayout logLayout = new LinearLayout(this.mContext);
        LinearLayout.LayoutParams logLayoutParams = new LinearLayout.LayoutParams(-1, -2);
        logLayoutParams.setMargins(0, 20, 0, 0);
        logLayout.setLayoutParams(logLayoutParams);
        logLayout.setOrientation(1);
        GradientDrawable logBg = new GradientDrawable();
        logBg.setColor(855638016);
        logBg.setCornerRadius(10.0f);
        logLayout.setBackground(logBg);
        this.tvLogOutput = new TextView(this.mContext);
        this.tvLogOutput.setTextSize(12.0f);
        this.tvLogOutput.setTextColor(-1);
        this.tvLogOutput.setPadding(15, 15, 15, 15);
        this.scrollViewLog = new ScrollView(this.mContext);
        FrameLayout.LayoutParams scrollLogParams = new FrameLayout.LayoutParams(-1, MaterialCardViewHelper.DEFAULT_FADE_ANIM_DURATION);
        this.scrollViewLog.setLayoutParams(scrollLogParams);
        this.scrollViewLog.addView(this.tvLogOutput);
        logLayout.addView(this.scrollViewLog);
        linearLayout5.addView(logLayout);
        setWidth(800);
        setHeight(800);
        setContentView(linearLayout);
        setBackgroundDrawable(new ColorDrawable(0));
        setOutsideTouchable(true);
        setFocusable(true);
        if (Build.VERSION.SDK_INT >= 26) {
            setWindowLayoutType(2038);
        } else {
            setWindowLayoutType(2003);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startInjection(final TextView buttonText) {
        final String packageName;
        final File soFile;
        if (this.selectedGame == 0) {
            packageName = "com.neversoft.rpg.erolabs";
            soFile = this.cherryTaleSoFile;
        } else {
            packageName = "com.nerversoft.ark.recode";
            soFile = this.arkReSoFile;
        }
        if (this.binaryFile == null || !this.binaryFile.exists()) {
            showToast("注入工具未准备就绪，请等待...");
            return;
        }
        if (soFile == null || !soFile.exists()) {
            showToast("SO文件未准备就绪，请等待...");
            return;
        }
        buttonText.setEnabled(false);
        buttonText.setText("注入中...");
        this.tvLogOutput.setText("");
        new Thread(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.5
            @Override // java.lang.Runnable
            public void run() {
                FloatContentView.this.executeInjection(packageName, "", soFile.getAbsolutePath(), buttonText);
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void executeInjection(String packageName, String pid, String soPath, final TextView buttonText) {
        appendLog("启动中...");
        try {
            File targetSo = new File(soPath);
            if (this.binaryFile != null && this.binaryFile.exists() && targetSo.exists()) {
                Runtime.getRuntime().exec("chmod 777 " + soPath).waitFor();
                List<String> command = new ArrayList<>();
                command.add(this.binaryFile.getAbsolutePath());
                command.add("-pkg");
                command.add(packageName);
                command.add("-lib");
                command.add(soPath);
                ProcessBuilder builder = new ProcessBuilder(command);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                do {
                } while (reader.readLine() != null);
                final int exitCode = process.waitFor();
                if (this.tvLogOutput != null) {
                    this.tvLogOutput.post(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.6
                        @Override // java.lang.Runnable
                        public void run() {
                            if (exitCode == 0) {
                                FloatContentView.this.tvLogOutput.setText("启动成功 即将退出");
                                FloatContentView.this.tvLogOutput.postDelayed(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.6.1
                                    @Override // java.lang.Runnable
                                    public void run() {
                                        Process.killProcess(Process.myPid());
                                        System.exit(0);
                                    }
                                }, 1000L);
                            } else {
                                FloatContentView.this.tvLogOutput.setText("启动失败：未启动游戏或驱动异常");
                                FloatContentView.this.resetButton(buttonText);
                            }
                        }
                    });
                }
                return;
            }
            this.tvLogOutput.setText("启动失败：未下载驱动内部安装包");
            resetButton(buttonText);
        } catch (Exception e) {
            this.tvLogOutput.setText("启动异常：请检查游戏是否运行");
            resetButton(buttonText);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetButton(final TextView buttonText) {
        if (buttonText != null) {
            buttonText.post(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.7
                @Override // java.lang.Runnable
                public void run() {
                    buttonText.setEnabled(true);
                    buttonText.setText("开始启动");
                }
            });
        }
    }

    private void appendLog(final String text) {
        if (this.tvLogOutput != null) {
            this.tvLogOutput.post(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.8
                @Override // java.lang.Runnable
                public void run() {
                    String currentText;
                    String currentText2 = FloatContentView.this.tvLogOutput.getText().toString();
                    if (!currentText2.isEmpty()) {
                        currentText = currentText2 + "\n" + text;
                    } else {
                        currentText = text;
                    }
                    FloatContentView.this.tvLogOutput.setText(currentText);
                    if (FloatContentView.this.scrollViewLog != null) {
                        FloatContentView.this.scrollViewLog.postDelayed(new Runnable() { // from class: com.Riruriru.Sx.FloatContentView.8.1
                            @Override // java.lang.Runnable
                            public void run() {
                                FloatContentView.this.scrollViewLog.fullScroll(130);
                            }
                        }, 100L);
                    }
                }
            });
        }
    }

    private void showToast(String str) {
        Toast.makeText(this.mContext, str, 1).show();
    }

    public void showView() {
        showAtLocation(getContentView(), 3, 10, 0);
    }
}
