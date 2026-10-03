package irene.window.algui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import irene.window.algui.AlGuiData;
import irene.window.algui.CustomizeView.GradientTextView;
import irene.window.algui.Tools.AppPermissionTool;
import irene.window.algui.Tools.ViewTool;

/* loaded from: classes.dex */
public class AlGuiWindowView {
    public static final String TAG = "AlGuiHoverView";

    /* renamed from: irene.window.algui.AlGuiWindowView$100000004, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000004 implements View.OnClickListener {
        AnonymousClass100000004() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000011, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000011 implements View.OnClickListener {
        AnonymousClass100000011() {
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
        }
    }

    public static GradientTextView showNeonLightText(Context context, CharSequence charSequence, int[] iArr, float f, Typeface typeface, int i, int i2, int i3) {
        if (context == null) {
            return null;
        }
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示悬浮视图，只有声明了此权限才能显示悬浮视图！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
            return null;
        }
        if (!AppPermissionTool.checkOverlayPermission(context)) {
            return null;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 32 | 8 | 16;
        layoutParams.gravity = i;
        layoutParams.x = i2;
        layoutParams.y = i3;
        ((ViewGroup.LayoutParams) layoutParams).width = -2;
        ((ViewGroup.LayoutParams) layoutParams).height = -2;
        layoutParams.format = 1;
        layoutParams.windowAnimations = android.R.style.Animation_Toast;
        layoutParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        GradientTextView gradientTextView = new GradientTextView(context);
        gradientTextView.setColors(iArr);
        gradientTextView.setMovementMethod(LinkMovementMethod.getInstance());
        gradientTextView.setTextSize(0, ViewTool.convertDpToPx(context, f));
        if (typeface != null) {
            gradientTextView.setTypeface(typeface);
        }
        if (charSequence != null) {
            gradientTextView.setText(charSequence);
        }
        windowManager.addView(gradientTextView, layoutParams);
        return gradientTextView;
    }

    public static TextView showText(Context context, CharSequence charSequence, int i, float f, Typeface typeface, int i2, int i3, int i4) {
        if (context == null) {
            return null;
        }
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示悬浮视图，只有声明了此权限才能显示悬浮视图！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
            return null;
        }
        if (!AppPermissionTool.checkOverlayPermission(context)) {
            return null;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 32 | 8 | 16;
        layoutParams.gravity = i2;
        layoutParams.x = i3;
        layoutParams.y = i4;
        ((ViewGroup.LayoutParams) layoutParams).width = -2;
        ((ViewGroup.LayoutParams) layoutParams).height = -2;
        layoutParams.format = 1;
        layoutParams.windowAnimations = android.R.style.Animation_Toast;
        layoutParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        TextView textView = new TextView(context);
        textView.setTextColor(i);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setTextSize(0, ViewTool.convertDpToPx(context, f));
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        windowManager.addView(textView, layoutParams);
        return textView;
    }

    public static void showWebView(Context context, CharSequence charSequence, String str) {
        if (context == null || str == null) {
            return;
        }
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示悬浮视图，只有声明了此权限才能显示悬浮视图！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
            return;
        }
        if (AppPermissionTool.checkOverlayPermission(context)) {
            WindowManager windowManager = (WindowManager) context.getSystemService("window");
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            ((ViewGroup.LayoutParams) layoutParams).width = -2;
            ((ViewGroup.LayoutParams) layoutParams).height = -2;
            layoutParams.gravity = 17;
            layoutParams.format = 1;
            layoutParams.windowAnimations = android.R.style.Animation_Toast;
            layoutParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 32 | 262144;
            layoutParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
            WindowManager windowManager2 = (WindowManager) context.getSystemService("window");
            WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams();
            layoutParams2.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 8;
            layoutParams2.gravity = 17;
            ((ViewGroup.LayoutParams) layoutParams2).width = -2;
            ((ViewGroup.LayoutParams) layoutParams2).height = -2;
            layoutParams2.format = 1;
            layoutParams2.windowAnimations = android.R.style.Animation_Toast;
            layoutParams2.type = Build.VERSION.SDK_INT < 26 ? 2003 : 2038;
            FrameLayout frameLayout = new FrameLayout(context);
            LinearLayout addRoundButton = addRoundButton(context, charSequence, -1, 40, -838822264, 1.5f, -822083585);
            frameLayout.addView(addRoundButton);
            FrameLayout frameLayout2 = new FrameLayout(context);
            frameLayout2.setClipChildren(true);
            LinearLayout linearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams3.setMargins(80, 80, 80, 80);
            linearLayout.setLayoutParams(layoutParams3);
            linearLayout.setClipChildren(true);
            linearLayout.setOrientation(1);
            int convertDpToPx = ViewTool.convertDpToPx(context, 1);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setColor(-1573900240);
            gradientDrawable.setStroke(convertDpToPx, -822083585);
            linearLayout.setBackground(gradientDrawable);
            WebView addWebView = addWebView(context, str);
            LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-2, -2);
            layoutParams4.setMargins(30, 30, 30, 30);
            addWebView.setLayoutParams(layoutParams4);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            linearLayout2.setOrientation(0);
            float f = 35;
            float f2 = 0;
            LinearLayout addSquareButton = addSquareButton(context, "关闭", -1, f, -823241904, f2, -822083585);
            addSquareButton.setOnClickListener(new AnonymousClass100000000(windowManager, frameLayout2));
            LinearLayout addSquareButton2 = addSquareButton(context, "隐藏", -1, f, -838822264, f2, -822083585);
            addSquareButton2.setOnClickListener(new AnonymousClass100000001(windowManager, frameLayout2, windowManager2, frameLayout, layoutParams2));
            LinearLayout addSquareButton3 = addSquareButton(context, "刷新", -1, f, -838822264, f2, -822083585);
            addSquareButton3.setOnClickListener(new AnonymousClass100000002(addWebView, windowManager, frameLayout2, layoutParams));
            linearLayout2.addView(addSquareButton);
            linearLayout2.addView(addSquareButton2);
            linearLayout2.addView(addSquareButton3);
            linearLayout.addView(linearLayout2);
            linearLayout.addView(addWebView);
            frameLayout2.addView(linearLayout);
            windowManager.addView(frameLayout2, layoutParams);
            frameLayout2.setOnTouchListener(new AnonymousClass100000003(windowManager, frameLayout2, windowManager2, frameLayout, layoutParams2));
            addRoundButton.setOnClickListener(new AnonymousClass100000004());
            addRoundButton.setOnTouchListener(new AnonymousClass100000005(layoutParams2, frameLayout, windowManager2, windowManager, frameLayout2, layoutParams));
            long j = 500;
            new AnonymousClass100000006(j, j, addWebView).start();
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000000, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000000 implements View.OnClickListener {
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000000(WindowManager windowManager, FrameLayout frameLayout) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$windowManager.removeView(this.val$rootLayout);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000001, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000001 implements View.OnClickListener {
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000001(WindowManager windowManager, FrameLayout frameLayout, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$ballManager = windowManager2;
            this.val$ballLayout = frameLayout2;
            this.val$ballParams = layoutParams;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$windowManager.removeView(this.val$rootLayout);
            this.val$ballManager.addView(this.val$ballLayout, this.val$ballParams);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000002, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000002 implements View.OnClickListener {
        private final WindowManager.LayoutParams val$layoutParams;
        private final FrameLayout val$rootLayout;
        private final WebView val$view;
        private final WindowManager val$windowManager;

        AnonymousClass100000002(WebView webView, WindowManager windowManager, FrameLayout frameLayout, WindowManager.LayoutParams layoutParams) {
            this.val$view = webView;
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$layoutParams = layoutParams;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$view.reload();
            this.val$windowManager.updateViewLayout(this.val$rootLayout, this.val$layoutParams);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000003, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000003 implements View.OnTouchListener {
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000003(WindowManager windowManager, FrameLayout frameLayout, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$ballManager = windowManager2;
            this.val$ballLayout = frameLayout2;
            this.val$ballParams = layoutParams;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case 4:
                    this.val$windowManager.removeView(this.val$rootLayout);
                    this.val$ballManager.addView(this.val$ballLayout, this.val$ballParams);
                    return true;
                default:
                    return false;
            }
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000005, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000005 implements View.OnTouchListener {
        private float downX;
        private float downY;
        private int signX;
        private int signY;
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final WindowManager.LayoutParams val$layoutParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;
        boolean isOne = true;
        boolean isMove = false;
        int moveThreshold = 20;

        AnonymousClass100000005(WindowManager.LayoutParams layoutParams, FrameLayout frameLayout, WindowManager windowManager, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams2) {
            this.val$ballParams = layoutParams;
            this.val$ballLayout = frameLayout;
            this.val$ballManager = windowManager;
            this.val$windowManager = windowManager2;
            this.val$rootLayout = frameLayout2;
            this.val$layoutParams = layoutParams2;
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARN: Code restructure failed: missing block: B:21:0x00a7, code lost:
        
            return false;
         */
        @Override // android.view.View.OnTouchListener
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case 0:
                    this.isOne = true;
                    this.isMove = false;
                    this.signX = this.val$ballParams.x;
                    this.signY = this.val$ballParams.y;
                    this.downX = motionEvent.getRawX();
                    this.downY = motionEvent.getRawY();
                    break;
                case 1:
                    if (!this.isMove) {
                        this.val$ballManager.removeView(this.val$ballLayout);
                        this.val$windowManager.addView(this.val$rootLayout, this.val$layoutParams);
                    }
                    if (this.isMove) {
                        this.val$ballLayout.setAlpha(1);
                        break;
                    }
                    break;
                case 2:
                    float abs = Math.abs(motionEvent.getRawX() - this.downX);
                    float abs2 = Math.abs(motionEvent.getRawY() - this.downY);
                    if (abs > this.moveThreshold || abs2 > this.moveThreshold) {
                        this.isMove = true;
                    }
                    if (this.isMove) {
                        if (this.isOne) {
                            this.val$ballLayout.setAlpha(0.3f);
                            this.isOne = false;
                        }
                        this.val$ballParams.x = this.signX + ((int) (motionEvent.getRawX() - this.downX));
                        this.val$ballParams.y = this.signY + ((int) (motionEvent.getRawY() - this.downY));
                        this.val$ballManager.updateViewLayout(this.val$ballLayout, this.val$ballParams);
                        break;
                    }
                    break;
            }
            // 还原修正: jadx 丢弃了这个 return（boolean 方法必须有返回值）
            return false;
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000006, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000006 extends CountDownTimer {
        private final WebView val$view;

        AnonymousClass100000006(long j, long j2, WebView webView) {
            super(j, j2);
            this.val$view = webView;
        }

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            this.val$view.reload();
        }
    }

    public static void showWebSite(Context context, CharSequence charSequence, String str) {
        if (context == null || str == null) {
            return;
        }
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示悬浮视图，只有声明了此权限才能显示悬浮视图！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
            return;
        }
        if (AppPermissionTool.checkOverlayPermission(context)) {
            WindowManager windowManager = (WindowManager) context.getSystemService("window");
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            ((ViewGroup.LayoutParams) layoutParams).width = -1;
            ((ViewGroup.LayoutParams) layoutParams).height = -1;
            layoutParams.gravity = 17;
            layoutParams.format = 1;
            layoutParams.windowAnimations = android.R.style.Animation_Toast;
            layoutParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 32 | 262144;
            layoutParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
            WindowManager windowManager2 = (WindowManager) context.getSystemService("window");
            WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams();
            layoutParams2.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 8;
            layoutParams2.gravity = 17;
            ((ViewGroup.LayoutParams) layoutParams2).width = -2;
            ((ViewGroup.LayoutParams) layoutParams2).height = -2;
            layoutParams2.format = 1;
            layoutParams2.windowAnimations = android.R.style.Animation_Toast;
            layoutParams2.type = Build.VERSION.SDK_INT < 26 ? 2003 : 2038;
            FrameLayout frameLayout = new FrameLayout(context);
            LinearLayout addRoundButton = addRoundButton(context, charSequence, -1, 40, -838822264, 1.5f, -822083585);
            frameLayout.addView(addRoundButton);
            FrameLayout frameLayout2 = new FrameLayout(context);
            frameLayout2.setClipChildren(true);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            linearLayout.setClipChildren(true);
            linearLayout.setOrientation(1);
            int convertDpToPx = ViewTool.convertDpToPx(context, 1);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(0);
            gradientDrawable.setColor(-1573900240);
            gradientDrawable.setStroke(convertDpToPx, -822083585);
            linearLayout.setBackground(gradientDrawable);
            WebView addWebSite = addWebSite(context, str);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -1);
            layoutParams3.setMargins(30, 30, 30, 30);
            addWebSite.setLayoutParams(layoutParams3);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            linearLayout2.setOrientation(0);
            float f = 35;
            float f2 = 0;
            LinearLayout addSquareButton = addSquareButton(context, "关闭", -1, f, -823241904, f2, -822083585);
            addSquareButton.setOnClickListener(new AnonymousClass100000007(windowManager, frameLayout2));
            LinearLayout addSquareButton2 = addSquareButton(context, "隐藏", -1, f, -838822264, f2, -822083585);
            addSquareButton2.setOnClickListener(new AnonymousClass100000008(windowManager, frameLayout2, windowManager2, frameLayout, layoutParams2));
            LinearLayout addSquareButton3 = addSquareButton(context, "刷新", -1, f, -838822264, f2, -822083585);
            addSquareButton3.setOnClickListener(new AnonymousClass100000009(addWebSite, windowManager, frameLayout2, layoutParams));
            linearLayout2.addView(addSquareButton);
            linearLayout2.addView(addSquareButton2);
            linearLayout2.addView(addSquareButton3);
            linearLayout.addView(linearLayout2);
            linearLayout.addView(addWebSite);
            frameLayout2.addView(linearLayout);
            windowManager.addView(frameLayout2, layoutParams);
            frameLayout2.setOnTouchListener(new AnonymousClass100000010(windowManager, frameLayout2, windowManager2, frameLayout, layoutParams2));
            addRoundButton.setOnClickListener(new AnonymousClass100000011());
            addRoundButton.setOnTouchListener(new AnonymousClass100000012(layoutParams2, frameLayout, windowManager2, windowManager, frameLayout2, layoutParams));
            long j = 500;
            new AnonymousClass100000013(j, j, addWebSite).start();
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000007, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000007 implements View.OnClickListener {
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000007(WindowManager windowManager, FrameLayout frameLayout) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$windowManager.removeView(this.val$rootLayout);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000008, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000008 implements View.OnClickListener {
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000008(WindowManager windowManager, FrameLayout frameLayout, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$ballManager = windowManager2;
            this.val$ballLayout = frameLayout2;
            this.val$ballParams = layoutParams;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$windowManager.removeView(this.val$rootLayout);
            this.val$ballManager.addView(this.val$ballLayout, this.val$ballParams);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000009, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000009 implements View.OnClickListener {
        private final WindowManager.LayoutParams val$layoutParams;
        private final FrameLayout val$rootLayout;
        private final WebView val$view;
        private final WindowManager val$windowManager;

        AnonymousClass100000009(WebView webView, WindowManager windowManager, FrameLayout frameLayout, WindowManager.LayoutParams layoutParams) {
            this.val$view = webView;
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$layoutParams = layoutParams;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.val$view.reload();
            this.val$windowManager.updateViewLayout(this.val$rootLayout, this.val$layoutParams);
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000010, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000010 implements View.OnTouchListener {
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;

        AnonymousClass100000010(WindowManager windowManager, FrameLayout frameLayout, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams) {
            this.val$windowManager = windowManager;
            this.val$rootLayout = frameLayout;
            this.val$ballManager = windowManager2;
            this.val$ballLayout = frameLayout2;
            this.val$ballParams = layoutParams;
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case 4:
                    this.val$windowManager.removeView(this.val$rootLayout);
                    this.val$ballManager.addView(this.val$ballLayout, this.val$ballParams);
                    return true;
                default:
                    return false;
            }
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000012, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000012 implements View.OnTouchListener {
        private float downX;
        private float downY;
        private int signX;
        private int signY;
        private final FrameLayout val$ballLayout;
        private final WindowManager val$ballManager;
        private final WindowManager.LayoutParams val$ballParams;
        private final WindowManager.LayoutParams val$layoutParams;
        private final FrameLayout val$rootLayout;
        private final WindowManager val$windowManager;
        boolean isOne = true;
        boolean isMove = false;
        int moveThreshold = 20;

        AnonymousClass100000012(WindowManager.LayoutParams layoutParams, FrameLayout frameLayout, WindowManager windowManager, WindowManager windowManager2, FrameLayout frameLayout2, WindowManager.LayoutParams layoutParams2) {
            this.val$ballParams = layoutParams;
            this.val$ballLayout = frameLayout;
            this.val$ballManager = windowManager;
            this.val$windowManager = windowManager2;
            this.val$rootLayout = frameLayout2;
            this.val$layoutParams = layoutParams2;
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARN: Code restructure failed: missing block: B:21:0x00a7, code lost:
        
            return false;
         */
        @Override // android.view.View.OnTouchListener
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getActionMasked()) {
                case 0:
                    this.isOne = true;
                    this.isMove = false;
                    this.signX = this.val$ballParams.x;
                    this.signY = this.val$ballParams.y;
                    this.downX = motionEvent.getRawX();
                    this.downY = motionEvent.getRawY();
                    break;
                case 1:
                    if (!this.isMove) {
                        this.val$ballManager.removeView(this.val$ballLayout);
                        this.val$windowManager.addView(this.val$rootLayout, this.val$layoutParams);
                    }
                    if (this.isMove) {
                        this.val$ballLayout.setAlpha(1);
                        break;
                    }
                    break;
                case 2:
                    float abs = Math.abs(motionEvent.getRawX() - this.downX);
                    float abs2 = Math.abs(motionEvent.getRawY() - this.downY);
                    if (abs > this.moveThreshold || abs2 > this.moveThreshold) {
                        this.isMove = true;
                    }
                    if (this.isMove) {
                        if (this.isOne) {
                            this.val$ballLayout.setAlpha(0.3f);
                            this.isOne = false;
                        }
                        this.val$ballParams.x = this.signX + ((int) (motionEvent.getRawX() - this.downX));
                        this.val$ballParams.y = this.signY + ((int) (motionEvent.getRawY() - this.downY));
                        this.val$ballManager.updateViewLayout(this.val$ballLayout, this.val$ballParams);
                        break;
                    }
                    break;
            }
            // 还原修正: jadx 丢弃了这个 return（boolean 方法必须有返回值）
            return false;
        }
    }

    /* renamed from: irene.window.algui.AlGuiWindowView$100000013, reason: invalid class name */
    /* loaded from: classes.dex */
    static class AnonymousClass100000013 extends CountDownTimer {
        private final WebView val$view;

        AnonymousClass100000013(long j, long j2, WebView webView) {
            super(j, j2);
            this.val$view = webView;
        }

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            this.val$view.reload();
        }
    }

    private static LinearLayout addRoundButton(Context context, CharSequence charSequence, int i, float f, int i2, float f2, int i3) {
        if (context == null) {
            return null;
        }
        int convertDpToPx = ViewTool.convertDpToPx(context, f2);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(1);
        gradientDrawable.setColor(i2);
        gradientDrawable.setStroke(convertDpToPx, i3);
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewTool.convertDpToPx(context, f), ViewTool.convertDpToPx(context, f));
        layoutParams.setMargins(5, 5, 5, 5);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(17);
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setPadding(convertDpToPx, convertDpToPx, convertDpToPx, convertDpToPx);
        TextView textView = new TextView(context);
        textView.setGravity(17);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextColor(i);
        linearLayout.addView(textView);
        textView.setTextSize(0, ViewTool.convertDpToPx(context, 10.0f));
        return linearLayout;
    }

    private static LinearLayout addSquareButton(Context context, CharSequence charSequence, int i, float f, int i2, float f2, int i3) {
        if (context == null) {
            return null;
        }
        int convertDpToPx = ViewTool.convertDpToPx(context, f2);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(0);
        gradientDrawable.setColor(i2);
        gradientDrawable.setStroke(convertDpToPx, i3);
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewTool.convertDpToPx(context, f), ViewTool.convertDpToPx(context, f));
        layoutParams.setMargins(4, 4, 4, 4);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(17);
        linearLayout.setBackground(gradientDrawable);
        linearLayout.setPadding(convertDpToPx, convertDpToPx, convertDpToPx, convertDpToPx);
        TextView textView = new TextView(context);
        textView.setGravity(17);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextColor(i);
        linearLayout.addView(textView);
        textView.setAutoSizeTextTypeUniformWithConfiguration(2, ViewTool.convertDpToPx(context, f), 1, 2);
        return linearLayout;
    }

    private static WebView addWebView(Context context, String str) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        WebView webView = new WebView(context);
        webView.setId(AlGuiData.AlguiView.WebView.getId());
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(0);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new AnonymousClass100000014());
        if (str != null) {
            webView.loadData(str, "text/html", "utf-8");
        }
        return webView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiWindowView$100000014, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000014 extends WebViewClient {
        AnonymousClass100000014() {
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView webView, int i, String str, String str2) {
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            super.shouldOverrideUrlLoading(webView, str);
            webView.loadUrl(str);
            return true;
        }
    }

    private static WebView addWebSite(Context context, String str) {
        WebView addWebView = addWebView(context, null);
        if (str != null) {
            addWebView.loadUrl(str);
        }
        return addWebView;
    }
}
