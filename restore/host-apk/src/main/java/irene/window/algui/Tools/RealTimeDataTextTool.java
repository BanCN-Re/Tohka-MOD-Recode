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
            new Thread(new AnonymousClass100000001(new AnonymousClass100000000(Looper.getMainLooper(), context, textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000000, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000000 extends Handler {
        private final Context val$context_A;
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000000(Looper looper, Context context, TextView textView, String str) {
            super(looper);
            this.val$context_A = context;
            this.val$textView_A = textView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%d%%", new Integer(SystemTool.getBatteryLevel(this.val$context_A)))).toString()).append("</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000001, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000001 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000001(Handler handler) {
            this.val$handler = handler;
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
    }

    public static TextView textAddAvailableMemory(Context context, boolean z, TextView textView) {
        if (context == null) {
            return textView;
        }
        if (textView == null) {
            return textView;
        }
        new Thread(new AnonymousClass100000003(new AnonymousClass100000002(Looper.getMainLooper(), context, z, textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000002, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000002 extends Handler {
        private final Context val$context_A;
        private final boolean val$isFormat_A;
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000002(Looper looper, Context context, boolean z, TextView textView, String str) {
            super(looper);
            this.val$context_A = context;
            this.val$isFormat_A = z;
            this.val$textView_A = textView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%s", SystemTool.getMemoryUsage(this.val$context_A, this.val$isFormat_A))).toString()).append("</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000003, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000003 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000003(Handler handler) {
            this.val$handler = handler;
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
    }

    public static TextView textAddAvailableStorage(boolean z, TextView textView) {
        if (textView != null) {
            new Thread(new AnonymousClass100000005(new AnonymousClass100000004(Looper.getMainLooper(), z, textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000004, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000004 extends Handler {
        private final boolean val$isFormat_A;
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000004(Looper looper, boolean z, TextView textView, String str) {
            super(looper);
            this.val$isFormat_A = z;
            this.val$textView_A = textView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%s", SystemTool.getAvailableStorage(this.val$isFormat_A))).toString()).append("</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000005, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000005 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000005(Handler handler) {
            this.val$handler = handler;
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
    }

    public static TextView textAddTime(String str, TextView textView) {
        if (textView != null) {
            new Thread(new AnonymousClass100000007(new AnonymousClass100000006(Looper.getMainLooper(), str, textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000006, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000006 extends Handler {
        private final String val$text;
        private final TextView val$textView_A;
        private final String val$timeFormat_A;

        AnonymousClass100000006(Looper looper, String str, TextView textView, String str2) {
            super(looper);
            this.val$timeFormat_A = str;
            this.val$textView_A = textView;
            this.val$text = str2;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(new SimpleDateFormat(this.val$timeFormat_A != null ? this.val$timeFormat_A : "yyyy年MM月dd日 HH:mm:ss EE", Locale.getDefault()).format((Date) new java.sql.Date(System.currentTimeMillis()))).toString()).append("</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000007, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000007 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000007(Handler handler) {
            this.val$handler = handler;
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
    }

    public static TextView textAddFps(TextView textView) {
        if (textView != null) {
            String charSequence = textView.getText().toString() != null ? textView.getText().toString() : "";
            Choreographer choreographer = Choreographer.getInstance();
            choreographer.postFrameCallback(new AnonymousClass100000008(textView, charSequence, choreographer));
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000008, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000008 implements Choreographer.FrameCallback {
        long lastFrameTimeNanos = 0;
        private final Choreographer val$choreographer;
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000008(TextView textView, String str, Choreographer choreographer) {
            this.val$textView_A = textView;
            this.val$text = str;
            this.val$choreographer = choreographer;
        }

        @Override // android.view.Choreographer.FrameCallback
        public void doFrame(long j) {
            long j2 = this.lastFrameTimeNanos;
            this.lastFrameTimeNanos = j;
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(this.val$text).append("<font color='").toString()).append(RealTimeDataTextTool.ordinaryDataColor).toString()).append("'>").toString()).append(String.format(Locale.getDefault(), "%.2f FPS", new Double(1.0E9d / (j - j2)))).toString()).append("</font>").toString()));
            this.val$choreographer.postFrameCallback(this);
        }
    }

    public static TextView textAddSELinuxMode(TextView textView) {
        if (textView != null) {
            new Thread(new AnonymousClass100000010(new AnonymousClass100000009(Looper.getMainLooper(), textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000009, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000009 extends Handler {
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000009(Looper looper, TextView textView, String str) {
            super(looper);
            this.val$textView_A = textView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(this.val$text).append(SystemTool.isSELinuxPermissive() ? "<font color='#EF5350'>环境危险！SELinux处于-宽容模式</font>" : "<font color='#3F51B5'>环境安全！SELinux处于-强制模式</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000010, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000010 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000010(Handler handler) {
            this.val$handler = handler;
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
    }

    public static TextView textAddDetectAppRooted(TextView textView) {
        if (textView != null) {
            new Thread(new AnonymousClass100000012(new AnonymousClass100000011(Looper.getMainLooper(), textView, textView.getText().toString() != null ? textView.getText().toString() : ""))).start();
        }
        return textView;
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000011, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000011 extends Handler {
        private final String val$text;
        private final TextView val$textView_A;

        AnonymousClass100000011(Looper looper, TextView textView, String str) {
            super(looper);
            this.val$textView_A = textView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(this.val$text).append(AppTool.isRootEnabled() ? "<font color='#009688'>当前应用已授予ROOT权限</font>" : "<font color='#795548'>当前应用未授予ROOT权限</font>").toString()));
        }
    }

    /* renamed from: irene.window.algui.Tools.RealTimeDataTextTool$100000012, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000012 implements Runnable {
        private final Handler val$handler;

        AnonymousClass100000012(Handler handler) {
            this.val$handler = handler;
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
    }
}
