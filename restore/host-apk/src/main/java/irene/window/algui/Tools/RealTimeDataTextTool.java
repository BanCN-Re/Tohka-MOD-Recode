package irene.window.algui.Tools;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.view.Choreographer;
import android.widget.TextView;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public class RealTimeDataTextTool {
    public static final String TAG = "RealTimeDataTextTool";
    private static String ordinaryDataColor = "#9C27B0";

    public static String getOrdinaryDataColor_RGB() {
        return ordinaryDataColor;
    }

    public static void setOrdinaryDataColor_RGB(String str) {
        if (str == null || !str.matches("^#[0-9a-fA-F]{6}$")) {
            return;
        }
        ordinaryDataColor = str;
    }

    public static TextView textAddPower(Context context, TextView textView) {
        if (context != null && textView != null) {
            new Thread(new Runnable(new Handler(Looper.getMainLooper(), context, textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000000
                private final Context val$context_A;
                private final String val$text;
                private final TextView val$textView_A;

                {
                    this.val$context_A = context;
                    this.val$textView_A = textView;
                    this.val$text = r4;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%d%%", new Integer(SystemTool.getBatteryLevel(this.val$context_A)))).toString()).append("</font>").toString()));
                }
            }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000001
                private final Handler val$handler;

                {
                    this.val$handler = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        this.val$handler.sendEmptyMessage(0);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        return textView;
    }

    public static TextView textAddAvailableMemory(Context context, boolean z, TextView textView) {
        if (context == null) {
            return textView;
        }
        if (textView == null) {
            return textView;
        }
        new Thread(new Runnable(new Handler(Looper.getMainLooper(), context, z, textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000002
            private final Context val$context_A;
            private final boolean val$isFormat_A;
            private final String val$text;
            private final TextView val$textView_A;

            {
                this.val$context_A = context;
                this.val$isFormat_A = z;
                this.val$textView_A = textView;
                this.val$text = r5;
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%s", SystemTool.getMemoryUsage(this.val$context_A, this.val$isFormat_A))).toString()).append("</font>").toString()));
            }
        }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000003
            private final Handler val$handler;

            {
                this.val$handler = r1;
            }

            @Override // java.lang.Runnable
            public void run() {
                while (true) {
                    this.val$handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return textView;
    }

    public static TextView textAddAvailableStorage(boolean z, TextView textView) {
        if (textView != null) {
            new Thread(new Runnable(new Handler(Looper.getMainLooper(), z, textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000004
                private final boolean val$isFormat_A;
                private final String val$text;
                private final TextView val$textView_A;

                {
                    this.val$isFormat_A = z;
                    this.val$textView_A = textView;
                    this.val$text = r4;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%s", SystemTool.getAvailableStorage(this.val$isFormat_A))).toString()).append("</font>").toString()));
                }
            }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000005
                private final Handler val$handler;

                {
                    this.val$handler = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        this.val$handler.sendEmptyMessage(0);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        return textView;
    }

    public static TextView textAddTime(String str, TextView textView) {
        if (textView != null) {
            new Thread(new Runnable(new Handler(Looper.getMainLooper(), str, textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000006
                private final String val$text;
                private final TextView val$textView_A;
                private final String val$timeFormat_A;

                {
                    this.val$timeFormat_A = str;
                    this.val$textView_A = textView;
                    this.val$text = r4;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(new SimpleDateFormat(this.val$timeFormat_A != null ? this.val$timeFormat_A : "yyyy年MM月dd日 HH:mm:ss EE", Locale.getDefault()).format((Date) new java.sql.Date(System.currentTimeMillis()))).toString()).append("</font>").toString()));
                }
            }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000007
                private final Handler val$handler;

                {
                    this.val$handler = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        this.val$handler.sendEmptyMessage(0);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        return textView;
    }

    public static TextView textAddFps(TextView textView) {
        if (textView != null) {
            String charSequence = textView.getText().toString() != null ? textView.getText().toString() : "";
            Choreographer choreographer = Choreographer.getInstance();
            choreographer.postFrameCallback(new Choreographer.FrameCallback(textView, charSequence, choreographer) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000008
                long lastFrameTimeNanos = 0;
                private final Choreographer val$choreographer;
                private final String val$text;
                private final TextView val$textView_A;

                {
                    this.val$textView_A = textView;
                    this.val$text = charSequence;
                    this.val$choreographer = choreographer;
                }

                @Override // android.view.Choreographer.FrameCallback
                public void doFrame(long j) {
                    long j2 = this.lastFrameTimeNanos;
                    this.lastFrameTimeNanos = j;
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%.2f FPS", new Double(1.0E9d / (j - j2)))).toString()).append("</font>").toString()));
                    this.val$choreographer.postFrameCallback(this);
                }
            });
        }
        return textView;
    }

    public static TextView textAddSELinuxMode(TextView textView) {
        if (textView != null) {
            new Thread(new Runnable(new Handler(Looper.getMainLooper(), textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000009
                private final String val$text;
                private final TextView val$textView_A;

                {
                    this.val$textView_A = textView;
                    this.val$text = r3;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(this.val$text).append(SystemTool.isSELinuxPermissive() ? "<font color='#EF5350'>环境危险！SELinux处于-宽容模式</font>" : "<font color='#3F51B5'>环境安全！SELinux处于-强制模式</font>").toString()));
                }
            }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000010
                private final Handler val$handler;

                {
                    this.val$handler = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        this.val$handler.sendEmptyMessage(0);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        return textView;
    }

    public static TextView textAddDetectAppRooted(TextView textView) {
        if (textView != null) {
            new Thread(new Runnable(new Handler(Looper.getMainLooper(), textView, textView.getText().toString() != null ? textView.getText().toString() : "") { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000011
                private final String val$text;
                private final TextView val$textView_A;

                {
                    this.val$textView_A = textView;
                    this.val$text = r3;
                }

                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(this.val$text).append(AppTool.isRootEnabled() ? "<font color='#009688'>当前应用已授予ROOT权限</font>" : "<font color='#795548'>当前应用未授予ROOT权限</font>").toString()));
                }
            }) { // from class: irene.window.algui.Tools.RealTimeDataTextTool.100000012
                private final Handler val$handler;

                {
                    this.val$handler = r1;
                }

                @Override // java.lang.Runnable
                public void run() {
                    while (true) {
                        this.val$handler.sendEmptyMessage(0);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        return textView;
    }
}
