package irene.window.algui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import irene.window.algui.AlGui;
import irene.window.algui.Tools.AppPermissionTool;
import irene.window.algui.Tools.HackerTool;
import irene.window.algui.Tools.VariousTools;
import java.util.Locale;

/* loaded from: classes.dex */
public class Loading {
    public static final String TAG = "Loading";
    private static Context context;
    static int value = 0;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000003, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000003 implements AlGui.T_SeekBarIntOnChangeListener {
        AnonymousClass100000003() {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, int i, boolean z) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, int i) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarIntOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, int i) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000004, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000004 implements AlGui.T_SeekBarFloatOnChangeListener {
        AnonymousClass100000004() {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }

        @Override // irene.window.algui.AlGui.T_SeekBarFloatOnChangeListener
        public void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000005, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000005 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000005() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000006, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000006 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000006() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000007, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000007 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000007() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000008, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000008 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000008() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000009, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000009 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000009() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000010, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000010 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000010() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000011, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000011 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000011() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000012, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000012 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000012() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000013, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000013 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000013() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000021, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000021 implements AlGui.T_CheckBoxOnChangeListener {
        AnonymousClass100000021() {
        }

        @Override // irene.window.algui.AlGui.T_CheckBoxOnChangeListener
        public void onClick(CompoundButton compoundButton, boolean z) {
        }
    }

    public static void start(Context context2) {
        context = context2;
        AppPermissionTool.floatingWindowPermission(context);
        AlGuiDialogBox.showTextDiaLog(context2, "删除“QQ”", "若删除“QQ”，其所有数据也将被删除", "取消");
        initConfigurations();
        initMenu();
        AlGui.GUI(context).showBall();
    }

    private static void initConfigurations() {
        AlGui.GUI(context).getMenuMainTitle().setText("ALGUI");
        AlGui.GUI(context).getMenuMainTitle().setTextSize(11.0f);
        AlGui.GUI(context).getMenuSubTitle().setText("版本：1.0");
        AlGui.GUI(context).getMenuExplanation().setText("作者：艾琳⼁仅供用于学习交流请勿用于违法用途⼁如果您有任何疑问请进游戏逆向交流群：730967224进行交流讨论");
        AlGui.GUI(context).getMenuBottomLeftButton().setText("隐藏/退出");
        AlGui.GUI(context).getMenuBottomRightButton().setText("最小化");
        float f = 50;
        AlGui.GUI(context).setBallImage("shi.gif", f, f);
        AlGui.GUI(context).setAllViewMargins(8, 8, 8, 8);
        AlGuiData.menuScrollWidth = 809.0f;
        AlGuiData.menuScrollHeight = 554.0f;
        AlGuiData.rootLayoutFilletRadius = 10.9f;
        AlGuiData.rootLayoutStrokeWidth = 0.4f;
        AlGuiData.menuTopLineFilletRadius = 20.0f;
        AlGuiData.menuTransparency = 1.0f;
        AlGuiData.rootLayoutBackColor = -13619152;
        AlGuiData.rootLayoutStrokeColor = -12434878;
        AlGuiData.menuTopLineColor = -4342339;
        AlGuiData.menuMainTitleTextColor = -1;
        AlGuiData.menuSubTitleTextColor = 1627389951;
        AlGuiData.menuLiveStreamIconColor = -4342339;
        AlGuiData.menuExplanationBackColor = 1627389951;
        AlGuiData.menuExplanationTextColor = -1;
        AlGuiData.menuScrollBackColor = -14606047;
        AlGuiData.menuBottLeftButtonTextColor = -1;
        AlGuiData.menuBottRightButtonTextColor = -1;
        AlGuiData.menuBottRightTriangleColor = -12434878;
        AlGui.GUI(context).updateMenuAppearance();
        AlGui.GUI(context).updateMenu();
    }

    private static void initMenu() {
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).PrefabricatedMenu().addExplanation(null));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).PrefabricatedMenu().addAttributeStatusMenu(null));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).PrefabricatedMenu().addAppearanceSettingsMenu(null));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).PrefabricatedMenu().addGameFrontSightMenu(null));
        float f = 3;
        float f2 = 0;
        float f3 = 11;
        float f4 = 9;
        float f5 = 5;
        float f6 = 8;
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).addCollapse((CharSequence) "折叠菜单1", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, f, -1, f2, -3814679, false, AlGui.GUI(context).addLine(0.5f, -2039584, true), AlGui.GUI(context).addLinearLayout(17, 0, -1, -2, AlGui.GUI(context).addLine(2.0f, -2039584, false), AlGui.GUI(context).addTextView("这是一个普通文本", f3, ViewCompat.MEASURED_STATE_MASK, null)), AlGui.GUI(context).addMarqueeTextView(3, "这是一个可以滚动的字幕文本", f3, ViewCompat.MEASURED_STATE_MASK, null, 5000, -1, true), AlGui.GUI(context).addWebView("<html><head><style>h1, img { display: inline-block; vertical-align: middle; }</style></head><body><h1>这是一个HTML视图</h1><img width='10%' src='https://img2.imgtp.com/2024/02/15/h1mOgOxq.jpg' ></body></html>"), AlGui.GUI(context).addEditText(f4, null, -3223858, "这是一个输入框", -1040122112, "", f5, -1476395008, f2, 0, new AnonymousClass100000000()), AlGui.GUI(context).addButton("普通按钮", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000001()), AlGui.GUI(context).addSwitch("开关", f3, ViewCompat.MEASURED_STATE_MASK, null, "说明：这是一个开关", f6, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000002()), AlGui.GUI(context).addSeekBarInt("整数拖动条", f3, ViewCompat.MEASURED_STATE_MASK, null, 0, 0, 100, -11234587, -11234587, -11234587, new AnonymousClass100000003()), AlGui.GUI(context).addSeekBarFloat("小数拖动条", f3, ViewCompat.MEASURED_STATE_MASK, null, f2, f2, 100, -11234587, -11234587, -11234587, new AnonymousClass100000004()), AlGui.GUI(context).addLinearLayout(17, 0, -2, -2, AlGui.GUI(context).addCheckBox("复选框1", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000005()), AlGui.GUI(context).addCheckBox("复选框2", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000006())), AlGui.GUI(context).addLinearLayout(17, 0, -1, -2, AlGui.GUI(context).addSmallButton("小按钮1", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, new AnonymousClass100000007()), AlGui.GUI(context).addSmallButton("小按钮2", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, new AnonymousClass100000008()))));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).addCollapse((CharSequence) "折叠菜单2", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, f, -1, f2, -3814679, false, new View[0]));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).addTextView("这是一个普通文本", f3, -1, null));
        AlGui.GUI(context).getMenuScrollingListLayout().addView(AlGui.GUI(context).addButton("普通按钮", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000009()));
        LinearLayout addLinearLayout = AlGui.GUI(context).addLinearLayout(AlGui.GUI(context).getMenuScrollingListLayout(), 3, 0, -1, -2);
        LinearLayout addCollapse = AlGui.GUI(context).addCollapse((ViewGroup) AlGui.GUI(context).getMenuScrollingListLayout(), (CharSequence) "测试折叠菜单1", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, f, -1, f2, -3814679, false);
        LinearLayout addLinearLayout2 = AlGui.GUI(context).addLinearLayout(addCollapse, 3, 0, -1, -2);
        AlGui.GUI(context).addCheckBox(addLinearLayout2, "复选框", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000010());
        AlGui.GUI(context).addCheckBox(addLinearLayout2, "复选框", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000011());
        AlGui.GUI(context).addCheckBox(addLinearLayout2, "复选框", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000012());
        AlGui.GUI(context).addCheckBox(addLinearLayout2, "复选框", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000013());
        LinearLayout addLinearLayout3 = AlGui.GUI(context).addLinearLayout(addCollapse, 3, 0, -1, -2);
        AlGui.GUI(context).addSwitch(addLinearLayout3, "开关", f3, ViewCompat.MEASURED_STATE_MASK, null, "说明：这是一个开关", f6, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000014());
        AlGui.GUI(context).addSwitch(addLinearLayout3, "开关", f3, ViewCompat.MEASURED_STATE_MASK, null, "说明：这是一个开关", f6, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000015());
        AlGui.GUI(context).addSwitch(addLinearLayout3, "开关", f3, ViewCompat.MEASURED_STATE_MASK, null, "说明：这是一个开关", f6, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000016());
        AlGui.GUI(context).addSwitch(addLinearLayout3, "开关", f3, ViewCompat.MEASURED_STATE_MASK, null, "说明：这是一个开关", f6, -6381922, null, -11751600, -10044566, -769226, -1092784, new AnonymousClass100000017());
        LinearLayout addCollapse2 = AlGui.GUI(context).addCollapse((ViewGroup) addLinearLayout, (CharSequence) "测试折叠菜单2", 10, ViewCompat.MEASURED_STATE_MASK, (Typeface) null, f, -1, f2, -3814679, false);
        LinearLayout addLinearLayout4 = AlGui.GUI(context).addLinearLayout(addCollapse2, 16, 0, -1, -2);
        AlGui.GUI(context).addTextView(addLinearLayout4, "调节器", f3, -1092784, null);
        TextView addTextView = AlGui.GUI(context).addTextView(new StringBuffer().append(value).append("").toString(), f3, -1092784, null);
        AlGui.GUI(context).addSmallButton(addLinearLayout4, " - ", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, new AnonymousClass100000018(addTextView));
        addLinearLayout4.addView(addTextView);
        AlGui.GUI(context).addSmallButton(addLinearLayout4, " + ", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, new AnonymousClass100000019(addTextView));
        AlGui.GUI(context).addSmallButton(addLinearLayout4, "确认修改", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, new AnonymousClass100000020());
        AlGui.GUI(context).addCheckBox(addLinearLayout4, "复选框1", 11.0f, ViewCompat.MEASURED_STATE_MASK, null, -12627531, new AnonymousClass100000021());
        LinearLayout addLinearLayout5 = AlGui.GUI(context).addLinearLayout(addCollapse2, 3, 1, -1, -2);
        AlGui.GUI(context).addEditText(addLinearLayout5, f4, null, -3223858, "这是一个输入框", -1040122112, "", f5, -1476395008, f2, 0, -1, "确认", f5, -11234587, f2, -1, new AnonymousClass100000022());
        AlGui.GUI(context).addButton(addLinearLayout5, "普通按钮", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000023());
        AlGui.GUI(context).addEditText(addLinearLayout5, f4, null, -3223858, "这是一个输入框", -1040122112, "", f5, -1476395008, f2, 0, -1, "确认", f5, -11234587, f2, -1, new AnonymousClass100000024());
        AlGui.GUI(context).addButton(addLinearLayout5, "普通按钮", f3, -1, null, f5, -12627531, f2, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AnonymousClass100000025());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000000, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000000 implements AlGui.T_EditTextOnChangeListener {
        AnonymousClass100000000() {
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
            editText.getText().toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000001, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000001 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000001() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            AlGuiWindowView.showWebView(Loading.context, "视频", new StringBuffer().append(new StringBuffer().append("<html><body><video width=\"100%\" height=\"auto\" controls><source src=\"").append("http://api.yujn.cn/api/xjj.php").toString()).append("\" type=\"video/mp4\"></video></body></html>").toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000002, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000002 implements AlGui.T_SwitchOnChangeListener {
        AnonymousClass100000002() {
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            String charSequence = compoundButton.getText().toString();
            textView.getText().toString();
            if (z) {
                HackerTool.linuxHackerFile(Loading.context, "libModOn.so");
                AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "开启成功！", 5000);
                VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("开启成功！").toString(), Locale.CHINA);
                return;
            }
            HackerTool.linuxHackerFile(Loading.context, "libModOff.so");
            AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "关闭成功！", 5000);
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("关闭成功！").toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000014, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000014 implements AlGui.T_SwitchOnChangeListener {
        AnonymousClass100000014() {
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            String charSequence = compoundButton.getText().toString();
            textView.getText().toString();
            if (z) {
                HackerTool.linuxHackerFile(Loading.context, "libModOn.so");
                AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "开启成功！", 5000);
                VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("开启成功！").toString(), Locale.CHINA);
                return;
            }
            HackerTool.linuxHackerFile(Loading.context, "libModOff.so");
            AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "关闭成功！", 5000);
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("关闭成功！").toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000015, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000015 implements AlGui.T_SwitchOnChangeListener {
        AnonymousClass100000015() {
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            String charSequence = compoundButton.getText().toString();
            textView.getText().toString();
            if (z) {
                HackerTool.linuxHackerFile(Loading.context, "libModOn.so");
                AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "开启成功！", 5000);
                VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("开启成功！").toString(), Locale.CHINA);
                return;
            }
            HackerTool.linuxHackerFile(Loading.context, "libModOff.so");
            AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "关闭成功！", 5000);
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("关闭成功！").toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000016, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000016 implements AlGui.T_SwitchOnChangeListener {
        AnonymousClass100000016() {
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            String charSequence = compoundButton.getText().toString();
            textView.getText().toString();
            if (z) {
                HackerTool.linuxHackerFile(Loading.context, "libModOn.so");
                AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "开启成功！", 5000);
                VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("开启成功！").toString(), Locale.CHINA);
                return;
            }
            HackerTool.linuxHackerFile(Loading.context, "libModOff.so");
            AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "关闭成功！", 5000);
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("关闭成功！").toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000017, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000017 implements AlGui.T_SwitchOnChangeListener {
        AnonymousClass100000017() {
        }

        @Override // irene.window.algui.AlGui.T_SwitchOnChangeListener
        public void onClick(CompoundButton compoundButton, TextView textView, boolean z) {
            String charSequence = compoundButton.getText().toString();
            textView.getText().toString();
            if (z) {
                HackerTool.linuxHackerFile(Loading.context, "libModOn.so");
                AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "开启成功！", 5000);
                VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("开启成功！").toString(), Locale.CHINA);
                return;
            }
            HackerTool.linuxHackerFile(Loading.context, "libModOff.so");
            AlGuiBubbleNotification.Inform(Loading.context).showSuccessNotification_Simplicity(null, charSequence, "关闭成功！", 5000);
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append(charSequence).append("关闭成功！").toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000018, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000018 implements AlGui.T_ButtonOnChangeListener {
        private final TextView val$valueText;

        AnonymousClass100000018(TextView textView) {
            this.val$valueText = textView;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            Loading.value--;
            this.val$valueText.setText(new StringBuffer().append(Loading.value).append("").toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000019, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000019 implements AlGui.T_ButtonOnChangeListener {
        private final TextView val$valueText;

        AnonymousClass100000019(TextView textView) {
            this.val$valueText = textView;
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            Loading.value++;
            this.val$valueText.setText(new StringBuffer().append(Loading.value).append("").toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000020, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000020 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000020() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            VariousTools.convertTextToSpeech(Loading.context, new StringBuffer().append("数值为").append(Loading.value).toString(), Locale.CHINA);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000022, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000022 implements AlGui.T_EditTextOnChangeListener {
        AnonymousClass100000022() {
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
            editText.getText().toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000023, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000023 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000023() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            AlGuiWindowView.showWebView(Loading.context, "视频", new StringBuffer().append(new StringBuffer().append("<html><body><video width=\"100%\" height=\"auto\" controls><source src=\"").append("http://api.yujn.cn/api/xjj.php").toString()).append("\" type=\"video/mp4\"></video></body></html>").toString());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000024, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000024 implements AlGui.T_EditTextOnChangeListener {
        AnonymousClass100000024() {
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
            editText.getText().toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.Loading$100000025, reason: invalid class name */
    /* loaded from: classes.dex */
    static public class AnonymousClass100000025 implements AlGui.T_ButtonOnChangeListener {
        AnonymousClass100000025() {
        }

        @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            if (z) {
                gradientDrawable.setColor(-1092784);
            } else {
                gradientDrawable.setColor(-10044566);
            }
        }
    }
}
