package irene.window.algui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.ViewCompat;
import irene.window.algui.AlGui;
import irene.window.algui.AlGuiBubbleNotification;
import irene.window.algui.AlGuiData;
import irene.window.algui.CustomizeView.MarqueeTextView;
import irene.window.algui.CustomizeView.vLinearLayout;
import irene.window.algui.Tools.AppPermissionTool;
import irene.window.algui.Tools.AppTool;
import irene.window.algui.Tools.RealTimeDataTextTool;
import irene.window.algui.Tools.SystemTool;
import irene.window.algui.Tools.VariousTools;
import irene.window.algui.Tools.ViewTool;
import java.util.Iterator;
import java.util.Map;

/* loaded from: classes.dex */
public class AlGuiPrefabricatedMenu_LB {
    public static final String TAG = "AlGuiPrefabricatedMenu_LB";
    private AlGui algui;
    private Context context;
    private TextView WaterMark = null;
    private WindowManager wManager = null;
    private WindowManager.LayoutParams wParams = null;

    /* JADX INFO: Access modifiers changed from: protected */
    public AlGuiPrefabricatedMenu_LB(Context context, AlGui alGui) {
        if (context == null || alGui == null) {
            return;
        }
        this.context = context;
        this.algui = alGui;
    }

    public vLinearLayout addExplanation(ViewGroup viewGroup) {
        if (this.context == null || this.algui == null) {
            return null;
        }
        float f = 0;
        float f2 = 11;
        float f3 = 5;
        vLinearLayout addCollapse = this.algui.addCollapse((CharSequence) "使用说明", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, 3, -1, f, -3814679, true, AlGui.GUI(this.context).addLinearLayout(17, 1, -1, -2, this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView("使用说明", f2, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)), this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml("本项目由 <font color='#EF5350'>麻省理工学院许可证</font> 进行发布和分发"), f2, ViewCompat.MEASURED_STATE_MASK, null)), this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml("开发者：<font color='#424242'>艾琳 (唯一作者)</font>"), f2, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml("开发者QQ：<a href=\"https://qm.qq.com/q/3U1H3JMf5Y\">3353484607</a>"), f2, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml("交流群QQ：<a color='#3F51B5' href=\"mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3Dqeey7hum96m64eaHkKKjHC6micNY9_oI\">730967224</a>"), f2, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml("当前版本：<font color='#424242'>v2.0 (正式版)</font>"), f2, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addLine(0.5f, -2039584, true), this.algui.addButton("使用文档", f2, -1, null, f3, -12627531, f, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000000(this)), this.algui.addButton("更新GUI", f2, -1, null, f3, -12627531, f, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000001(this)), this.algui.addButton("作者B站", f2, -1, null, f3, -12627531, f, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000004(this)), this.algui.addButton("赞助作者(加入开发)", f2, -1, null, f3, -12627531, f, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000005(this)), this.algui.addButton("交流社区(Bug反馈)", f2, -1, null, f3, -12627531, f, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000008(this)), this.algui.addLine(0.5f, -2039584, true), AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("Copyright © 2023 艾琳 版权所有", 9, -6381922, Typeface.create(Typeface.DEFAULT, 0))));
        if (viewGroup != null) {
            viewGroup.addView(addCollapse);
            return addCollapse;
        }
        return addCollapse;
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000000, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000000 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000000(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            VariousTools.gotoWeb(this.this$0.context, "https://gitee.com/ByteAL/ALGUI");
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000001, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000001 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000001(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            VariousTools.gotoWeb(this.this$0.context, "https://www.123pan.com/s/RMOtVv-G8ijh.html");
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000004, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000004 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000004(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            AlGuiBubbleNotification.Inform(this.this$0.context).showMessageNotification_Simplicity_Button(null, "请选择打开方式", "你希望在内部直接打开，还是在外部游览器打开", "内部", new AnonymousClass100000002(this), "外部", new AnonymousClass100000003(this));
        }

        /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000004$100000002, reason: invalid class name */
        /* loaded from: classes.dex */
        class AnonymousClass100000002 implements AlGuiBubbleNotification.T_ButtonOnChangeListener {
            private final AnonymousClass100000004 this$0;

            AnonymousClass100000002(AnonymousClass100000004 anonymousClass100000004) {
                this.this$0 = anonymousClass100000004;
            }

            @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                if (AppPermissionTool.isAndroidManifestPermissionExist(this.this$0.this$0.context, "android.permission.INTERNET")) {
                    AlGuiWindowView.showWebSite(this.this$0.this$0.context, "B站", "https://b23.tv/NoEaEXW");
                    return;
                }
                AlGuiBubbleNotification.Inform(this.this$0.this$0.context).showMistakeNotification_Simplicity(null, "内部无法加载", "可能没有网络权限，我们将自动跳转到第三方游览器加载！", 5000);
                VariousTools.gotoWeb(this.this$0.this$0.context, "https://b23.tv/NoEaEXW");
            }
        }

        /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000004$100000003, reason: invalid class name */
        /* loaded from: classes.dex */
        class AnonymousClass100000003 implements AlGuiBubbleNotification.T_ButtonOnChangeListener {
            private final AnonymousClass100000004 this$0;

            AnonymousClass100000003(AnonymousClass100000004 anonymousClass100000004) {
                this.this$0 = anonymousClass100000004;
            }

            @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                VariousTools.gotoWeb(this.this$0.this$0.context, "https://b23.tv/NoEaEXW");
            }
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000005, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000005 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000005(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            if (AppPermissionTool.isAndroidManifestPermissionExist(this.this$0.context, "android.permission.INTERNET")) {
                AlGuiWindowView.showWebView(this.this$0.context, "赞助", "<html><body><img src=\"https://img2.imgtp.com/2024/02/14/aOxtoFpY.png\"></img></body></html>");
            } else {
                VariousTools.gotoWeb(this.this$0.context, "https://img2.imgtp.com/2024/02/14/aOxtoFpY.png");
            }
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000008, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000008 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000008(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            AlGuiBubbleNotification.Inform(this.this$0.context).showMessageNotification_Simplicity_Button(null, "请选择打开方式", "你希望在内部直接打开，还是在外部游览器打开", "内部", new AnonymousClass100000006(this), "外部", new AnonymousClass100000007(this));
        }

        /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000008$100000006, reason: invalid class name */
        /* loaded from: classes.dex */
        class AnonymousClass100000006 implements AlGuiBubbleNotification.T_ButtonOnChangeListener {
            private final AnonymousClass100000008 this$0;

            AnonymousClass100000006(AnonymousClass100000008 anonymousClass100000008) {
                this.this$0 = anonymousClass100000008;
            }

            @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                if (AppPermissionTool.isAndroidManifestPermissionExist(this.this$0.this$0.context, "android.permission.INTERNET")) {
                    AlGuiWindowView.showWebSite(this.this$0.this$0.context, "社区", "https://txc.qq.com/products/634245");
                    return;
                }
                AlGuiBubbleNotification.Inform(this.this$0.this$0.context).showMistakeNotification_Simplicity(null, "内部无法加载", "可能没有网络权限，我们将自动跳转到第三方游览器加载！", 5000);
                VariousTools.gotoWeb(this.this$0.this$0.context, "https://txc.qq.com/products/634245");
            }
        }

        /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000008$100000007, reason: invalid class name */
        /* loaded from: classes.dex */
        class AnonymousClass100000007 implements AlGuiBubbleNotification.T_ButtonOnChangeListener {
            private final AnonymousClass100000008 this$0;

            AnonymousClass100000007(AnonymousClass100000008 anonymousClass100000008) {
                this.this$0 = anonymousClass100000008;
            }

            @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                VariousTools.gotoWeb(this.this$0.this$0.context, "https://txc.qq.com/products/634245");
            }
        }
    }

    private HorizontalScrollView textAddSELinuxMode(HorizontalScrollView horizontalScrollView) {
        if (horizontalScrollView != null) {
            MarqueeTextView marqueeTextView = (MarqueeTextView) horizontalScrollView.findViewById(AlGuiData.AlguiView.MarqueeTextView.getId());
            new Thread(new AnonymousClass100000010(this, new AnonymousClass100000009(this, Looper.getMainLooper(), marqueeTextView, marqueeTextView.getText().toString() != null ? marqueeTextView.getText().toString() : ""))).start();
        }
        return horizontalScrollView;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000009, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000009 extends Handler {
        private final AlGuiPrefabricatedMenu_LB this$0;
        private final String val$text;
        private final MarqueeTextView val$textView_A;

        AnonymousClass100000009(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB, Looper looper, MarqueeTextView marqueeTextView, String str) {
            super(looper);
            this.this$0 = alGuiPrefabricatedMenu_LB;
            this.val$textView_A = marqueeTextView;
            this.val$text = str;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            this.val$textView_A.setText(Html.fromHtml(new StringBuffer().append(this.val$text).append(SystemTool.isSELinuxPermissive() ? "<font color='#EF5350'>环境危险！SELinux处于-宽容模式</font>" : "<font color='#3F51B5'>环境安全！SELinux处于-强制模式</font>").toString()));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000010, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000010 implements Runnable {
        private final AlGuiPrefabricatedMenu_LB this$0;
        private final Handler val$handler;

        AnonymousClass100000010(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB, Handler handler) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
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

    public vLinearLayout addAttributeStatusMenu(ViewGroup viewGroup) {
        TextView textView;
        StringBuffer append;
        String str;
        TextView textView2;
        View view;
        View view2;
        TextView addTextView;
        if (this.context == null || this.algui == null) {
            return null;
        }
        AlGui alGui = this.algui;
        float f = 3;
        float f2 = 0;
        View addLine = this.algui.addLine(0.5f, -2039584, true);
        float f3 = 11;
        LinearLayout addLinearLayout = AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("系统状态实时监测", f3, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)));
        View addLine2 = this.algui.addLine(0.5f, -2039584, true);
        TextView textAddTime = RealTimeDataTextTool.textAddTime("yyyy年MM月dd日 HH:mm:ss EE", this.algui.addTextView("系统当前时间：", f3, ViewCompat.MEASURED_STATE_MASK, null));
        View addLine3 = this.algui.addLine(0.5f, -2039584, true);
        TextView textAddPower = RealTimeDataTextTool.textAddPower(this.context, this.algui.addTextView("系统实时电量：", f3, ViewCompat.MEASURED_STATE_MASK, null));
        View addLine4 = this.algui.addLine(0.5f, -2039584, true);
        TextView textAddFps = RealTimeDataTextTool.textAddFps(this.algui.addTextView("系统实时帧率：", f3, ViewCompat.MEASURED_STATE_MASK, null));
        View addLine5 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView2 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("<hr>系统总共内存：<font color='").append(RealTimeDataTextTool.getOrdinaryDataColor_RGB()).toString()).append("'>").toString()).append(SystemTool.getTotalMemory(this.context, true)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine6 = this.algui.addLine(0.5f, -2039584, true);
        TextView textAddAvailableMemory = RealTimeDataTextTool.textAddAvailableMemory(this.context, true, this.algui.addTextView("系统可用内存：", f3, ViewCompat.MEASURED_STATE_MASK, null));
        View addLine7 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView3 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("系统总共存储：<font color='").append(RealTimeDataTextTool.getOrdinaryDataColor_RGB()).toString()).append("'>").toString()).append(SystemTool.getTotalStorage(true)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine8 = this.algui.addLine(0.5f, -2039584, true);
        TextView textAddAvailableStorage = RealTimeDataTextTool.textAddAvailableStorage(true, this.algui.addTextView("系统可用存储：", f3, ViewCompat.MEASURED_STATE_MASK, null));
        View addLine9 = this.algui.addLine(0.5f, -2039584, true);
        LinearLayout addLinearLayout2 = AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("设备信息", f3, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)));
        View addLine10 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView4 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("品牌名：<font color='#757575'>").append(Build.BRAND).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine11 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView5 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("系统型号：<font color='#757575'>").append(Build.MODEL).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine12 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView6 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("系统分辨率：<font color='#757575'>").append(SystemTool.getScreenResolution(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine13 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView7 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("系统真实DP：<font color='#757575'>").append(SystemTool.getRealScreenDP(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine14 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView8 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("系统CPU位数：<font color='#757575'>").append(Build.CPU_ABI).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine15 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView9 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("设备系统：<font color='#757575'>").append(SystemTool.isHarmonyOs() ? "HarmonyOs [鸿蒙系统]" : "Android [安卓系统]").toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine16 = this.algui.addLine(0.5f, -2039584, true);
        AlGui alGui2 = this.algui;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer append2 = new StringBuffer().append("系统版本：<font color='#757575'>");
        if (SystemTool.isHarmonyOs()) {
            textView = textAddAvailableStorage;
            append = new StringBuffer().append("HarmonyOs ");
            str = SystemTool.getHarmonyVersion();
        } else {
            textView = textAddAvailableStorage;
            append = new StringBuffer().append("Android ");
            str = Build.VERSION.RELEASE;
        }
        TextView addTextView10 = alGui2.addTextView(Html.fromHtml(stringBuffer.append(append2.append(append.append(str).toString()).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine17 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView11 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("SDK版本号：<font color='#757575'>").append(Build.VERSION.SDK).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine18 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView12 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("系统ID：<font color='#757575'>").append(Build.ID).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine19 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView13 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append("Root权限：").append(SystemTool.inspectRootPermission() ? "<font color='#009688'>当前设备拥有ROOT权限</font>" : "<font color='#795548'>当前设备没有ROOT权限</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine20 = this.algui.addLine(0.5f, -2039584, true);
        LinearLayout addLinearLayout3 = AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("应用信息", f3, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)));
        View addLine21 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView14 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("应用名称：<font color='#757575'>").append(AppTool.getAppName(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine22 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView15 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("版本名称：<font color='#757575'>").append(AppTool.getVersion(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine23 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView16 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("版本号：<font color='#757575'>").append(AppTool.getVersionCode(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine24 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView17 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("包名：<font color='#757575'>").append(AppTool.getAppPackageName(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine25 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView18 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append("系统应用：").append(AppTool.isSystemApp(this.context) ? "<font color='#009688'>是</font>" : "<font color='#795548'>否</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine26 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView19 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append("调试模式：").append(AppTool.isDebugMode(this.context) ? "<font color='#009688'>true</font>" : "<font color='#795548'>false</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        View addLine27 = this.algui.addLine(0.5f, -2039584, true);
        TextView addTextView20 = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("当前应用uid：<font color='#3F51B5'>").append(AppTool.getAppUid(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        if (AppTool.getSameUidAppNames(this.context).size() == 0) {
            textView2 = addTextView20;
            addTextView = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("uid跨进程：<font color='#757575'>").append("没有可以跨进程的应用，因为没有检测到与当前应用uid相同的应用").toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
            view = addLine22;
            view2 = addLine11;
        } else {
            textView2 = addTextView20;
            view = addLine22;
            view2 = addLine11;
            addTextView = this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("uid跨进程：<font color='#BA68C8'>").append(AlGuiPrefabricatedMenu_LB$$ExternalSyntheticBackport0.m(", ", AppTool.getSameUidAppNames(this.context))).toString()).append("与当前应用uid相同, 它们可以跨进程").toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null);
        }
        vLinearLayout addCollapse = alGui.addCollapse((CharSequence) "属性状态", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, f, -1, f2, -3814679, false, addLine, addLinearLayout, addLine2, textAddTime, addLine3, textAddPower, addLine4, textAddFps, addLine5, addTextView2, addLine6, textAddAvailableMemory, addLine7, addTextView3, addLine8, textView, addLine9, addLinearLayout2, addLine10, addTextView4, view2, addTextView5, addLine12, addTextView6, addLine13, addTextView7, addLine14, addTextView8, addLine15, addTextView9, addLine16, addTextView10, addLine17, addTextView11, addLine18, addTextView12, addLine19, addTextView13, addLine20, addLinearLayout3, addLine21, addTextView14, view, addTextView15, addLine23, addTextView16, addLine24, addTextView17, addLine25, addTextView18, addLine26, addTextView19, addLine27, textView2, addTextView, this.algui.addLine(0.5f, -2039584, true), this.algui.addTextView(Html.fromHtml(new StringBuffer().append(new StringBuffer().append("入口Activity：<font color='#757575'>").append(AppTool.getLauncherActivityName(this.context)).toString()).append("</font>").toString()), f3, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addLine(0.5f, -2039584, true), AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("环境检测机制", f3, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1))), this.algui.addLine(0.5f, -2039584, true), textAddSELinuxMode(this.algui.addMarqueeTextView(3, "", f3, -12627531, null, 8000, -1, true)), this.algui.addLine(0.5f, -2039584, true), AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("Copyright © 2023 艾琳 版权所有", 9, -6381922, Typeface.create(Typeface.DEFAULT, 0))));
        if (viewGroup != null) {
            viewGroup.addView(addCollapse);
            return addCollapse;
        }
        return addCollapse;
    }

    public vLinearLayout addAppearanceSettingsMenu(ViewGroup viewGroup) {
        if (this.context == null || this.algui == null) {
            return null;
        }
        float f = 0;
        float f2 = 11;
        float f3 = 8;
        float f4 = 9;
        float f5 = 5;
        vLinearLayout addCollapse = this.algui.addCollapse((CharSequence) "菜单设置", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, 3, -1, f, -3814679, false, this.algui.addLine(0.5f, -2039584, true), AlGui.GUI(this.context).addLinearLayout(17, 1, -1, -2, this.algui.addTextView("全局颜色设置", f2, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)), this.algui.addTextView("支持的颜色格式 ARGB格式(0xFF4DB6AC)  HEX格式(#4DB6AC)", f3, ViewCompat.MEASURED_STATE_MASK, null), this.algui.addTextView("设置完成后将自动保存配置", f3, ViewCompat.MEASURED_STATE_MASK, null)), this.algui.addEditText(f4, null, -3223858, "菜单上下边栏背景颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000011(this)), this.algui.addEditText(f4, null, -3223858, "菜单说明布局背景颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000012(this)), this.algui.addEditText(f4, null, -3223858, "菜单滚动列表背景颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000013(this)), this.algui.addEditText(f4, null, -3223858, "菜单描边边框绘制颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000014(this)), this.algui.addEditText(f4, null, -3223858, "菜单右下三角绘制颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000015(this)), this.algui.addEditText(f4, null, -3223858, "菜单顶部线条绘制颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000016(this)), this.algui.addEditText(f4, null, -3223858, "菜单主要标题文本颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000017(this)), this.algui.addEditText(f4, null, -3223858, "菜单小副标题文本颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000018(this)), this.algui.addEditText(f4, null, -3223858, "菜单说明内容文本颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000019(this)), this.algui.addEditText(f4, null, -3223858, "菜单左下按钮文本颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000020(this)), this.algui.addEditText(f4, null, -3223858, "菜单右下按钮文本颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000021(this)), this.algui.addEditText(f4, null, -3223858, "菜单直播模式图标颜色", -1040122112, "", f5, -1476395008, f, 0, -1, "应用", f5, -11234587, f, -1, new AnonymousClass100000022(this)), this.algui.addButton("恢复默认颜色", f2, -1, null, f5, -12627531, f, -1, -1, -2, new AnonymousClass100000023(this)), this.algui.addTextView(" ", 10, 0, null), AlGui.GUI(this.context).addLinearLayout(17, 1, -1, -2, this.algui.addTextView("全局属性设置", f2, ViewCompat.MEASURED_STATE_MASK, Typeface.create(Typeface.DEFAULT, 1)), this.algui.addTextView("设置完成后将自动保存配置", f3, ViewCompat.MEASURED_STATE_MASK, null)), this.algui.addSeekBarFloat("菜单圆角半径", f2, ViewCompat.MEASURED_STATE_MASK, null, 0.0f, AlGuiData.getMenuAttributeSP(this.context).getFloat((String) AlGuiData.getMenuAttributeData().get("根布局圆角半径键名"), ((Float) AlGuiData.getMenuAttributeData().get("根布局圆角半径默认数据")).floatValue()), 50.0f, -11234587, -11234587, -11234587, new AnonymousClass100000024(this)), this.algui.addSeekBarFloat("菜单描边宽度", f2, ViewCompat.MEASURED_STATE_MASK, null, 0.0f, AlGuiData.getMenuAttributeSP(this.context).getFloat((String) AlGuiData.getMenuAttributeData().get("根布局描边宽度键名"), ((Float) AlGuiData.getMenuAttributeData().get("根布局描边宽度默认数据")).floatValue()), 10.0f, -11234587, -11234587, -11234587, new AnonymousClass100000025(this)), this.algui.addSeekBarFloat("顶部线条圆角", f2, ViewCompat.MEASURED_STATE_MASK, null, 0.0f, AlGuiData.getMenuAttributeSP(this.context).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单顶部线条圆角半径键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单顶部线条圆角半径默认数据")).floatValue()), 20.0f, -11234587, -11234587, -11234587, new AnonymousClass100000026(this)), this.algui.addSeekBarFloat("悬浮窗透明度", f2, ViewCompat.MEASURED_STATE_MASK, null, 0.1f, AlGuiData.getMenuAttributeSP(this.context).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单透明度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单透明度默认数据")).floatValue()), 1.0f, -11234587, -11234587, -11234587, new AnonymousClass100000027(this)), this.algui.addButton("恢复默认属性", f2, -1, null, f5, -12627531, f, -1, -1, -2, new AnonymousClass100000028(this)), AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("Copyright © 2023 艾琳 版权所有", f4, -6381922, Typeface.create(Typeface.DEFAULT, 0))));
        if (viewGroup != null) {
            viewGroup.addView(addCollapse);
            return addCollapse;
        }
        return addCollapse;
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000011, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000011 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000011(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuRootLayout().setBackColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("根布局背景颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000012, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000012 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000012(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuExplanationLayout().setBackgroundColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单说明背景颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000013, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000013 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000013(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuScrollingList().setBackgroundColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单滚动列表背景颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000014, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000014 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000014(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuRootLayout().setBorderColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("根布局描边颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000015, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000015 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000015(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuBottomRightTrianglePaint().setColor(parseColor);
                    this.this$0.algui.getMenuBottomRightTriangleView().invalidate();
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单右下角三角形颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000016, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000016 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000016(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuTopLine().setBackColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单顶部线条颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000017, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000017 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000017(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuMainTitle().setTextColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单主标题文本颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000018, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000018 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000018(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuSubTitle().setTextColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单副标题文本颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000019, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000019 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000019(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuExplanation().setTextColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单说明文本颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000020, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000020 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000020(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuBottomLeftButton().setTextColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单左下角按钮文本颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000021, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000021 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000021(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuBottomRightButton().setTextColor(parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单右下角按钮文本颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000022, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000022 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000022(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.algui.getMenuLiveStreamIcon().setImageTintList(ColorStateList.valueOf(parseColor));
                    AlGuiData.getMenuColorSPED(this.this$0.context).putInt((String) AlGuiData.getMenuColorData().get("菜单直播模式图标颜色键名"), parseColor);
                    AlGuiData.getMenuColorSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000023, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000023 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000023(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            AlGuiData.getMenuColorSPED(this.this$0.context).clear();
            AlGuiData.getMenuColorSPED(this.this$0.context).apply();
            this.this$0.algui.updateMenuAppearance();
            AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "恢复成功", "已恢复所有颜色并清除保存的颜色配置", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000024, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000024 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000024(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            this.this$0.algui.getMenuRootLayout().setFilletRadiu(ViewTool.convertDpToPx(this.this$0.context, f));
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            AlGuiData.getMenuAttributeSPED(this.this$0.context).putFloat((String) AlGuiData.getMenuAttributeData().get("根布局圆角半径键名"), f);
            AlGuiData.getMenuAttributeSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000025, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000025 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000025(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            this.this$0.algui.getMenuRootLayout().setBorderSize(ViewTool.convertDpToPx(this.this$0.context, f));
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            AlGuiData.getMenuAttributeSPED(this.this$0.context).putFloat((String) AlGuiData.getMenuAttributeData().get("根布局描边宽度键名"), f);
            AlGuiData.getMenuAttributeSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000026, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000026 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000026(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            this.this$0.algui.getMenuTopLine().setFilletRadiu(ViewTool.convertDpToPx(this.this$0.context, f));
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            AlGuiData.getMenuAttributeSPED(this.this$0.context).putFloat((String) AlGuiData.getMenuAttributeData().get("菜单顶部线条圆角半径键名"), f);
            AlGuiData.getMenuAttributeSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000027, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000027 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000027(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            this.this$0.algui.getMenuRootLayout().setAlpha(f);
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            AlGuiData.getMenuAttributeSPED(this.this$0.context).putFloat((String) AlGuiData.getMenuAttributeData().get("菜单透明度键名"), f);
            AlGuiData.getMenuAttributeSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000028, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000028 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000028(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            Iterator<? extends Map.Entry<String, ?>> it = AlGuiData.getMenuAttributeSP(this.this$0.context).getAll().entrySet().iterator();
            while (it.hasNext()) {
                String key = it.next().getKey();
                if (!key.equals((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表宽度键名")) && !key.equals((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表高度键名"))) {
                    AlGuiData.getMenuAttributeSPED(this.this$0.context).remove(key);
                }
            }
            AlGuiData.getMenuAttributeSPED(this.this$0.context).apply();
            this.this$0.algui.updateMenuAppearance();
            AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "恢复成功", "已恢复所有属性并清除保存的属性配置", 5000);
        }
    }

    public vLinearLayout addGameFrontSightMenu(ViewGroup viewGroup) {
        if (this.context == null || this.algui == null) {
            return null;
        }
        initFrontSight();
        float f = 0;
        float f2 = 11;
        float f3 = 9;
        float f4 = 5;
        vLinearLayout addCollapse = this.algui.addCollapse((CharSequence) "游戏准星", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, 3, -1, f, -3814679, false, this.algui.addLine(0.5f, -2039584, true), this.algui.addSwitch("游戏准星", f2, ViewCompat.MEASURED_STATE_MASK, null, "在游戏内绘制一个准星 - 提升精准度", 8, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000029(this)), this.algui.addEditText(f3, null, -3223858, "准星样式 例如：⊙☉⊕·✛╋☩ 等等", -1040122112, "", f4, -1476395008, f, 0, -1, "应用", f4, -11234587, f, -1, new AnonymousClass100000030(this)), this.algui.addEditText(f3, null, -3223858, "准星颜色 例如：#009688 或 0xFF009688等等", -1040122112, "", f4, -1476395008, f, 0, -1, "应用", f4, -11234587, f, -1, new AnonymousClass100000031(this)), this.algui.addSeekBarFloat("准星大小", f2, ViewCompat.MEASURED_STATE_MASK, null, f, AlGuiData.getGameFrontSightSP(this.context).getFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星大小键名"), ((Float) AlGuiData.getGameFrontSightData().get("游戏准星大小默认数据")).floatValue()), 100, -11234587, -11234587, -11234587, new AnonymousClass100000032(this)), this.algui.addSeekBarFloat("准星透明", f2, ViewCompat.MEASURED_STATE_MASK, null, f, AlGuiData.getGameFrontSightSP(this.context).getFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星透明度键名"), ((Float) AlGuiData.getGameFrontSightData().get("游戏准星透明度默认数据")).floatValue()), 1, -11234587, -11234587, -11234587, new AnonymousClass100000033(this)), this.algui.addSeekBarInt("X偏移(相对中心点)", f2, ViewCompat.MEASURED_STATE_MASK, null, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED, AlGuiData.getGameFrontSightSP(this.context).getInt((String) AlGuiData.getGameFrontSightData().get("游戏准星X偏移键名"), ((Integer) AlGuiData.getGameFrontSightData().get("游戏准星X偏移默认数据")).intValue()), 1000, -11234587, -11234587, -11234587, new AnonymousClass100000034(this)), this.algui.addSeekBarInt("Y偏移(相对中心点)", f2, ViewCompat.MEASURED_STATE_MASK, null, NotificationManagerCompat.IMPORTANCE_UNSPECIFIED, AlGuiData.getGameFrontSightSP(this.context).getInt((String) AlGuiData.getGameFrontSightData().get("游戏准星Y偏移键名"), ((Integer) AlGuiData.getGameFrontSightData().get("游戏准星Y偏移默认数据")).intValue()), 1000, -11234587, -11234587, -11234587, new AnonymousClass100000035(this)), this.algui.addButton("恢复默认配置", f2, -1, null, f4, -12627531, f, -1, -1, -2, new AnonymousClass100000036(this)), AlGui.GUI(this.context).addLinearLayout(17, 0, -1, -2, this.algui.addTextView("Copyright © 2023 艾琳 版权所有", f3, -6381922, Typeface.create(Typeface.DEFAULT, 0))));
        if (viewGroup != null) {
            viewGroup.addView(addCollapse);
            return addCollapse;
        }
        return addCollapse;
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000029, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000029 implements AlGui.T_SwitchOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000029(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            if (z) {
                if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                    if (this.this$0.initFrontSight()) {
                        AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "初始化成功", "已加载游戏准星", 5000);
                    } else {
                        compoundButton.setChecked(false);
                        AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "准星初始化失败", "可能没有悬浮窗权限或空指针异常", 5000);
                        return;
                    }
                }
                this.this$0.WaterMark.setVisibility(0);
                AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "开启成功", "已加载游戏准星", 5000);
                return;
            }
            if (this.this$0.WaterMark != null && this.this$0.wManager != null && this.this$0.wParams != null) {
                this.this$0.WaterMark.setVisibility(8);
                AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "关闭成功", "已关闭游戏准星", 5000);
            }
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000030, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000030 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000030(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
                return;
            }
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                this.this$0.WaterMark.setText(obj);
                AlGuiData.getGameFrontSightSPED(this.this$0.context).putString((String) AlGuiData.getGameFrontSightData().get("游戏准星样式键名"), obj);
                AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
                AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存配置", 5000);
                return;
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入准星样式符号 例如：⊙☉⊕·✛╋☩ 等等", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000031, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000031 implements AlGui.T_EditTextOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000031(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void afterTextChanged(EditText editText, Editable editable) {
            if (editable.toString().matches("[a-zA-Z0-9#]*")) {
                return;
            }
            String replaceAll = editable.toString().replaceAll("[^a-zA-Z0-9#]", "");
            editText.setText(replaceAll);
            editText.setSelection(replaceAll.length());
        }

        @Override // irene.window.algui.AlGui.T_EditTextOnChangeListener
        public void buttonOnClick(EditText editText, View view, TextView textView, boolean z) {
            int parseColor;
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
                return;
            }
            String obj = editText.getText().toString() != null ? editText.getText().toString() : "";
            if (!obj.isEmpty()) {
                try {
                    if (obj.startsWith("0x") && obj.length() == 10) {
                        parseColor = (int) Long.parseLong(obj.substring(2), 16);
                    } else {
                        parseColor = Color.parseColor(obj);
                    }
                    this.this$0.WaterMark.setTextColor(parseColor);
                    AlGuiData.getGameFrontSightSPED(this.this$0.context).putInt((String) AlGuiData.getGameFrontSightData().get("游戏准星颜色键名"), parseColor);
                    AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
                    AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "应用成功", "已自动保存配置", 5000);
                    return;
                } catch (IllegalArgumentException e) {
                    AlGuiBubbleNotification.Inform(this.this$0.context).showMistakeNotification_Simplicity(null, "应用失败", "无效的颜色值", 5000);
                    return;
                }
            }
            AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "请输入颜色值 例如：#009688 或 0xFF009688", 5000);
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000032, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000032 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000032(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            this.this$0.WaterMark.setTextSize(0, ViewTool.convertDpToPx(this.this$0.context, f));
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
            }
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            AlGuiData.getGameFrontSightSPED(this.this$0.context).putFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星大小键名"), f);
            AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000033, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000033 implements AlGui.T_SeekBarFloatOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000033(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            this.this$0.WaterMark.setAlpha(f);
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
            }
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            AlGuiData.getGameFrontSightSPED(this.this$0.context).putFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星透明度键名"), f);
            AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000034, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000034 implements AlGui.T_SeekBarIntOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000034(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, int i, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            this.this$0.wParams.x = i;
            this.this$0.wManager.updateViewLayout(this.this$0.WaterMark, this.this$0.wParams);
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, int i) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
            }
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, int i) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            AlGuiData.getGameFrontSightSPED(this.this$0.context).putInt((String) AlGuiData.getGameFrontSightData().get("游戏准星X偏移键名"), seekBar.getProgress());
            AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000035, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000035 implements AlGui.T_SeekBarIntOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000035(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, int i, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            this.this$0.wParams.y = i;
            this.this$0.wManager.updateViewLayout(this.this$0.WaterMark, this.this$0.wParams);
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, int i) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showAlertNotification_Simplicity(null, "应用失败", "游戏准星未初始化，无法设置！", 5000);
            }
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, int i) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                return;
            }
            AlGuiData.getGameFrontSightSPED(this.this$0.context).putInt((String) AlGuiData.getGameFrontSightData().get("游戏准星Y偏移键名"), seekBar.getProgress());
            AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
        }
    }

    /* renamed from: irene.window.algui.AlGuiPrefabricatedMenu_LB$100000036, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000036 implements AlGui.T_ButtonOnChangeListener {
        private final AlGuiPrefabricatedMenu_LB this$0;

        AnonymousClass100000036(AlGuiPrefabricatedMenu_LB alGuiPrefabricatedMenu_LB) {
            this.this$0 = alGuiPrefabricatedMenu_LB;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            if (this.this$0.WaterMark == null || this.this$0.wManager == null || this.this$0.wParams == null) {
                AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "恢复失败", "游戏准星未初始化，无法恢复！", 5000);
                return;
            }
            AlGuiData.getGameFrontSightSPED(this.this$0.context).clear();
            AlGuiData.getGameFrontSightSPED(this.this$0.context).apply();
            this.this$0.updateFrontSightCF();
            this.this$0.wManager.updateViewLayout(this.this$0.WaterMark, this.this$0.wParams);
            AlGuiBubbleNotification.Inform(this.this$0.context).showSuccessNotification_Simplicity(null, "恢复成功", "已重置准星所有配置", 5000);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean initFrontSight() {
        if (this.context == null) {
            return false;
        }
        if (!AppPermissionTool.isAndroidManifestPermissionExist(this.context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(this.context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示准星，只有声明了此权限才能显示准星！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
            return false;
        }
        this.wManager = (WindowManager) this.context.getSystemService("window");
        this.wParams = new WindowManager.LayoutParams();
        this.wParams.flags = 1336;
        this.wParams.gravity = 17;
        ((ViewGroup.LayoutParams) this.wParams).width = -2;
        ((ViewGroup.LayoutParams) this.wParams).height = -2;
        this.wParams.format = 1;
        this.wParams.windowAnimations = android.R.style.Animation_Toast;
        this.wParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        this.WaterMark = new TextView(this.context);
        this.WaterMark.setVisibility(8);
        updateFrontSightCF();
        this.wManager.addView(this.WaterMark, this.wParams);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFrontSightCF() {
        if (this.wParams == null || this.WaterMark == null || this.wManager == null) {
            return;
        }
        this.wParams.x = AlGuiData.getGameFrontSightSP(this.context).getInt((String) AlGuiData.getGameFrontSightData().get("游戏准星X偏移键名"), ((Integer) AlGuiData.getGameFrontSightData().get("游戏准星X偏移默认数据")).intValue());
        this.wParams.y = AlGuiData.getGameFrontSightSP(this.context).getInt((String) AlGuiData.getGameFrontSightData().get("游戏准星Y偏移键名"), ((Integer) AlGuiData.getGameFrontSightData().get("游戏准星Y偏移默认数据")).intValue());
        this.WaterMark.setTextSize(0, ViewTool.convertDpToPx(this.context, AlGuiData.getGameFrontSightSP(this.context).getFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星大小键名"), ((Float) AlGuiData.getGameFrontSightData().get("游戏准星大小默认数据")).floatValue())));
        this.WaterMark.setTextColor(AlGuiData.getGameFrontSightSP(this.context).getInt((String) AlGuiData.getGameFrontSightData().get("游戏准星颜色键名"), ((Integer) AlGuiData.getGameFrontSightData().get("游戏准星颜色默认数据")).intValue()));
        this.WaterMark.setText(AlGuiData.getGameFrontSightSP(this.context).getString((String) AlGuiData.getGameFrontSightData().get("游戏准星样式键名"), (String) AlGuiData.getGameFrontSightData().get("游戏准星样式默认数据")));
        this.WaterMark.setAlpha(AlGuiData.getGameFrontSightSP(this.context).getFloat((String) AlGuiData.getGameFrontSightData().get("游戏准星透明度键名"), ((Float) AlGuiData.getGameFrontSightData().get("游戏准星透明度默认数据")).floatValue()));
    }
}
