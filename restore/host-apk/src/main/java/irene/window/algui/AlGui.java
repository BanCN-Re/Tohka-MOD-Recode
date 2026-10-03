package irene.window.algui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import irene.window.algui.AlGuiData;
import irene.window.algui.CustomizeView.GradientTextView;
import irene.window.algui.CustomizeView.MarqueeTextView;
import irene.window.algui.CustomizeView.vFrameLayout;
import irene.window.algui.CustomizeView.vLinearLayout;
import irene.window.algui.Tools.AppPermissionTool;
import irene.window.algui.Tools.ImageTool;
import irene.window.algui.Tools.ViewTool;
import java.io.IOException;

/* loaded from: classes.dex */
public class AlGui {
    public static final String TAG = "AlGui";
    private static AlGui algui;
    private Context aContext;
    private vLinearLayout ballLayout;
    private WindowManager ballManager;
    private WindowManager.LayoutParams ballParams;
    private TextView edition;
    private Button leftButton;
    private vLinearLayout lineTouchLayout;
    private ImageView liveStreamIcon;
    private vLinearLayout liveStreamLayout;
    private vLinearLayout mainLayout;
    private vFrameLayout menuLayout;
    private WindowManager menuManager;
    private WindowManager.LayoutParams menuParams;
    private AlGuiPrefabricatedMenu_LB prefabricatedMenu;
    private RelativeLayout relativeLayout;
    private Button rightButton;
    private ScrollView scroll;
    private vLinearLayout scrollLayout;
    private TextView subTitle;
    private vLinearLayout subTitleLayout;
    private TextView title;
    private vLinearLayout titleLayout;
    private vLinearLayout titleLiveStreamRootLayout;
    private vLinearLayout touchMoveLine;
    private Paint trianglePaint;
    private Path trianglePath;
    private View zoomTriangleView;
    private int[] viewWBJ = {8, 8, 8, 8};
    private boolean isBallView = false;
    private boolean isMenuView = false;

    /* loaded from: classes.dex */
    public interface T_ButtonOnChangeListener {
        void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z);
    }

    /* loaded from: classes.dex */
    public interface T_CheckBoxOnChangeListener {
        void onClick(CompoundButton compoundButton, boolean z);
    }

    /* loaded from: classes.dex */
    public interface T_EditTextOnChangeListener {
        void afterTextChanged(EditText editText, Editable editable);

        void beforeTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3);

        void buttonOnClick(EditText editText, View view, TextView textView, boolean z);

        void onTextChanged(EditText editText, CharSequence charSequence, int i, int i2, int i3);
    }

    /* loaded from: classes.dex */
    public interface T_SeekBarFloatOnChangeListener {
        void onProgressChanged(TextView textView, SeekBar seekBar, float f, boolean z);

        void onStartTrackingTouch(TextView textView, SeekBar seekBar, float f);

        void onStopTrackingTouch(TextView textView, SeekBar seekBar, float f);
    }

    /* loaded from: classes.dex */
    public interface T_SeekBarIntOnChangeListener {
        void onProgressChanged(TextView textView, SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(TextView textView, SeekBar seekBar, int i);

        void onStopTrackingTouch(TextView textView, SeekBar seekBar, int i);
    }

    /* loaded from: classes.dex */
    public interface T_SwitchOnChangeListener {
        void onClick(CompoundButton compoundButton, TextView textView, boolean z);
    }

    public Context getContext() {
        return this.aContext;
    }

    public AlGuiPrefabricatedMenu_LB PrefabricatedMenu() {
        if (this.prefabricatedMenu == null) {
            this.prefabricatedMenu = new AlGuiPrefabricatedMenu_LB(this.aContext, algui);
        }
        return this.prefabricatedMenu;
    }

    public WindowManager getBallWindow() {
        return this.ballManager;
    }

    public WindowManager.LayoutParams getBallWindowParams() {
        return this.ballParams;
    }

    public vLinearLayout getBallLayout() {
        return this.ballLayout;
    }

    public WindowManager getMenuWindow() {
        return this.menuManager;
    }

    public WindowManager.LayoutParams getMenuWindowParams() {
        return this.menuParams;
    }

    public vFrameLayout getMenuRootLayout() {
        return this.menuLayout;
    }

    public vLinearLayout getMenuMainLayout() {
        return this.mainLayout;
    }

    public vLinearLayout getMenuTopLayout() {
        return this.lineTouchLayout;
    }

    public vLinearLayout getMenuTopLine() {
        return this.touchMoveLine;
    }

    public vLinearLayout getMenuTitleLiveStreamRootLayout() {
        return this.titleLiveStreamRootLayout;
    }

    public vLinearLayout getMenuTitleLayout() {
        return this.titleLayout;
    }

    public TextView getMenuMainTitle() {
        return this.title;
    }

    public TextView getMenuSubTitle() {
        return this.edition;
    }

    public vLinearLayout getMenuLiveStreamLayout() {
        return this.liveStreamLayout;
    }

    public ImageView getMenuLiveStreamIcon() {
        return this.liveStreamIcon;
    }

    public vLinearLayout getMenuExplanationLayout() {
        return this.subTitleLayout;
    }

    public TextView getMenuExplanation() {
        return this.subTitle;
    }

    public ScrollView getMenuScrollingList() {
        return this.scroll;
    }

    public vLinearLayout getMenuScrollingListLayout() {
        return this.scrollLayout;
    }

    public RelativeLayout getMenuBottomLayout() {
        return this.relativeLayout;
    }

    public Button getMenuBottomLeftButton() {
        return this.leftButton;
    }

    public Button getMenuBottomRightButton() {
        return this.rightButton;
    }

    public View getMenuBottomRightTriangleView() {
        return this.zoomTriangleView;
    }

    public Path getMenuBottomRightTrianglePath() {
        return this.trianglePath;
    }

    public Paint getMenuBottomRightTrianglePaint() {
        return this.trianglePaint;
    }

    public static AlGui GUI(Context context) {
        if (algui == null) {
            algui = new AlGui(context);
        }
        return algui;
    }

    AlGui(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("实例化时传入的Context上下文对象为空！null");
        }
        this.aContext = context;
        initBallWindow();
        initBall();
        initMenuWindow();
        initMenu();
        updateMenuAppearance();
        setAllViewMargins(8, 8, 8, 8);
        float f = 50;
        setBallImage(null, f, f);
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW")) {
            if (AppPermissionTool.checkOverlayPermission(this.aContext)) {
                return;
            }
            if (Build.VERSION.SDK_INT < 26) {
                Toast.makeText(this.aContext, "授予悬浮窗权限后重进才能显示悬浮窗！", 1).show();
                return;
            } else {
                Toast.makeText(this.aContext, "请授予悬浮窗权限，才能显示窗口", 1).show();
                return;
            }
        }
        Toast.makeText(this.aContext, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示悬浮窗，只有声明了此权限才能授予悬浮窗权限！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0029  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public Drawable setBallImage(String str, float f, float f2) {
        Drawable decodeDrawable;
        if (this.ballLayout.getChildCount() > 0) {
            this.ballLayout.removeAllViews();
        }
        if (str != null) {
            try {
                decodeDrawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(this.aContext.getAssets(), str));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (decodeDrawable == null) {
                ImageView imageView = new ImageView(this.aContext);
                imageView.setBackground(decodeDrawable);
                this.ballLayout.addView(imageView, ViewTool.convertDpToPx(this.aContext, f), ViewTool.convertDpToPx(this.aContext, f2));
                if (decodeDrawable instanceof AnimatedImageDrawable) {
                    AnimatedImageDrawable animatedImageDrawable = (AnimatedImageDrawable) decodeDrawable;
                    animatedImageDrawable.start();
                    return animatedImageDrawable;
                }
                return decodeDrawable;
            }
            int convertDpToPx = ViewTool.convertDpToPx(this.aContext, 1.5f);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setColor(-13619152);
            gradientDrawable.setStroke(convertDpToPx, -12434878);
            LinearLayout linearLayout = new LinearLayout(this.aContext);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewTool.convertDpToPx(this.aContext, f), ViewTool.convertDpToPx(this.aContext, f)));
            linearLayout.setGravity(17);
            linearLayout.setBackground(gradientDrawable);
            linearLayout.setPadding(convertDpToPx, convertDpToPx, convertDpToPx, convertDpToPx);
            TextView textView = new TextView(this.aContext);
            textView.setGravity(17);
            textView.setText("MOD");
            textView.setTextColor(-1);
            textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 15.0f));
            linearLayout.addView(textView);
            this.ballLayout.addView(linearLayout);
            return null;
        }
        decodeDrawable = null;
        if (decodeDrawable == null) {
        }
    }

    public void setAllViewMargins(int i, int i2, int i3, int i4) {
        this.viewWBJ = new int[]{i, i2, i3, i4};
    }

    public void updateMenuAppearance() {
        this.menuLayout.setFilletRadiu(ViewTool.convertDpToPx(this.aContext, AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("根布局圆角半径键名"), ((Float) AlGuiData.getMenuAttributeData().get("根布局圆角半径默认数据")).floatValue())));
        this.menuLayout.setBorder(ViewTool.convertDpToPx(this.aContext, AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("根布局描边宽度键名"), ((Float) AlGuiData.getMenuAttributeData().get("根布局描边宽度默认数据")).floatValue())), AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("根布局描边颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("根布局描边颜色默认数据")).intValue()));
        this.menuLayout.setBackColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("根布局背景颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("根布局背景颜色默认数据")).intValue()));
        this.menuLayout.setAlpha(AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单透明度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单透明度默认数据")).floatValue()));
        this.touchMoveLine.setBackColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单顶部线条颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单顶部线条颜色默认数据")).intValue()));
        this.touchMoveLine.setFilletRadiu(ViewTool.convertDpToPx(this.aContext, AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单顶部线条圆角半径键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单顶部线条圆角半径默认数据")).floatValue())));
        this.title.setTextColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单主标题文本颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单主标题文本颜色默认数据")).intValue()));
        this.edition.setTextColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单副标题文本颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单副标题文本颜色默认数据")).intValue()));
        this.liveStreamIcon.setImageTintList(ColorStateList.valueOf(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单直播模式图标颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单直播模式图标颜色默认数据")).intValue())));
        this.subTitleLayout.setBackgroundColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单说明背景颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单说明背景颜色默认数据")).intValue()));
        this.subTitle.setTextColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单说明文本颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单说明文本颜色默认数据")).intValue()));
        this.scroll.setBackgroundColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单滚动列表背景颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单滚动列表背景颜色默认数据")).intValue()));
        this.scroll.getLayoutParams().width = (int) AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表宽度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单滚动列表宽度默认数据")).floatValue());
        this.scroll.getLayoutParams().height = (int) AlGuiData.getMenuAttributeSP(this.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表高度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单滚动列表高度默认数据")).floatValue());
        this.leftButton.setTextColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单左下角按钮文本颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单左下角按钮文本颜色默认数据")).intValue()));
        this.rightButton.setTextColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单右下角按钮文本颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单右下角按钮文本颜色默认数据")).intValue()));
        this.trianglePaint.setColor(AlGuiData.getMenuColorSP(this.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单右下角三角形颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单右下角三角形颜色默认数据")).intValue()));
        this.zoomTriangleView.invalidate();
        ((ViewGroup.LayoutParams) this.menuParams).width = -2;
        ((ViewGroup.LayoutParams) this.menuParams).height = -2;
        updateMenu();
    }

    private void initMenu() {
        this.menuLayout = new vFrameLayout(this.aContext);
        this.menuLayout.setClipChildren(true);
        this.menuLayout.setLayerType(2, null);
        this.mainLayout = new vLinearLayout(this.aContext);
        this.mainLayout.setBackgroundColor(0);
        this.mainLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.mainLayout.setOrientation(1);
        this.lineTouchLayout = new vLinearLayout(this.aContext);
        this.lineTouchLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.lineTouchLayout.setOrientation(1);
        this.lineTouchLayout.setGravity(1);
        this.touchMoveLine = new vLinearLayout(this.aContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewTool.convertDpToPx(this.aContext, 50), ViewTool.convertDpToPx(this.aContext, 3));
        layoutParams.setMargins(0, 16, 0, 16);
        this.touchMoveLine.setLayoutParams(layoutParams);
        this.titleLiveStreamRootLayout = new vLinearLayout(this.aContext);
        this.titleLiveStreamRootLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.titleLiveStreamRootLayout.setOrientation(0);
        this.titleLiveStreamRootLayout.setGravity(16);
        this.titleLayout = new vLinearLayout(this.aContext);
        this.titleLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        this.titleLayout.setOrientation(1);
        this.titleLayout.setPadding(20, 20, 20, 20);
        this.titleLayout.setGravity(3);
        this.title = new TextView(this.aContext);
        this.title.setText("ALGUI");
        this.title.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 15.0f));
        this.edition = new TextView(this.aContext);
        this.edition.setText("版本：2.0");
        this.edition.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 8.0f));
        this.liveStreamLayout = new vLinearLayout(this.aContext);
        this.liveStreamLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        this.liveStreamLayout.setOrientation(1);
        this.liveStreamLayout.setPadding(20, 20, 50, 40);
        this.liveStreamLayout.setGravity(5);
        this.liveStreamIcon = new ImageView(this.aContext);
        float f = 30;
        this.liveStreamIcon.setLayoutParams(new ViewGroup.LayoutParams(ViewTool.convertDpToPx(this.aContext, f), ViewTool.convertDpToPx(this.aContext, f)));
        this.liveStreamIcon.setImageBitmap(ImageTool.getBase64Image(AlGuiData.getVideo_Icon_LiveEnd()));
        this.subTitleLayout = new vLinearLayout(this.aContext);
        this.subTitleLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        this.subTitleLayout.setOrientation(0);
        this.subTitleLayout.setPadding(20, 5, 20, 5);
        this.subTitle = new TextView(this.aContext);
        this.subTitle.setMovementMethod(LinkMovementMethod.getInstance());
        this.subTitle.setText("作者：艾琳⼁仅供用于学习交流请勿用于违法用途⼁如果您有任何疑问请进游戏逆向交流群：730967224进行交流讨论");
        this.subTitle.setGravity(3);
        this.subTitle.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 8.0f));
        this.subTitle.setSelected(true);
        this.subTitle.setHorizontallyScrolling(true);
        this.subTitle.setSingleLine(true);
        this.subTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        this.subTitle.setMarqueeRepeatLimit(-1);
        this.subTitle.setFocusable(true);
        this.subTitle.setFocusableInTouchMode(true);
        this.subTitle.requestFocus();
        this.subTitle.requestFocusFromTouch();
        this.scroll = new ScrollView(this.aContext);
        this.scroll.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));
        this.scroll.setHorizontalScrollBarEnabled(false);
        this.scroll.setVerticalScrollBarEnabled(false);
        this.scroll.setLayerType(2, null);
        this.scrollLayout = new vLinearLayout(this.aContext);
        this.scrollLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.scrollLayout.setOrientation(1);
        this.scrollLayout.setPadding(20, 20, 20, 20);
        this.scrollLayout.setVerticalGravity(16);
        this.relativeLayout = new RelativeLayout(this.aContext);
        this.relativeLayout.setPadding(10, 3, 10, 3);
        this.relativeLayout.setVerticalGravity(17);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(9);
        this.leftButton = new Button(this.aContext);
        this.leftButton.setLayoutParams(layoutParams2);
        this.leftButton.setBackgroundColor(0);
        this.leftButton.setText("隐藏/退出");
        this.leftButton.setOnClickListener(new AnonymousClass100000004(this));
        this.leftButton.setOnLongClickListener(new AnonymousClass100000008(this));
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams3.addRule(11);
        this.rightButton = new Button(this.aContext);
        this.rightButton.setLayoutParams(layoutParams3);
        this.rightButton.setBackgroundColor(0);
        this.rightButton.setText("最小化");
        this.rightButton.setOnClickListener(new View.OnClickListener(this) { // from class: irene.window.algui.AlGui.100000009
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.this$0.clearMenu();
                this.this$0.showBall();
            }
        });
        this.trianglePaint = new Paint();
        this.trianglePaint.setStyle(Paint.Style.FILL);
        this.zoomTriangleView = new View(this, this.aContext) { // from class: irene.window.algui.AlGui.100000010
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                this.this$0.trianglePath = new Path();
                this.this$0.trianglePath.moveTo(0, getHeight());
                this.this$0.trianglePath.lineTo(getWidth(), getHeight());
                this.this$0.trianglePath.lineTo(getWidth(), getHeight() - ViewTool.convertDpToPx(this.this$0.aContext, 19));
                this.this$0.trianglePath.close();
                canvas.drawPath(this.this$0.trianglePath, this.this$0.trianglePaint);
            }
        };
        this.zoomTriangleView.setBackgroundColor(0);
        this.zoomTriangleView.setOnTouchListener(new ResizeViewTouchListener(this));
        this.lineTouchLayout.addView(this.touchMoveLine);
        this.mainLayout.addView(this.lineTouchLayout);
        this.titleLayout.addView(this.title);
        this.titleLayout.addView(this.edition);
        this.titleLiveStreamRootLayout.addView(this.titleLayout);
        this.liveStreamLayout.addView(this.liveStreamIcon);
        this.titleLiveStreamRootLayout.addView(this.liveStreamLayout);
        this.mainLayout.addView(this.titleLiveStreamRootLayout);
        this.subTitleLayout.addView(this.subTitle);
        this.mainLayout.addView(this.subTitleLayout);
        this.scroll.addView(this.scrollLayout);
        this.mainLayout.addView(this.scroll);
        this.relativeLayout.addView(this.leftButton);
        this.relativeLayout.addView(this.rightButton);
        this.mainLayout.addView(this.relativeLayout);
        this.menuLayout.addView(this.mainLayout);
        float f2 = 25;
        this.menuLayout.addView(this.zoomTriangleView, new FrameLayout.LayoutParams(ViewTool.convertDpToPx(this.aContext, f2), ViewTool.convertDpToPx(this.aContext, f2), 8388693));
        this.liveStreamIcon.setOnClickListener(new AnonymousClass100000013(this));
        this.menuLayout.setOnTouchListener(new View.OnTouchListener(this) { // from class: irene.window.algui.AlGui.100000014
            private float downX;
            private float downY;
            private float moveX;
            private float moveY;
            private int signX;
            private int signY;
            private final AlGui this$0;
            boolean isOne = true;
            boolean isMove = false;
            int moveThreshold = 20;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case 0:
                        this.this$0.touchMoveLine.setBackColor(-14575885);
                        this.isOne = true;
                        this.isMove = false;
                        this.signX = this.this$0.menuParams.x;
                        this.signY = this.this$0.menuParams.y;
                        this.downX = motionEvent.getRawX();
                        this.downY = motionEvent.getRawY();
                        return true;
                    case 1:
                        this.this$0.menuLayout.setAlpha(AlGuiData.getMenuAttributeSP(this.this$0.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单透明度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单透明度默认数据")).floatValue()));
                        this.this$0.touchMoveLine.setBackColor(AlGuiData.getMenuColorSP(this.this$0.aContext).getInt((String) AlGuiData.getMenuColorData().get("菜单顶部线条颜色键名"), ((Integer) AlGuiData.getMenuColorData().get("菜单顶部线条颜色默认数据")).intValue()));
                        return true;
                    case 2:
                        float abs = Math.abs(motionEvent.getRawX() - this.downX);
                        float abs2 = Math.abs(motionEvent.getRawY() - this.downY);
                        if (abs > this.moveThreshold || abs2 > this.moveThreshold) {
                            this.isMove = true;
                        }
                        if (this.isMove) {
                            if (this.isOne) {
                                this.this$0.lineTouchLayout.performHapticFeedback(0);
                                this.this$0.menuLayout.setAlpha(AlGuiData.getMenuAttributeSP(this.this$0.aContext).getFloat((String) AlGuiData.getMenuAttributeData().get("菜单透明度键名"), ((Float) AlGuiData.getMenuAttributeData().get("菜单透明度默认数据")).floatValue()) / 2);
                                this.isOne = false;
                            }
                            this.this$0.menuParams.x = this.signX + ((int) (motionEvent.getRawX() - this.downX));
                            this.this$0.menuParams.y = this.signY + ((int) (motionEvent.getRawY() - this.downY));
                            this.this$0.updateMenu();
                        }
                        return true;
                    case 3:
                    default:
                        return false;
                    case 4:
                        this.this$0.clearMenu();
                        this.this$0.showBall();
                        return true;
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGui$100000004, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000004 implements View.OnClickListener {
        AlertDialog dialog;
        private final AlGui this$0;

        AnonymousClass100000004(AlGui alGui) {
            this.this$0 = alGui;
        }

        public void hideBall() {
            this.this$0.clearMenu();
            this.this$0.showBall();
            this.this$0.ballLayout.setAlpha(1);
            this.this$0.ballLayout.setBackgroundColor(-769226);
            new CountDownTimer(this, 5000, 1000) { // from class: irene.window.algui.AlGui.100000004.100000000
                private final AnonymousClass100000004 this$0;

                {
                    this.this$0 = this;
                }

                @Override // android.os.CountDownTimer
                public void onTick(long j) {
                }

                @Override // android.os.CountDownTimer
                public void onFinish() {
                    this.this$0.this$0.ballLayout.setBackgroundColor(0);
                    this.this$0.this$0.ballLayout.setAlpha(0);
                    this.this$0.this$0.updateBall();
                    AlGuiBubbleNotification.Inform(this.this$0.this$0.aContext).showMessageNotification_Exquisite(null, "悬浮球已隐藏", "再次拖动悬浮球以恢复", 5000);
                }
            }.start();
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (!AlGuiData.getDiaLogFlagSP(this.this$0.aContext).getBoolean((String) AlGuiData.getDiaLogFlagData().get("悬浮窗隐藏弹窗不再提示键名"), ((Boolean) AlGuiData.getDiaLogFlagData().get("悬浮窗隐藏弹窗不再提示默认数据")).booleanValue())) {
                float f = 15;
                float f2 = 50;
                float f3 = 0;
                this.dialog = AlGuiDialogBox.showDiaLog(this.this$0.aContext, -822412550, 50.0f, AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 1, -1, -2, AlGui.GUI(this.this$0.aContext).addTextView("隐藏悬浮球", 18, -14606047, Typeface.create(Typeface.DEFAULT, 1)), AlGui.GUI(this.this$0.aContext).addTextView("你确定要隐藏悬浮球吗？\n\n如果确定隐藏那么我们会高亮悬浮球五秒\n\n请在这段时间记住它的位置以便后面恢复\n\n再次拖动悬浮球将恢复并显示\n", 12.0f, -12434878, null)), AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 0, -1, -2, AlGui.GUI(this.this$0.aContext).addButton("确定", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000004.100000001
                    private final AnonymousClass100000004 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        this.this$0.hideBall();
                        this.this$0.dialog.dismiss();
                    }
                }), AlGui.GUI(this.this$0.aContext).addButton("不再提示", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000004.100000002
                    private final AnonymousClass100000004 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        AlGuiData.getDiaLogFlagSPED(this.this$0.this$0.aContext).putBoolean((String) AlGuiData.getDiaLogFlagData().get("悬浮窗隐藏弹窗不再提示键名"), true);
                        AlGuiData.getDiaLogFlagSPED(this.this$0.this$0.aContext).apply();
                        this.this$0.hideBall();
                        this.this$0.dialog.dismiss();
                    }
                }), AlGui.GUI(this.this$0.aContext).addButton("取消", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000004.100000003
                    private final AnonymousClass100000004 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        this.this$0.dialog.dismiss();
                    }
                })));
                return;
            }
            hideBall();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGui$100000008, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000008 implements View.OnLongClickListener {
        AlertDialog dialog;
        private final AlGui this$0;

        AnonymousClass100000008(AlGui alGui) {
            this.this$0 = alGui;
        }

        public void exitMenu() {
            AlGuiBubbleNotification.Inform(this.this$0.aContext).clearW();
            this.this$0.clearMenu();
            this.this$0.clearBall();
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (!AlGuiData.getDiaLogFlagSP(this.this$0.aContext).getBoolean((String) AlGuiData.getDiaLogFlagData().get("悬浮窗退出弹窗不再提示键名"), ((Boolean) AlGuiData.getDiaLogFlagData().get("悬浮窗退出弹窗不再提示默认数据")).booleanValue())) {
                float f = 15;
                float f2 = 50;
                float f3 = 0;
                this.dialog = AlGuiDialogBox.showDiaLog(this.this$0.aContext, -822412550, 50.0f, AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 1, -1, -2, AlGui.GUI(this.this$0.aContext).addTextView("退出悬浮窗", 18, -14606047, Typeface.create(Typeface.DEFAULT, 1)), AlGui.GUI(this.this$0.aContext).addTextView("你确定要退出悬浮窗吗？此操作不可恢复", 15.0f, -12434878, null)), AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 0, -1, -2, AlGui.GUI(this.this$0.aContext).addButton("确定", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000008.100000005
                    private final AnonymousClass100000008 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        this.this$0.exitMenu();
                        this.this$0.dialog.dismiss();
                    }
                }), AlGui.GUI(this.this$0.aContext).addButton("不再提示", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000008.100000006
                    private final AnonymousClass100000008 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        AlGuiData.getDiaLogFlagSPED(this.this$0.this$0.aContext).putBoolean((String) AlGuiData.getDiaLogFlagData().get("悬浮窗退出弹窗不再提示键名"), true);
                        AlGuiData.getDiaLogFlagSPED(this.this$0.this$0.aContext).apply();
                        this.this$0.exitMenu();
                        this.this$0.dialog.dismiss();
                    }
                }), AlGui.GUI(this.this$0.aContext).addButton("取消", f, -822412550, null, f2, -12627531, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000008.100000007
                    private final AnonymousClass100000008 this$0;

                    {
                        this.this$0 = this;
                    }

                    @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                    public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                        this.this$0.dialog.dismiss();
                    }
                })));
            } else {
                exitMenu();
            }
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGui$100000013, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000013 implements View.OnClickListener {
        AlertDialog dialog;
        private final AlGui this$0;
        boolean isOn = true;
        GradientTextView w = null;

        AnonymousClass100000013(AlGui alGui) {
            this.this$0 = alGui;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            float f = 15;
            float f2 = 50;
            float f3 = 0;
            this.dialog = AlGuiDialogBox.showDiaLog(this.this$0.aContext, -822412550, 50.0f, AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 1, -1, -2, AlGui.GUI(this.this$0.aContext).addTextView("直播模式", 18, -14606047, Typeface.create(Typeface.DEFAULT, 1)), AlGui.GUI(this.this$0.aContext).addTextView("启动直播模式后 直播或录屏画面将无法看见所有窗口\n\n只有在现实中的你自己可见", 12.0f, -12434878, null)), AlGui.GUI(this.this$0.aContext).addLinearLayout(17, 0, -1, -2, AlGui.GUI(this.this$0.aContext).addButton(this.isOn ? "开始直播" : "结束直播", f, -822412550, null, f2, this.isOn ? -12627531 : -1092784, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000013.100000011
                private final AnonymousClass100000013 this$0;

                {
                    this.this$0 = this;
                }

                @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                    if (this.this$0.isOn) {
                        AlGuiData.setIsLiveStream(this.this$0.this$0.aContext, true);
                        this.this$0.this$0.liveStreamIcon.setImageBitmap(ImageTool.getBase64Image(AlGuiData.getVideo_Icon_LiveStart()));
                        AlGuiBubbleNotification.Inform(this.this$0.this$0.aContext).showMessageNotification_Exquisite(null, "开始直播", "安全系统已加载，实时保证您的安全，现在您可以录屏或直播了", 5000);
                        if (this.this$0.w == null) {
                            this.this$0.w = AlGuiWindowView.showNeonLightText(this.this$0.this$0.aContext, "正在直播中…", new int[]{-65332, -13312, -16711732, -65434}, 11.0f, null, 8388691, 30, 30);
                        }
                        this.this$0.w.setText("正在直播中…");
                    } else {
                        AlGuiData.setIsLiveStream(this.this$0.this$0.aContext, false);
                        this.this$0.this$0.liveStreamIcon.setImageBitmap(ImageTool.getBase64Image(AlGuiData.getVideo_Icon_LiveEnd()));
                        AlGuiBubbleNotification.Inform(this.this$0.this$0.aContext).showMessageNotification_Exquisite(null, "结束直播", "安全系统已销毁", 5000);
                        if (this.this$0.w != null) {
                            this.this$0.w.setText(" ");
                        }
                    }
                    this.this$0.isOn = true ^ this.this$0.isOn;
                    this.this$0.dialog.dismiss();
                }
            }), AlGui.GUI(this.this$0.aContext).addButton("取消", f, -822412550, null, f2, -10395295, f3, ViewCompat.MEASURED_STATE_MASK, -1, -2, new T_ButtonOnChangeListener(this) { // from class: irene.window.algui.AlGui.100000013.100000012
                private final AnonymousClass100000013 this$0;

                {
                    this.this$0 = this;
                }

                @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                public void onClick(View view2, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                    this.this$0.dialog.dismiss();
                }
            })));
        }
    }

    private void initBall() {
        this.ballLayout = new vLinearLayout(this.aContext);
        this.ballLayout.setGravity(17);
        this.ballLayout.setOnClickListener(new View.OnClickListener(this) { // from class: irene.window.algui.AlGui.100000015
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
            }
        });
        this.ballLayout.setOnTouchListener(new View.OnTouchListener(this) { // from class: irene.window.algui.AlGui.100000016
            private float downX;
            private float downY;
            private int signX;
            private int signY;
            private final AlGui this$0;
            boolean isOne = true;
            boolean isMove = false;
            int moveThreshold = 20;

            {
                this.this$0 = this;
            }

            /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
            /* JADX WARN: Code restructure failed: missing block: B:21:0x00b5, code lost:
            
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
                        this.signX = this.this$0.ballParams.x;
                        this.signY = this.this$0.ballParams.y;
                        this.downX = motionEvent.getRawX();
                        this.downY = motionEvent.getRawY();
                        break;
                    case 1:
                        if (!this.isMove) {
                            this.this$0.showMenu();
                            this.this$0.clearBall();
                        }
                        if (this.isMove) {
                            this.this$0.ballLayout.setAlpha(1);
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
                                this.this$0.ballLayout.setAlpha(0.3f);
                                this.isOne = false;
                            }
                            this.this$0.ballParams.x = this.signX + ((int) (motionEvent.getRawX() - this.downX));
                            this.this$0.ballParams.y = this.signY + ((int) (motionEvent.getRawY() - this.downY));
                            this.this$0.updateBall();
                            break;
                        }
                        break;
                }
            }
        });
    }

    private void initBallWindow() {
        this.ballManager = (WindowManager) this.aContext.getSystemService("window");
        this.ballParams = new WindowManager.LayoutParams();
        ((ViewGroup.LayoutParams) this.ballParams).width = -2;
        ((ViewGroup.LayoutParams) this.ballParams).height = -2;
        this.ballParams.gravity = 51;
        this.ballParams.format = 1;
        this.ballParams.windowAnimations = android.R.style.Animation.Toast;
        this.ballParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 8;
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW")) {
            this.ballParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        }
    }

    public void showBall() {
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW") && AppPermissionTool.checkOverlayPermission(this.aContext) && !this.isBallView) {
            this.isBallView = true;
            this.ballManager.addView(this.ballLayout, this.ballParams);
        }
    }

    public void updateBall() {
        if (this.isBallView) {
            this.ballManager.updateViewLayout(this.ballLayout, this.ballParams);
        }
    }

    public void clearBall() {
        if (this.ballManager == null || !this.isBallView) {
            return;
        }
        this.isBallView = false;
        this.ballManager.removeView(this.ballLayout);
    }

    private void initMenuWindow() {
        this.menuManager = (WindowManager) this.aContext.getSystemService("window");
        this.menuParams = new WindowManager.LayoutParams();
        ((ViewGroup.LayoutParams) this.menuParams).width = -2;
        ((ViewGroup.LayoutParams) this.menuParams).height = -2;
        this.menuParams.gravity = 51;
        this.menuParams.format = 1;
        this.menuParams.windowAnimations = android.R.style.Animation.Toast;
        this.menuParams.flags = AlGuiData.getLiveStreamFlags() | 16777216 | 32 | 262144;
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW")) {
            this.menuParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        }
    }

    public void showMenu() {
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW") && AppPermissionTool.checkOverlayPermission(this.aContext) && !this.isMenuView) {
            this.isMenuView = true;
            this.menuManager.addView(this.menuLayout, this.menuParams);
        }
    }

    public void updateMenu() {
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.aContext, "android.permission.SYSTEM_ALERT_WINDOW") && this.isMenuView) {
            this.menuManager.updateViewLayout(this.menuLayout, this.menuParams);
        }
    }

    public void clearMenu() {
        if (this.menuManager == null || !this.isMenuView) {
            return;
        }
        this.isMenuView = false;
        this.menuManager.removeView(this.menuLayout);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ResizeViewTouchListener implements View.OnTouchListener {
        private float downX;
        private float downY;
        private int dx;
        private int dy;
        private int initialX;
        private int initialY;
        private int menuLayoutHeight;
        private int menuLayoutWidth;
        private int scrollHeight;
        private int scrollWidth;
        private final AlGui this$0;
        boolean isMove = false;
        int moveThreshold = 10;

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case 0:
                    this.isMove = false;
                    this.downX = motionEvent.getRawX();
                    this.downY = motionEvent.getRawY();
                    this.initialX = this.this$0.menuParams.x;
                    this.initialY = this.this$0.menuParams.y;
                    this.dx = (int) (motionEvent.getRawX() - this.this$0.menuParams.x);
                    this.dy = (int) (motionEvent.getRawY() - this.this$0.menuParams.y);
                    this.menuLayoutWidth = this.this$0.menuLayout.getWidth();
                    this.menuLayoutHeight = this.this$0.menuLayout.getHeight();
                    this.scrollWidth = this.this$0.scroll.getWidth();
                    this.scrollHeight = this.this$0.scroll.getHeight();
                    return true;
                case 1:
                    if (!this.isMove) {
                        return true;
                    }
                    AlGuiData.getMenuAttributeSPED(this.this$0.aContext).putFloat((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表宽度键名"), this.this$0.scroll.getWidth());
                    AlGuiData.getMenuAttributeSPED(this.this$0.aContext).putFloat((String) AlGuiData.getMenuAttributeData().get("菜单滚动列表高度键名"), this.this$0.scroll.getHeight());
                    AlGuiData.getMenuAttributeSPED(this.this$0.aContext).apply();
                    ((ViewGroup.LayoutParams) this.this$0.menuParams).width = -2;
                    ((ViewGroup.LayoutParams) this.this$0.menuParams).height = -2;
                    this.this$0.updateMenu();
                    return true;
                case 2:
                    float abs = Math.abs(motionEvent.getRawX() - this.downX);
                    float abs2 = Math.abs(motionEvent.getRawY() - this.downY);
                    if (abs > this.moveThreshold || abs2 > this.moveThreshold) {
                        this.isMove = true;
                    }
                    if (!this.isMove) {
                        return true;
                    }
                    int rawX = (int) ((motionEvent.getRawX() - this.dx) - this.initialX);
                    int rawY = (int) ((motionEvent.getRawY() - this.dy) - this.initialY);
                    int max = Math.max(350, this.scrollWidth + rawX);
                    int max2 = Math.max(0, this.scrollHeight + rawY);
                    int max3 = Math.max(350, this.menuLayoutWidth + rawX);
                    int max4 = Math.max(0, this.menuLayoutHeight + rawY);
                    if (this.this$0.scroll != null) {
                        this.this$0.scroll.getLayoutParams().width = max;
                        this.this$0.scroll.getLayoutParams().height = max2;
                    }
                    if (this.this$0.menuParams != null) {
                        ((ViewGroup.LayoutParams) this.this$0.menuParams).width = max3;
                        if (this.this$0.scroll.getHeight() > 0) {
                            ((ViewGroup.LayoutParams) this.this$0.menuParams).height = max4;
                        }
                    }
                    this.this$0.updateMenu();
                    return true;
                default:
                    return false;
            }
        }

        public ResizeViewTouchListener(AlGui alGui) {
            this.this$0 = alGui;
        }
    }

    public LinearLayout addCollapse(ViewGroup viewGroup, CharSequence charSequence, int i, int i2, Typeface typeface, float f, int i3, float f2, int i4, boolean z) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackColor(i3);
        vlinearlayout.setFilletRadiu(ViewTool.convertDpToPx(this.aContext, f));
        vlinearlayout.setBorder(ViewTool.convertDpToPx(this.aContext, f2), i4);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setVerticalGravity(16);
        vlinearlayout.setOrientation(1);
        vlinearlayout.setId(AlGuiData.AlguiView.Collapse.getId());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams2);
        linearLayout.setVerticalGravity(16);
        linearLayout.setPadding(20, 20, 20, 20);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(0);
        LinearLayout linearLayout2 = new LinearLayout(this.aContext);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.setBackgroundColor(0);
        TextView textView = new TextView(this.aContext);
        textView.setBackgroundColor(0);
        textView.setTextColor(i2);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.setMargins(i + 5, 0, 0, 0);
        TextView textView2 = new TextView(this.aContext);
        textView2.setLayoutParams(layoutParams3);
        textView2.setBackgroundColor(0);
        textView2.setTextSize(0, ViewTool.convertDpToPx(this.aContext, i));
        if (charSequence != null) {
            textView2.setText(charSequence);
        }
        textView2.setTextColor(i2);
        if (typeface != null) {
            textView2.setTypeface(typeface);
        }
        linearLayout2.addView(textView);
        linearLayout2.addView(textView2);
        if (z) {
            linearLayout.setVisibility(0);
            textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 11));
            textView.setText("▼");
        } else {
            linearLayout.setVisibility(8);
            textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 8));
            textView.setText("▶");
        }
        linearLayout2.setOnClickListener(new View.OnClickListener(this, z, linearLayout, textView) { // from class: irene.window.algui.AlGui.100000017
            boolean isChecked;
            private final AlGui this$0;
            private final LinearLayout val$collapseSub;
            private final boolean val$isUnfold;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$isUnfold = z;
                this.val$collapseSub = linearLayout;
                this.val$textView = textView;
                this.isChecked = this.val$isUnfold;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.isChecked = !this.isChecked;
                if (this.isChecked) {
                    AlGuiSoundEffect.getAudio(this.this$0.aContext).playSoundEffect(AlGuiSoundEffect.COLLAPSEMENU_OPEN);
                    this.val$collapseSub.setVisibility(0);
                    this.val$textView.setTextSize(0, ViewTool.convertDpToPx(this.this$0.aContext, 11));
                    this.val$textView.setText("▼");
                    return;
                }
                AlGuiSoundEffect.getAudio(this.this$0.aContext).playSoundEffect(AlGuiSoundEffect.COLLAPSEMENU_SHUT);
                this.val$collapseSub.setVisibility(8);
                this.val$textView.setTextSize(0, ViewTool.convertDpToPx(this.this$0.aContext, 8));
                this.val$textView.setText("▶");
            }
        });
        vlinearlayout.addView(linearLayout2);
        vlinearlayout.addView(linearLayout);
        if (viewGroup != null) {
            viewGroup.addView(vlinearlayout);
        }
        return linearLayout;
    }

    public vLinearLayout addCollapse(CharSequence charSequence, int i, int i2, Typeface typeface, float f, int i3, float f2, int i4, boolean z, View... viewArr) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackColor(i3);
        vlinearlayout.setFilletRadiu(ViewTool.convertDpToPx(this.aContext, f));
        vlinearlayout.setBorder(ViewTool.convertDpToPx(this.aContext, f2), i4);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setVerticalGravity(16);
        vlinearlayout.setOrientation(1);
        vlinearlayout.setId(AlGuiData.AlguiView.Collapse.getId());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams2);
        linearLayout.setVerticalGravity(16);
        linearLayout.setPadding(20, 20, 20, 20);
        linearLayout.setOrientation(1);
        linearLayout.setBackgroundColor(0);
        LinearLayout linearLayout2 = new LinearLayout(this.aContext);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.setBackgroundColor(0);
        TextView textView = new TextView(this.aContext);
        textView.setBackgroundColor(0);
        textView.setTextColor(i2);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.setMargins(i + 5, 0, 0, 0);
        TextView textView2 = new TextView(this.aContext);
        textView2.setLayoutParams(layoutParams3);
        textView2.setBackgroundColor(0);
        textView2.setTextSize(0, ViewTool.convertDpToPx(this.aContext, i));
        if (charSequence != null) {
            textView2.setText(charSequence);
        }
        textView2.setTextColor(i2);
        if (typeface != null) {
            textView2.setTypeface(typeface);
        }
        linearLayout2.addView(textView);
        linearLayout2.addView(textView2);
        if (z) {
            linearLayout.setVisibility(0);
            textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 11));
            textView.setText("▼");
        } else {
            linearLayout.setVisibility(8);
            textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, 8));
            textView.setText("▶");
        }
        linearLayout2.setOnClickListener(new View.OnClickListener(this, z, linearLayout, textView) { // from class: irene.window.algui.AlGui.100000018
            boolean isChecked;
            private final AlGui this$0;
            private final LinearLayout val$collapseSub;
            private final boolean val$isUnfold;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$isUnfold = z;
                this.val$collapseSub = linearLayout;
                this.val$textView = textView;
                this.isChecked = this.val$isUnfold;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.isChecked = !this.isChecked;
                if (this.isChecked) {
                    AlGuiSoundEffect.getAudio(this.this$0.aContext).playSoundEffect(AlGuiSoundEffect.COLLAPSEMENU_OPEN);
                    this.val$collapseSub.setVisibility(0);
                    this.val$textView.setTextSize(0, ViewTool.convertDpToPx(this.this$0.aContext, 11));
                    this.val$textView.setText("▼");
                    return;
                }
                AlGuiSoundEffect.getAudio(this.this$0.aContext).playSoundEffect(AlGuiSoundEffect.COLLAPSEMENU_SHUT);
                this.val$collapseSub.setVisibility(8);
                this.val$textView.setTextSize(0, ViewTool.convertDpToPx(this.this$0.aContext, 8));
                this.val$textView.setText("▶");
            }
        });
        vlinearlayout.addView(linearLayout2);
        vlinearlayout.addView(linearLayout);
        for (View view : viewArr) {
            if (view != null) {
                view.getId();
                AlGuiData.AlguiView.SmallButton.getId();
                linearLayout.addView(view);
            }
        }
        return vlinearlayout;
    }

    public LinearLayout addLinearLayout(ViewGroup viewGroup, int i, int i2, int i3, int i4) {
        if (i3 != -2 && i3 != -1 && i3 != -1) {
            i3 = ViewTool.convertDpToPx(this.aContext, i3);
        }
        if (i4 != -2 && i4 != -1 && i4 != -1) {
            i4 = ViewTool.convertDpToPx(this.aContext, i4);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i3, i4, 100.0f);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setGravity(i);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(i2);
        if (viewGroup != null) {
            viewGroup.addView(linearLayout);
        }
        return linearLayout;
    }

    public LinearLayout addLinearLayout(int i, int i2, int i3, int i4, View... viewArr) {
        if (i3 != -2 && i3 != -1 && i3 != -1) {
            i3 = ViewTool.convertDpToPx(this.aContext, i3);
        }
        if (i4 != -2 && i4 != -1 && i4 != -1) {
            i4 = ViewTool.convertDpToPx(this.aContext, i4);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i3, i4, 100.0f);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setGravity(i);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(i2);
        for (View view : viewArr) {
            if (view != null) {
                view.getId();
                AlGuiData.AlguiView.SmallButton.getId();
                linearLayout.addView(view);
            }
        }
        return linearLayout;
    }

    public Switch addSwitch(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, CharSequence charSequence2, float f2, int i2, Typeface typeface2, int i3, int i4, int i5, int i6, T_SwitchOnChangeListener t_SwitchOnChangeListener) {
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(1);
        linearLayout.setId(AlGuiData.AlguiView.Switch.getId());
        Switch r5 = new Switch(this.aContext);
        if (charSequence != null) {
            r5.setText(charSequence);
        }
        r5.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        r5.setTextColor(i);
        setSwitchColor(r5, i5, i6);
        if (typeface != null) {
            r5.setTypeface(typeface);
        }
        TextView textView = new TextView(this.aContext);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, -10, 0, 0);
        textView.setLayoutParams(layoutParams2);
        if (charSequence2 != null) {
            textView.setText(charSequence2);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f2));
        textView.setTextColor(i2);
        if (typeface2 != null) {
            textView.setTypeface(typeface2);
        }
        r5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this, r5, i3, i5, i4, i6, t_SwitchOnChangeListener, textView) { // from class: irene.window.algui.AlGui.100000019
            private final AlGui this$0;
            private final Switch val$aSwitch;
            private final TextView val$desc;
            private final T_SwitchOnChangeListener val$fun;
            private final int val$railOffColor;
            private final int val$railOnColor;
            private final int val$roundOffColor;
            private final int val$roundOnColor;

            {
                this.this$0 = this;
                this.val$aSwitch = r5;
                this.val$roundOnColor = i3;
                this.val$roundOffColor = i5;
                this.val$railOnColor = i4;
                this.val$railOffColor = i6;
                this.val$fun = t_SwitchOnChangeListener;
                this.val$desc = textView;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                this.this$0.setSwitchColor(this.val$aSwitch, this.val$aSwitch.isChecked() ? this.val$roundOnColor : this.val$roundOffColor, this.val$aSwitch.isChecked() ? this.val$railOnColor : this.val$railOffColor);
                if (this.val$fun != null) {
                    this.val$fun.onClick(compoundButton, this.val$desc, this.val$aSwitch.isChecked());
                }
            }
        });
        linearLayout.addView(r5);
        linearLayout.addView(textView);
        if (viewGroup != null) {
            viewGroup.addView(linearLayout);
        }
        return r5;
    }

    public LinearLayout addSwitch(CharSequence charSequence, float f, int i, Typeface typeface, CharSequence charSequence2, float f2, int i2, Typeface typeface2, int i3, int i4, int i5, int i6, T_SwitchOnChangeListener t_SwitchOnChangeListener) {
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(1);
        linearLayout.setId(AlGuiData.AlguiView.Switch.getId());
        Switch r5 = new Switch(this.aContext);
        if (charSequence != null) {
            r5.setText(charSequence);
        }
        r5.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        r5.setTextColor(i);
        setSwitchColor(r5, i5, i6);
        if (typeface != null) {
            r5.setTypeface(typeface);
        }
        TextView textView = new TextView(this.aContext);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, -10, 0, 0);
        textView.setLayoutParams(layoutParams2);
        if (charSequence2 != null) {
            textView.setText(charSequence2);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f2));
        textView.setTextColor(i2);
        if (typeface2 != null) {
            textView.setTypeface(typeface2);
        }
        r5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this, r5, i3, i5, i4, i6, t_SwitchOnChangeListener, textView) { // from class: irene.window.algui.AlGui.100000020
            private final AlGui this$0;
            private final Switch val$aSwitch;
            private final TextView val$desc;
            private final T_SwitchOnChangeListener val$fun;
            private final int val$railOffColor;
            private final int val$railOnColor;
            private final int val$roundOffColor;
            private final int val$roundOnColor;

            {
                this.this$0 = this;
                this.val$aSwitch = r5;
                this.val$roundOnColor = i3;
                this.val$roundOffColor = i5;
                this.val$railOnColor = i4;
                this.val$railOffColor = i6;
                this.val$fun = t_SwitchOnChangeListener;
                this.val$desc = textView;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                this.this$0.setSwitchColor(this.val$aSwitch, this.val$aSwitch.isChecked() ? this.val$roundOnColor : this.val$roundOffColor, this.val$aSwitch.isChecked() ? this.val$railOnColor : this.val$railOffColor);
                if (this.val$fun != null) {
                    this.val$fun.onClick(compoundButton, this.val$desc, this.val$aSwitch.isChecked());
                }
            }
        });
        linearLayout.addView(r5);
        linearLayout.addView(textView);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean setSwitchColor(Switch r3, int i, int i2) {
        if (r3 == null || r3.getThumbDrawable() == null || r3.getTrackDrawable() == null) {
            return false;
        }
        r3.getThumbDrawable().setColorFilter(i, PorterDuff.Mode.MULTIPLY);
        r3.getTrackDrawable().setColorFilter(i2, PorterDuff.Mode.MULTIPLY);
        return true;
    }

    public vLinearLayout addSmallButton(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, float f2, int i2, float f3, int i3, T_ButtonOnChangeListener t_ButtonOnChangeListener) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setId(AlGuiData.AlguiView.SmallButton.getId());
        vlinearlayout.setGravity(17);
        TextView textView = new TextView(this.aContext);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        textView.setGravity(17);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        vlinearlayout.setOnClickListener(new AnonymousClass100000022(this, t_ButtonOnChangeListener, gradientDrawable, textView, vlinearlayout));
        vlinearlayout.addView(textView);
        if (viewGroup != null) {
            viewGroup.addView(vlinearlayout);
        }
        return vlinearlayout;
    }

    /* renamed from: irene.window.algui.AlGui$100000022, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000022 implements View.OnClickListener {
        private final AlGui this$0;
        private final GradientDrawable val$back;
        private final vLinearLayout val$button;
        private final TextView val$buttonText;
        private final T_ButtonOnChangeListener val$fun;
        boolean isChecked = true;
        boolean b = true;

        AnonymousClass100000022(AlGui alGui, T_ButtonOnChangeListener t_ButtonOnChangeListener, GradientDrawable gradientDrawable, TextView textView, vLinearLayout vlinearlayout) {
            this.this$0 = alGui;
            this.val$fun = t_ButtonOnChangeListener;
            this.val$back = gradientDrawable;
            this.val$buttonText = textView;
            this.val$button = vlinearlayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.b) {
                this.b = false;
                if (this.val$fun != null) {
                    this.val$fun.onClick(view, this.val$back, this.val$buttonText, this.isChecked);
                    this.isChecked = !this.isChecked;
                }
                int defaultColor = this.val$back.getColor().getDefaultColor();
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
                long j = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
                scaleAnimation.setDuration(j);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(2);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
                alphaAnimation.setDuration(j);
                alphaAnimation.setRepeatCount(1);
                alphaAnimation.setRepeatMode(2);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(alphaAnimation);
                this.val$back.setColor(ViewTool.darkenColor(defaultColor, 0.7f));
                animationSet.setAnimationListener(new Animation.AnimationListener(this, this.val$button, this.val$back, defaultColor) { // from class: irene.window.algui.AlGui.100000022.100000021
                    private final AnonymousClass100000022 this$0;
                    private final GradientDrawable val$back;
                    private final vLinearLayout val$button;
                    private final int val$color;

                    {
                        this.this$0 = this;
                        this.val$button = r2;
                        this.val$back = r3;
                        this.val$color = defaultColor;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        this.val$button.clearAnimation();
                        this.val$back.setColor(this.val$color);
                        this.this$0.b = true;
                    }
                });
                this.val$button.startAnimation(animationSet);
            }
        }
    }

    public vLinearLayout addSmallButton(CharSequence charSequence, float f, int i, Typeface typeface, float f2, int i2, float f3, int i3, T_ButtonOnChangeListener t_ButtonOnChangeListener) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setId(AlGuiData.AlguiView.SmallButton.getId());
        vlinearlayout.setGravity(17);
        TextView textView = new TextView(this.aContext);
        if (charSequence == null) {
            charSequence = "按钮";
        }
        textView.setText(charSequence);
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        textView.setGravity(17);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        vlinearlayout.setOnClickListener(new AnonymousClass100000024(this, t_ButtonOnChangeListener, gradientDrawable, textView, vlinearlayout));
        vlinearlayout.addView(textView);
        return vlinearlayout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGui$100000024, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000024 implements View.OnClickListener {
        private final AlGui this$0;
        private final GradientDrawable val$back;
        private final vLinearLayout val$button;
        private final TextView val$buttonText;
        private final T_ButtonOnChangeListener val$fun;
        boolean isChecked = true;
        boolean b = true;

        AnonymousClass100000024(AlGui alGui, T_ButtonOnChangeListener t_ButtonOnChangeListener, GradientDrawable gradientDrawable, TextView textView, vLinearLayout vlinearlayout) {
            this.this$0 = alGui;
            this.val$fun = t_ButtonOnChangeListener;
            this.val$back = gradientDrawable;
            this.val$buttonText = textView;
            this.val$button = vlinearlayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.b) {
                this.b = false;
                if (this.val$fun != null) {
                    this.val$fun.onClick(view, this.val$back, this.val$buttonText, this.isChecked);
                    this.isChecked = !this.isChecked;
                }
                int defaultColor = this.val$back.getColor().getDefaultColor();
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
                long j = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
                scaleAnimation.setDuration(j);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(2);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
                alphaAnimation.setDuration(j);
                alphaAnimation.setRepeatCount(1);
                alphaAnimation.setRepeatMode(2);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(alphaAnimation);
                this.val$back.setColor(ViewTool.darkenColor(defaultColor, 0.7f));
                animationSet.setAnimationListener(new Animation.AnimationListener(this, this.val$button, this.val$back, defaultColor) { // from class: irene.window.algui.AlGui.100000024.100000023
                    private final AnonymousClass100000024 this$0;
                    private final GradientDrawable val$back;
                    private final vLinearLayout val$button;
                    private final int val$color;

                    {
                        this.this$0 = this;
                        this.val$button = r2;
                        this.val$back = r3;
                        this.val$color = defaultColor;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        this.val$button.clearAnimation();
                        this.val$back.setColor(this.val$color);
                        this.this$0.b = true;
                    }
                });
                this.val$button.startAnimation(animationSet);
            }
        }
    }

    public vLinearLayout addButton(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, float f2, int i2, float f3, int i3, int i4, int i5, T_ButtonOnChangeListener t_ButtonOnChangeListener) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i3);
        if (i4 != -2 && i4 != -1 && i4 != -1) {
            i4 = ViewTool.convertDpToPx(this.aContext, i4);
        }
        if (i5 != -2 && i5 != -1 && i5 != -1) {
            i5 = ViewTool.convertDpToPx(this.aContext, i5);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i4, i5, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setId(AlGuiData.AlguiView.Button.getId());
        vlinearlayout.setGravity(17);
        TextView textView = new TextView(this.aContext);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        textView.setGravity(17);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        vlinearlayout.setOnClickListener(new AnonymousClass100000026(this, t_ButtonOnChangeListener, gradientDrawable, textView, vlinearlayout));
        vlinearlayout.addView(textView);
        if (viewGroup != null) {
            viewGroup.addView(vlinearlayout);
        }
        return vlinearlayout;
    }

    /* renamed from: irene.window.algui.AlGui$100000026, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000026 implements View.OnClickListener {
        private final AlGui this$0;
        private final GradientDrawable val$back;
        private final vLinearLayout val$button;
        private final TextView val$buttonText;
        private final T_ButtonOnChangeListener val$fun;
        boolean isChecked = true;
        boolean b = true;

        AnonymousClass100000026(AlGui alGui, T_ButtonOnChangeListener t_ButtonOnChangeListener, GradientDrawable gradientDrawable, TextView textView, vLinearLayout vlinearlayout) {
            this.this$0 = alGui;
            this.val$fun = t_ButtonOnChangeListener;
            this.val$back = gradientDrawable;
            this.val$buttonText = textView;
            this.val$button = vlinearlayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.b) {
                this.b = false;
                if (this.val$fun != null) {
                    this.val$fun.onClick(view, this.val$back, this.val$buttonText, this.isChecked);
                    this.isChecked = !this.isChecked;
                }
                int defaultColor = this.val$back.getColor().getDefaultColor();
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
                long j = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
                scaleAnimation.setDuration(j);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(2);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
                alphaAnimation.setDuration(j);
                alphaAnimation.setRepeatCount(1);
                alphaAnimation.setRepeatMode(2);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(alphaAnimation);
                this.val$back.setColor(ViewTool.darkenColor(defaultColor, 0.7f));
                animationSet.setAnimationListener(new Animation.AnimationListener(this, this.val$button, this.val$back, defaultColor) { // from class: irene.window.algui.AlGui.100000026.100000025
                    private final AnonymousClass100000026 this$0;
                    private final GradientDrawable val$back;
                    private final vLinearLayout val$button;
                    private final int val$color;

                    {
                        this.this$0 = this;
                        this.val$button = r2;
                        this.val$back = r3;
                        this.val$color = defaultColor;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        this.val$button.clearAnimation();
                        this.val$back.setColor(this.val$color);
                        this.this$0.b = true;
                    }
                });
                this.val$button.startAnimation(animationSet);
            }
        }
    }

    public vLinearLayout addButton(CharSequence charSequence, float f, int i, Typeface typeface, float f2, int i2, float f3, int i3, int i4, int i5, T_ButtonOnChangeListener t_ButtonOnChangeListener) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i3);
        if (i4 != -2 && i4 != -1 && i4 != -1) {
            i4 = ViewTool.convertDpToPx(this.aContext, i4);
        }
        if (i5 != -2 && i5 != -1 && i5 != -1) {
            i5 = ViewTool.convertDpToPx(this.aContext, i5);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i4, i5, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        vLinearLayout vlinearlayout = new vLinearLayout(this.aContext);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(10, 10, 10, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setId(AlGuiData.AlguiView.Button.getId());
        vlinearlayout.setGravity(17);
        TextView textView = new TextView(this.aContext);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        textView.setGravity(17);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        vlinearlayout.setOnClickListener(new AnonymousClass100000028(this, t_ButtonOnChangeListener, gradientDrawable, textView, vlinearlayout));
        vlinearlayout.addView(textView);
        return vlinearlayout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGui$100000028, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000028 implements View.OnClickListener {
        private final AlGui this$0;
        private final GradientDrawable val$back;
        private final vLinearLayout val$button;
        private final TextView val$buttonText;
        private final T_ButtonOnChangeListener val$fun;
        boolean isChecked = true;
        boolean b = true;

        AnonymousClass100000028(AlGui alGui, T_ButtonOnChangeListener t_ButtonOnChangeListener, GradientDrawable gradientDrawable, TextView textView, vLinearLayout vlinearlayout) {
            this.this$0 = alGui;
            this.val$fun = t_ButtonOnChangeListener;
            this.val$back = gradientDrawable;
            this.val$buttonText = textView;
            this.val$button = vlinearlayout;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.b) {
                this.b = false;
                if (this.val$fun != null) {
                    this.val$fun.onClick(view, this.val$back, this.val$buttonText, this.isChecked);
                    this.isChecked = !this.isChecked;
                }
                int defaultColor = this.val$back.getColor().getDefaultColor();
                ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 1, 0.5f, 1, 0.5f);
                long j = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
                scaleAnimation.setDuration(j);
                scaleAnimation.setRepeatCount(1);
                scaleAnimation.setRepeatMode(2);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
                alphaAnimation.setDuration(j);
                alphaAnimation.setRepeatCount(1);
                alphaAnimation.setRepeatMode(2);
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(alphaAnimation);
                this.val$back.setColor(ViewTool.darkenColor(defaultColor, 0.7f));
                animationSet.setAnimationListener(new Animation.AnimationListener(this, this.val$button, this.val$back, defaultColor) { // from class: irene.window.algui.AlGui.100000028.100000027
                    private final AnonymousClass100000028 this$0;
                    private final GradientDrawable val$back;
                    private final vLinearLayout val$button;
                    private final int val$color;

                    {
                        this.this$0 = this;
                        this.val$button = r2;
                        this.val$back = r3;
                        this.val$color = defaultColor;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        this.val$button.clearAnimation();
                        this.val$back.setColor(this.val$color);
                        this.this$0.b = true;
                    }
                });
                this.val$button.startAnimation(animationSet);
            }
        }
    }

    public EditText addEditText(ViewGroup viewGroup, float f, Typeface typeface, int i, CharSequence charSequence, int i2, CharSequence charSequence2, float f2, int i3, float f3, int i4, T_EditTextOnChangeListener t_EditTextOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setColor(i3);
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i4);
        EditText editText = new EditText(this.aContext);
        editText.setTextIsSelectable(true);
        editText.setInputType(131072);
        editText.setLayoutParams(layoutParams);
        if (charSequence != null) {
            editText.setHint(charSequence);
        }
        editText.setHintTextColor(i);
        if (charSequence2 != null) {
            editText.setText(charSequence2);
        }
        editText.setTextColor(i2);
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        editText.setBackground(gradientDrawable);
        if (typeface != null) {
            editText.setTypeface(typeface);
        }
        editText.setId(AlGuiData.AlguiView.EditText.getId());
        editText.setOnLongClickListener(new AnonymousClass100000030(this, editText));
        editText.addTextChangedListener(new TextWatcher(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000031
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence3, int i5, int i6, int i7) {
                if (this.val$fun != null) {
                    this.val$fun.beforeTextChanged(this.val$editext, charSequence3, i5, i6, i7);
                }
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence3, int i5, int i6, int i7) {
                if (this.val$fun != null) {
                    this.val$fun.onTextChanged(this.val$editext, charSequence3, i5, i6, i7);
                }
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                if (this.val$fun != null) {
                    this.val$fun.afterTextChanged(this.val$editext, editable);
                }
            }
        });
        if (viewGroup != null) {
            viewGroup.addView(editText);
        }
        return editText;
    }

    /* renamed from: irene.window.algui.AlGui$100000030, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000030 implements View.OnLongClickListener {
        PopupMenu popupMenu;
        private final AlGui this$0;
        private final EditText val$editext;

        AnonymousClass100000030(AlGui alGui, EditText editText) {
            this.this$0 = alGui;
            this.val$editext = editText;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.popupMenu == null) {
                this.popupMenu = new PopupMenu(this.this$0.aContext, view);
                Menu menu = this.popupMenu.getMenu();
                menu.add(0, 95, 0, "全选");
                menu.add(0, 96, 0, "复制");
                menu.add(0, 97, 0, "粘贴");
                menu.add(0, 98, 0, "剪切");
                this.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(this, this.val$editext) { // from class: irene.window.algui.AlGui.100000030.100000029
                    ClipboardManager clipboardManager;
                    private final AnonymousClass100000030 this$0;
                    private final EditText val$editext;

                    {
                        this.this$0 = this;
                        this.val$editext = r2;
                        this.clipboardManager = (ClipboardManager) this.this$0.this$0.aContext.getSystemService("clipboard");
                    }

                    @Override // android.widget.PopupMenu.OnMenuItemClickListener
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ClipData newPlainText = ClipData.newPlainText("text", this.val$editext.getText());
                        switch (menuItem.getItemId()) {
                            case 95:
                                this.val$editext.selectAll();
                                return true;
                            case 96:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                return true;
                            case 97:
                                if (this.clipboardManager.hasPrimaryClip() && this.clipboardManager.getPrimaryClipDescription().hasMimeType("text/plain")) {
                                    this.val$editext.setText("");
                                    this.val$editext.getText().insert(this.val$editext.getSelectionStart(), this.clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                                }
                                return true;
                            case 98:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                this.val$editext.setText("");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
            this.popupMenu.show();
            return false;
        }
    }

    public EditText addEditText(float f, Typeface typeface, int i, CharSequence charSequence, int i2, CharSequence charSequence2, float f2, int i3, float f3, int i4, T_EditTextOnChangeListener t_EditTextOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setColor(i3);
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i4);
        EditText editText = new EditText(this.aContext);
        editText.setTextIsSelectable(true);
        editText.setInputType(131072);
        editText.setLayoutParams(layoutParams);
        if (charSequence != null) {
            editText.setHint(charSequence);
        }
        editText.setHintTextColor(i);
        if (charSequence2 != null) {
            editText.setText(charSequence2);
        }
        editText.setTextColor(i2);
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        editText.setBackground(gradientDrawable);
        if (typeface != null) {
            editText.setTypeface(typeface);
        }
        editText.setId(AlGuiData.AlguiView.EditText.getId());
        editText.setOnLongClickListener(new AnonymousClass100000033(this, editText));
        editText.addTextChangedListener(new TextWatcher(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000034
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence3, int i5, int i6, int i7) {
                if (this.val$fun != null) {
                    this.val$fun.beforeTextChanged(this.val$editext, charSequence3, i5, i6, i7);
                }
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence3, int i5, int i6, int i7) {
                if (this.val$fun != null) {
                    this.val$fun.onTextChanged(this.val$editext, charSequence3, i5, i6, i7);
                }
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                if (this.val$fun != null) {
                    this.val$fun.afterTextChanged(this.val$editext, editable);
                }
            }
        });
        return editText;
    }

    /* renamed from: irene.window.algui.AlGui$100000033, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000033 implements View.OnLongClickListener {
        PopupMenu popupMenu;
        private final AlGui this$0;
        private final EditText val$editext;

        AnonymousClass100000033(AlGui alGui, EditText editText) {
            this.this$0 = alGui;
            this.val$editext = editText;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.popupMenu == null) {
                this.popupMenu = new PopupMenu(this.this$0.aContext, view);
                Menu menu = this.popupMenu.getMenu();
                menu.add(0, 95, 0, "全选");
                menu.add(0, 96, 0, "复制");
                menu.add(0, 97, 0, "粘贴");
                menu.add(0, 98, 0, "剪切");
                this.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(this, this.val$editext) { // from class: irene.window.algui.AlGui.100000033.100000032
                    ClipboardManager clipboardManager;
                    private final AnonymousClass100000033 this$0;
                    private final EditText val$editext;

                    {
                        this.this$0 = this;
                        this.val$editext = r2;
                        this.clipboardManager = (ClipboardManager) this.this$0.this$0.aContext.getSystemService("clipboard");
                    }

                    @Override // android.widget.PopupMenu.OnMenuItemClickListener
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ClipData newPlainText = ClipData.newPlainText("text", this.val$editext.getText());
                        switch (menuItem.getItemId()) {
                            case 95:
                                this.val$editext.selectAll();
                                return true;
                            case 96:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                return true;
                            case 97:
                                if (this.clipboardManager.hasPrimaryClip() && this.clipboardManager.getPrimaryClipDescription().hasMimeType("text/plain")) {
                                    this.val$editext.setText("");
                                    this.val$editext.getText().insert(this.val$editext.getSelectionStart(), this.clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                                }
                                return true;
                            case 98:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                this.val$editext.setText("");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
            this.popupMenu.show();
            return false;
        }
    }

    public EditText addEditText(ViewGroup viewGroup, float f, Typeface typeface, int i, CharSequence charSequence, int i2, CharSequence charSequence2, float f2, int i3, float f3, int i4, int i5, CharSequence charSequence3, float f4, int i6, float f5, int i7, T_EditTextOnChangeListener t_EditTextOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.EditText.getId());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2, 1.0f);
        layoutParams2.setMargins(0, 0, 8, 0);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setColor(i3);
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i4);
        EditText editText = new EditText(this.aContext);
        editText.setTextIsSelectable(true);
        editText.setInputType(131072);
        editText.setLayoutParams(layoutParams2);
        if (charSequence != null) {
            editText.setHint(charSequence);
        }
        editText.setHintTextColor(i);
        if (charSequence2 != null) {
            editText.setText(charSequence2);
        }
        editText.setTextColor(i2);
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        editText.setBackground(gradientDrawable);
        if (typeface != null) {
            editText.setTypeface(typeface);
        }
        editText.setOnLongClickListener(new AnonymousClass100000036(this, editText));
        editText.addTextChangedListener(new TextWatcher(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000037
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence4, int i8, int i9, int i10) {
                if (this.val$fun != null) {
                    this.val$fun.beforeTextChanged(this.val$editext, charSequence4, i8, i9, i10);
                }
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence4, int i8, int i9, int i10) {
                if (this.val$fun != null) {
                    this.val$fun.onTextChanged(this.val$editext, charSequence4, i8, i9, i10);
                }
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                if (this.val$fun != null) {
                    this.val$fun.afterTextChanged(this.val$editext, editable);
                }
            }
        });
        vLinearLayout addSmallButton = addSmallButton(charSequence3, f, i5, typeface, f4, i6, f5, i7, new T_ButtonOnChangeListener(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000038
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable2, TextView textView, boolean z) {
                if (this.val$fun != null) {
                    this.val$fun.buttonOnClick(this.val$editext, view, textView, z);
                }
            }
        });
        addSmallButton.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        linearLayout.addView(editText);
        linearLayout.addView(addSmallButton);
        if (viewGroup != null) {
            viewGroup.addView(linearLayout);
        }
        return editText;
    }

    /* renamed from: irene.window.algui.AlGui$100000036, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000036 implements View.OnLongClickListener {
        PopupMenu popupMenu;
        private final AlGui this$0;
        private final EditText val$editext;

        AnonymousClass100000036(AlGui alGui, EditText editText) {
            this.this$0 = alGui;
            this.val$editext = editText;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.popupMenu == null) {
                this.popupMenu = new PopupMenu(this.this$0.aContext, view);
                Menu menu = this.popupMenu.getMenu();
                menu.add(0, 95, 0, "全选");
                menu.add(0, 96, 0, "复制");
                menu.add(0, 97, 0, "粘贴");
                menu.add(0, 98, 0, "剪切");
                this.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(this, this.val$editext) { // from class: irene.window.algui.AlGui.100000036.100000035
                    ClipboardManager clipboardManager;
                    private final AnonymousClass100000036 this$0;
                    private final EditText val$editext;

                    {
                        this.this$0 = this;
                        this.val$editext = r2;
                        this.clipboardManager = (ClipboardManager) this.this$0.this$0.aContext.getSystemService("clipboard");
                    }

                    @Override // android.widget.PopupMenu.OnMenuItemClickListener
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ClipData newPlainText = ClipData.newPlainText("text", this.val$editext.getText());
                        switch (menuItem.getItemId()) {
                            case 95:
                                this.val$editext.selectAll();
                                return true;
                            case 96:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                return true;
                            case 97:
                                if (this.clipboardManager.hasPrimaryClip() && this.clipboardManager.getPrimaryClipDescription().hasMimeType("text/plain")) {
                                    this.val$editext.setText("");
                                    this.val$editext.getText().insert(this.val$editext.getSelectionStart(), this.clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                                }
                                return true;
                            case 98:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                this.val$editext.setText("");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
            this.popupMenu.show();
            return false;
        }
    }

    public LinearLayout addEditText(float f, Typeface typeface, int i, CharSequence charSequence, int i2, CharSequence charSequence2, float f2, int i3, float f3, int i4, int i5, CharSequence charSequence3, float f4, int i6, float f5, int i7, T_EditTextOnChangeListener t_EditTextOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.EditText.getId());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2, 1.0f);
        layoutParams2.setMargins(0, 0, 8, 0);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.aContext, f2));
        gradientDrawable.setColor(i3);
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.aContext, f3), i4);
        EditText editText = new EditText(this.aContext);
        editText.setTextIsSelectable(true);
        editText.setInputType(131072);
        editText.setLayoutParams(layoutParams2);
        if (charSequence != null) {
            editText.setHint(charSequence);
        }
        editText.setHintTextColor(i);
        if (charSequence2 != null) {
            editText.setText(charSequence2);
        }
        editText.setTextColor(i2);
        editText.setPadding(20, 10, 20, 10);
        editText.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        editText.setBackground(gradientDrawable);
        editText.setTypeface(typeface);
        editText.setOnLongClickListener(new AnonymousClass100000040(this, editText));
        editText.addTextChangedListener(new TextWatcher(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000041
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence4, int i8, int i9, int i10) {
                if (this.val$fun != null) {
                    this.val$fun.beforeTextChanged(this.val$editext, charSequence4, i8, i9, i10);
                }
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence4, int i8, int i9, int i10) {
                if (this.val$fun != null) {
                    this.val$fun.onTextChanged(this.val$editext, charSequence4, i8, i9, i10);
                }
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                if (this.val$fun != null) {
                    this.val$fun.afterTextChanged(this.val$editext, editable);
                }
            }
        });
        vLinearLayout addSmallButton = addSmallButton(charSequence3, f, i5, typeface, f4, i6, f5, i7, new T_ButtonOnChangeListener(this, t_EditTextOnChangeListener, editText) { // from class: irene.window.algui.AlGui.100000042
            private final AlGui this$0;
            private final EditText val$editext;
            private final T_EditTextOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_EditTextOnChangeListener;
                this.val$editext = editText;
            }

            @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
            public void onClick(View view, GradientDrawable gradientDrawable2, TextView textView, boolean z) {
                if (this.val$fun != null) {
                    this.val$fun.buttonOnClick(this.val$editext, view, textView, z);
                }
            }
        });
        addSmallButton.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        linearLayout.addView(editText);
        linearLayout.addView(addSmallButton);
        return linearLayout;
    }

    /* renamed from: irene.window.algui.AlGui$100000040, reason: invalid class name */
    /* loaded from: classes.dex */
    class AnonymousClass100000040 implements View.OnLongClickListener {
        PopupMenu popupMenu;
        private final AlGui this$0;
        private final EditText val$editext;

        AnonymousClass100000040(AlGui alGui, EditText editText) {
            this.this$0 = alGui;
            this.val$editext = editText;
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.popupMenu == null) {
                this.popupMenu = new PopupMenu(this.this$0.aContext, view);
                Menu menu = this.popupMenu.getMenu();
                menu.add(0, 95, 0, "全选");
                menu.add(0, 96, 0, "复制");
                menu.add(0, 97, 0, "粘贴");
                menu.add(0, 98, 0, "剪切");
                this.popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(this, this.val$editext) { // from class: irene.window.algui.AlGui.100000040.100000039
                    ClipboardManager clipboardManager;
                    private final AnonymousClass100000040 this$0;
                    private final EditText val$editext;

                    {
                        this.this$0 = this;
                        this.val$editext = r2;
                        this.clipboardManager = (ClipboardManager) this.this$0.this$0.aContext.getSystemService("clipboard");
                    }

                    @Override // android.widget.PopupMenu.OnMenuItemClickListener
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ClipData newPlainText = ClipData.newPlainText("text", this.val$editext.getText());
                        switch (menuItem.getItemId()) {
                            case 95:
                                this.val$editext.selectAll();
                                return true;
                            case 96:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                return true;
                            case 97:
                                if (this.clipboardManager.hasPrimaryClip() && this.clipboardManager.getPrimaryClipDescription().hasMimeType("text/plain")) {
                                    this.val$editext.setText("");
                                    this.val$editext.getText().insert(this.val$editext.getSelectionStart(), this.clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                                }
                                return true;
                            case 98:
                                this.clipboardManager.setPrimaryClip(newPlainText);
                                this.val$editext.setText("");
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
            this.popupMenu.show();
            return false;
        }
    }

    public SeekBar addSeekBarFloat(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, float f2, float f3, float f4, int i2, int i3, int i4, T_SeekBarFloatOnChangeListener t_SeekBarFloatOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.SeekBarFloat.getId());
        SeekBar seekBar = new SeekBar(this.aContext);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        float f5 = 10;
        float f6 = f2 * f5;
        float f7 = f3 * f5;
        float f8 = f5 * f4;
        int i5 = (int) f8;
        seekBar.setMax(i5);
        int i6 = (int) f6;
        seekBar.setMin(i6);
        if (f7 < f6) {
            seekBar.setProgress(i6);
        } else if (f7 > f8) {
            seekBar.setProgress(i5);
        } else {
            seekBar.setProgress((int) f7);
        }
        seekBar.setProgress((int) f7);
        seekBar.setThumbTintList(ColorStateList.valueOf(i2));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(i3));
        seekBar.setProgressTintList(ColorStateList.valueOf(i4));
        TextView textView = new TextView(this.aContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (charSequence != null) {
            textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) charSequence).toString()).append("：").toString()).append(seekBar.getProgress() / 10.0f).toString());
        }
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, charSequence, textView, t_SeekBarFloatOnChangeListener) { // from class: irene.window.algui.AlGui.100000043
            private final AlGui this$0;
            private final T_SeekBarFloatOnChangeListener val$fun;
            private final CharSequence val$text;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$text = charSequence;
                this.val$textView = textView;
                this.val$fun = t_SeekBarFloatOnChangeListener;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i7, boolean z) {
                float f9 = i7 / 10.0f;
                if (this.val$text != null) {
                    this.val$textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) this.val$text).toString()).append("：").toString()).append(f9).toString());
                }
                if (this.val$fun != null) {
                    this.val$fun.onProgressChanged(this.val$textView, seekBar2, f9, z);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStartTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress() / 10.0f);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStopTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress() / 10.0f);
                }
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        if (viewGroup != null) {
            viewGroup.addView(linearLayout);
        }
        return seekBar;
    }

    public LinearLayout addSeekBarFloat(CharSequence charSequence, float f, int i, Typeface typeface, float f2, float f3, float f4, int i2, int i3, int i4, T_SeekBarFloatOnChangeListener t_SeekBarFloatOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.SeekBarFloat.getId());
        SeekBar seekBar = new SeekBar(this.aContext);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        float f5 = 10;
        float f6 = f2 * f5;
        float f7 = f3 * f5;
        float f8 = f5 * f4;
        int i5 = (int) f8;
        seekBar.setMax(i5);
        int i6 = (int) f6;
        seekBar.setMin(i6);
        if (f7 < f6) {
            seekBar.setProgress(i6);
        } else if (f7 > f8) {
            seekBar.setProgress(i5);
        } else {
            seekBar.setProgress((int) f7);
        }
        seekBar.setProgress((int) f7);
        seekBar.setThumbTintList(ColorStateList.valueOf(i2));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(i3));
        seekBar.setProgressTintList(ColorStateList.valueOf(i4));
        TextView textView = new TextView(this.aContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (charSequence != null) {
            textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) charSequence).toString()).append("：").toString()).append(seekBar.getProgress() / 10.0f).toString());
        }
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, charSequence, textView, t_SeekBarFloatOnChangeListener) { // from class: irene.window.algui.AlGui.100000044
            private final AlGui this$0;
            private final T_SeekBarFloatOnChangeListener val$fun;
            private final CharSequence val$text;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$text = charSequence;
                this.val$textView = textView;
                this.val$fun = t_SeekBarFloatOnChangeListener;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i7, boolean z) {
                float f9 = i7 / 10.0f;
                if (this.val$text != null) {
                    this.val$textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) this.val$text).toString()).append("：").toString()).append(f9).toString());
                }
                if (this.val$fun != null) {
                    this.val$fun.onProgressChanged(this.val$textView, seekBar2, f9, z);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStartTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress() / 10.0f);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStopTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress() / 10.0f);
                }
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        return linearLayout;
    }

    public SeekBar addSeekBarInt(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, int i2, int i3, int i4, int i5, int i6, int i7, T_SeekBarIntOnChangeListener t_SeekBarIntOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.SeekBarInt.getId());
        SeekBar seekBar = new SeekBar(this.aContext);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        seekBar.setMax(i4);
        seekBar.setMin(i2);
        if (i3 < i2) {
            seekBar.setProgress(i2);
        } else if (i3 > i4) {
            seekBar.setProgress(i4);
        } else {
            seekBar.setProgress(i3);
        }
        seekBar.setThumbTintList(ColorStateList.valueOf(i5));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(i6));
        seekBar.setProgressTintList(ColorStateList.valueOf(i7));
        TextView textView = new TextView(this.aContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (charSequence != null) {
            textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) charSequence).toString()).append("：").toString()).append(seekBar.getProgress()).toString());
        }
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, charSequence, textView, t_SeekBarIntOnChangeListener) { // from class: irene.window.algui.AlGui.100000045
            private final AlGui this$0;
            private final T_SeekBarIntOnChangeListener val$fun;
            private final CharSequence val$text;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$text = charSequence;
                this.val$textView = textView;
                this.val$fun = t_SeekBarIntOnChangeListener;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i8, boolean z) {
                if (this.val$text != null) {
                    this.val$textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) this.val$text).toString()).append("：").toString()).append(i8).toString());
                }
                if (this.val$fun != null) {
                    this.val$fun.onProgressChanged(this.val$textView, seekBar2, i8, z);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStartTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress());
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStopTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress());
                }
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        if (viewGroup != null) {
            viewGroup.addView(linearLayout);
        }
        return seekBar;
    }

    public LinearLayout addSeekBarInt(CharSequence charSequence, float f, int i, Typeface typeface, int i2, int i3, int i4, int i5, int i6, int i7, T_SeekBarIntOnChangeListener t_SeekBarIntOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        LinearLayout linearLayout = new LinearLayout(this.aContext);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setClipChildren(true);
        linearLayout.setOrientation(0);
        linearLayout.setId(AlGuiData.AlguiView.SeekBarInt.getId());
        SeekBar seekBar = new SeekBar(this.aContext);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        seekBar.setMax(i4);
        seekBar.setMin(i2);
        if (i3 < i2) {
            seekBar.setProgress(i2);
        } else if (i3 > i4) {
            seekBar.setProgress(i4);
        } else {
            seekBar.setProgress(i3);
        }
        seekBar.setThumbTintList(ColorStateList.valueOf(i5));
        seekBar.setProgressBackgroundTintList(ColorStateList.valueOf(i6));
        seekBar.setProgressTintList(ColorStateList.valueOf(i7));
        TextView textView = new TextView(this.aContext);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1.0f));
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (charSequence != null) {
            textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) charSequence).toString()).append("：").toString()).append(seekBar.getProgress()).toString());
        }
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(this, charSequence, textView, t_SeekBarIntOnChangeListener) { // from class: irene.window.algui.AlGui.100000046
            private final AlGui this$0;
            private final T_SeekBarIntOnChangeListener val$fun;
            private final CharSequence val$text;
            private final TextView val$textView;

            {
                this.this$0 = this;
                this.val$text = charSequence;
                this.val$textView = textView;
                this.val$fun = t_SeekBarIntOnChangeListener;
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i8, boolean z) {
                if (this.val$text != null) {
                    this.val$textView.setText(new StringBuffer().append(new StringBuffer().append(new StringBuffer().append("•").append((Object) this.val$text).toString()).append("：").toString()).append(i8).toString());
                }
                if (this.val$fun != null) {
                    this.val$fun.onProgressChanged(this.val$textView, seekBar2, i8, z);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStartTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress());
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                if (this.val$fun != null) {
                    this.val$fun.onStopTrackingTouch(this.val$textView, seekBar2, seekBar2.getProgress());
                }
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        return linearLayout;
    }

    public CheckBox addCheckBox(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface, int i2, T_CheckBoxOnChangeListener t_CheckBoxOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        CheckBox checkBox = new CheckBox(this.aContext);
        checkBox.setLayoutParams(layoutParams);
        checkBox.setId(AlGuiData.AlguiView.CheckBox.getId());
        if (typeface != null) {
            checkBox.setTypeface(typeface);
        }
        if (charSequence != null) {
            checkBox.setText(charSequence);
        }
        checkBox.setTextColor(i);
        checkBox.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        if (i2 != 0) {
            checkBox.setButtonTintList(ColorStateList.valueOf(i2));
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this, t_CheckBoxOnChangeListener) { // from class: irene.window.algui.AlGui.100000047
            private final AlGui this$0;
            private final T_CheckBoxOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_CheckBoxOnChangeListener;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (this.val$fun != null) {
                    this.val$fun.onClick(compoundButton, z);
                }
            }
        });
        if (viewGroup != null) {
            viewGroup.addView(checkBox);
        }
        return checkBox;
    }

    public CheckBox addCheckBox(CharSequence charSequence, float f, int i, Typeface typeface, int i2, T_CheckBoxOnChangeListener t_CheckBoxOnChangeListener) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        CheckBox checkBox = new CheckBox(this.aContext);
        checkBox.setLayoutParams(layoutParams);
        checkBox.setId(AlGuiData.AlguiView.CheckBox.getId());
        if (typeface != null) {
            checkBox.setTypeface(typeface);
        }
        if (charSequence != null) {
            checkBox.setText(charSequence);
        }
        checkBox.setTextColor(i);
        checkBox.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        if (i2 != 0) {
            checkBox.setButtonTintList(ColorStateList.valueOf(i2));
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this, t_CheckBoxOnChangeListener) { // from class: irene.window.algui.AlGui.100000048
            private final AlGui this$0;
            private final T_CheckBoxOnChangeListener val$fun;

            {
                this.this$0 = this;
                this.val$fun = t_CheckBoxOnChangeListener;
            }

            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (this.val$fun != null) {
                    this.val$fun.onClick(compoundButton, z);
                }
            }
        });
        return checkBox;
    }

    public WebView addWebView(ViewGroup viewGroup, String str) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        WebView webView = new WebView(this.aContext);
        webView.setId(AlGuiData.AlguiView.WebView.getId());
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(0);
        webView.getSettings().setAppCacheEnabled(false);
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
        webView.setWebViewClient(new WebViewClient(this) { // from class: irene.window.algui.AlGui.100000049
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, int i, String str2, String str3) {
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, String str2) {
                super.shouldOverrideUrlLoading(webView2, str2);
                webView2.loadUrl(str2);
                return true;
            }
        });
        if (str != null) {
            webView.loadData(str, "text/html", "utf-8");
            if (viewGroup != null) {
                viewGroup.addView(webView);
            }
        }
        return webView;
    }

    public WebView addWebView(String str) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        WebView webView = new WebView(this.aContext);
        webView.setId(AlGuiData.AlguiView.WebView.getId());
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(0);
        webView.getSettings().setAppCacheEnabled(false);
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
        webView.setWebViewClient(new WebViewClient(this) { // from class: irene.window.algui.AlGui.100000050
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, int i, String str2, String str3) {
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, String str2) {
                super.shouldOverrideUrlLoading(webView2, str2);
                webView2.loadUrl(str2);
                return true;
            }
        });
        if (str != null) {
            webView.loadData(str, "text/html", "utf-8");
        }
        return webView;
    }

    public WebView addWebView(ViewGroup viewGroup, String str, float f, float f2, int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewTool.convertDpToPx(this.aContext, f), ViewTool.convertDpToPx(this.aContext, f2), 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        WebView webView = new WebView(this.aContext);
        webView.setId(AlGuiData.AlguiView.WebView.getId());
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(i);
        webView.getSettings().setAppCacheEnabled(false);
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
        webView.setWebViewClient(new WebViewClient(this) { // from class: irene.window.algui.AlGui.100000051
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, int i2, String str2, String str3) {
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, String str2) {
                super.shouldOverrideUrlLoading(webView2, str2);
                webView2.loadUrl(str2);
                return true;
            }
        });
        if (str != null) {
            webView.loadData(str, "text/html", "utf-8");
            if (viewGroup != null) {
                viewGroup.addView(webView);
            }
        }
        return webView;
    }

    public WebView addWebView(String str, float f, float f2, int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewTool.convertDpToPx(this.aContext, f), ViewTool.convertDpToPx(this.aContext, f2), 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        WebView webView = new WebView(this.aContext);
        webView.setId(AlGuiData.AlguiView.WebView.getId());
        webView.setLayoutParams(layoutParams);
        webView.setBackgroundColor(i);
        webView.getSettings().setAppCacheEnabled(false);
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
        webView.setWebViewClient(new WebViewClient(this) { // from class: irene.window.algui.AlGui.100000052
            private final AlGui this$0;

            {
                this.this$0 = this;
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView2, int i2, String str2, String str3) {
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView2, String str2) {
                super.shouldOverrideUrlLoading(webView2, str2);
                webView2.loadUrl(str2);
                return true;
            }
        });
        if (str != null) {
            webView.loadData(str, "text/html", "utf-8");
        }
        return webView;
    }

    public WebView addWebSite(ViewGroup viewGroup, String str) {
        WebView addWebView = addWebView(null);
        addWebView.setId(AlGuiData.AlguiView.WebSite.getId());
        if (str != null) {
            addWebView.loadUrl(str);
            if (viewGroup != null) {
                viewGroup.addView(addWebView);
            }
        }
        return addWebView;
    }

    public WebView addWebSite(String str) {
        WebView addWebView = addWebView(null);
        addWebView.setId(AlGuiData.AlguiView.WebSite.getId());
        if (str != null) {
            addWebView.loadUrl(str);
        }
        return addWebView;
    }

    public TextView addTextView(ViewGroup viewGroup, CharSequence charSequence, float f, int i, Typeface typeface) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        TextView textView = new TextView(this.aContext);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setLayoutParams(layoutParams);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        textView.setId(AlGuiData.AlguiView.TextView.getId());
        if (viewGroup != null) {
            viewGroup.addView(textView);
        }
        return textView;
    }

    public TextView addTextView(CharSequence charSequence, float f, int i, Typeface typeface) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        TextView textView = new TextView(this.aContext);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setLayoutParams(layoutParams);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        textView.setTextColor(i);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        textView.setId(AlGuiData.AlguiView.TextView.getId());
        textView.setGravity(17);
        return textView;
    }

    public MarqueeTextView addMarqueeTextView(ViewGroup viewGroup, int i, CharSequence charSequence, float f, int i2, Typeface typeface, long j, int i3, boolean z) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this.aContext);
        horizontalScrollView.setLayoutParams(layoutParams);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.setVerticalScrollBarEnabled(false);
        horizontalScrollView.setBackgroundColor(0);
        MarqueeTextView marqueeTextView = new MarqueeTextView(this.aContext);
        marqueeTextView.setMovementMethod(LinkMovementMethod.getInstance());
        marqueeTextView.setLayoutParams(layoutParams);
        if (charSequence != null) {
            marqueeTextView.setText(charSequence);
        }
        marqueeTextView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        marqueeTextView.setTextColor(i2);
        if (typeface != null) {
            marqueeTextView.setTypeface(typeface);
        }
        marqueeTextView.setGravity(i);
        marqueeTextView.setId(AlGuiData.AlguiView.MarqueeTextView.getId());
        marqueeTextView.setSingleLine(z);
        marqueeTextView.getAnimation().setDuration(j);
        marqueeTextView.getAnimation().setRepeatCount(i3);
        marqueeTextView.getAnimation().setInterpolator(new LinearInterpolator());
        marqueeTextView.startAnimation(marqueeTextView.getAnimation());
        horizontalScrollView.addView(marqueeTextView);
        if (viewGroup != null) {
            viewGroup.addView(horizontalScrollView);
        }
        return marqueeTextView;
    }

    public HorizontalScrollView addMarqueeTextView(int i, CharSequence charSequence, float f, int i2, Typeface typeface, long j, int i3, boolean z) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2, 100.0f);
        layoutParams.setMargins(this.viewWBJ[0], this.viewWBJ[1], this.viewWBJ[2], this.viewWBJ[3]);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this.aContext);
        horizontalScrollView.setLayoutParams(layoutParams);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.setVerticalScrollBarEnabled(false);
        horizontalScrollView.setBackgroundColor(0);
        MarqueeTextView marqueeTextView = new MarqueeTextView(this.aContext);
        marqueeTextView.setMovementMethod(LinkMovementMethod.getInstance());
        marqueeTextView.setLayoutParams(layoutParams);
        if (charSequence != null) {
            marqueeTextView.setText(charSequence);
        }
        marqueeTextView.setTextSize(0, ViewTool.convertDpToPx(this.aContext, f));
        marqueeTextView.setTextColor(i2);
        if (typeface != null) {
            marqueeTextView.setTypeface(typeface);
        }
        marqueeTextView.setGravity(i);
        marqueeTextView.setId(AlGuiData.AlguiView.MarqueeTextView.getId());
        marqueeTextView.setSingleLine(z);
        marqueeTextView.getAnimation().setDuration(j);
        marqueeTextView.getAnimation().setRepeatCount(i3);
        marqueeTextView.getAnimation().setInterpolator(new LinearInterpolator());
        marqueeTextView.startAnimation(marqueeTextView.getAnimation());
        horizontalScrollView.addView(marqueeTextView);
        return horizontalScrollView;
    }

    public View addLine(ViewGroup viewGroup, float f, int i, boolean z) {
        View view = new View(this.aContext);
        view.setBackgroundColor(i);
        view.setLayoutParams(new LinearLayout.LayoutParams(z ? -1 : ViewTool.convertDpToPx(this.aContext, f), z ? ViewTool.convertDpToPx(this.aContext, f) : -1, z ? 100.0f : 0));
        view.setId(AlGuiData.AlguiView.Line.getId());
        if (viewGroup != null) {
            viewGroup.addView(view);
        }
        return view;
    }

    public View addLine(float f, int i, boolean z) {
        View view = new View(this.aContext);
        view.setBackgroundColor(i);
        view.setLayoutParams(new LinearLayout.LayoutParams(z ? -1 : ViewTool.convertDpToPx(this.aContext, f), z ? ViewTool.convertDpToPx(this.aContext, f) : -1, z ? 100.0f : 0));
        view.setId(AlGuiData.AlguiView.Line.getId());
        return view;
    }
}
