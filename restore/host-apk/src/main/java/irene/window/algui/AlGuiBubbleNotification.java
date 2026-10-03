package irene.window.algui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import irene.window.algui.AlGuiData;
import irene.window.algui.CustomizeView.vFrameLayout;
import irene.window.algui.CustomizeView.vLinearLayout;
import irene.window.algui.Tools.AppPermissionTool;
import irene.window.algui.Tools.ImageTool;
import irene.window.algui.Tools.ViewTool;

/* loaded from: classes.dex */
public class AlGuiBubbleNotification {
    public static final String TAG = "AlGuiBubbleNotification";
    private static AlGuiBubbleNotification bn;
    private WindowManager Manager;
    private Context context;
    private boolean isWLayout = false;
    private vLinearLayout mainLayout;
    private vFrameLayout rootLayout;
    private WindowManager.LayoutParams wParams;

    /* loaded from: classes.dex */
    public interface T_ButtonOnChangeListener {
        void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z);
    }

    public WindowManager getWindowManager() {
        return this.Manager;
    }

    public WindowManager.LayoutParams getWindowManageLayoutParams() {
        return this.wParams;
    }

    public vFrameLayout getRootLayout() {
        return this.rootLayout;
    }

    public vLinearLayout getMainLayout() {
        return this.mainLayout;
    }

    private void initWindow() {
        this.Manager = (WindowManager) this.context.getSystemService("window");
        this.wParams = new WindowManager.LayoutParams();
        ((ViewGroup.LayoutParams) this.wParams).width = -2;
        ((ViewGroup.LayoutParams) this.wParams).height = -2;
        this.wParams.gravity = 8388693;
        this.wParams.format = 1;
        this.wParams.windowAnimations = android.R.style.Animation.Toast;
        this.wParams.flags = AlGuiData.getLiveStreamFlags() | 8 | 16777216;
        this.wParams.y = dpToPx(16);
        this.wParams.x = dpToPx(16);
        if (AppPermissionTool.isAndroidManifestPermissionExist(this.context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            this.wParams.type = Build.VERSION.SDK_INT >= 26 ? 2038 : 2003;
        }
    }

    public void showW() {
        if (this.context != null && AppPermissionTool.isAndroidManifestPermissionExist(this.context, "android.permission.SYSTEM_ALERT_WINDOW") && AppPermissionTool.checkOverlayPermission(this.context) && !this.isWLayout) {
            this.isWLayout = true;
            this.Manager.addView(this.rootLayout, this.wParams);
        }
    }

    public void updateW() {
        if (this.context != null && this.isWLayout) {
            this.Manager.updateViewLayout(this.rootLayout, this.wParams);
        }
    }

    public void clearW() {
        if (this.context == null || this.Manager == null || !this.isWLayout) {
            return;
        }
        this.isWLayout = false;
        this.Manager.removeView(this.rootLayout);
    }

    public static AlGuiBubbleNotification Inform(Context context) {
        if (bn == null) {
            bn = new AlGuiBubbleNotification(context);
        }
        return bn;
    }

    AlGuiBubbleNotification(Context context) {
        if (context == null) {
            return;
        }
        this.context = context;
        initWindow();
        initLayout();
        showW();
    }

    private int dpToPx(int i) {
        return (int) ((i * this.context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void initLayout() {
        this.rootLayout = new vFrameLayout(this.context);
        this.mainLayout = new vLinearLayout(this.context);
        this.mainLayout.setBackgroundColor(0);
        this.mainLayout.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        this.mainLayout.setOrientation(1);
        TextView textView = new TextView(this.context);
        textView.setText(" ");
        textView.setTextSize(0, ViewTool.convertDpToPx(this.context, 1.0f));
        this.mainLayout.addView(textView);
        this.rootLayout.addView(this.mainLayout);
    }

    private vLinearLayout addSmallButton(CharSequence charSequence, float f, int i, Typeface typeface, float f2, int i2, float f3, int i3, T_ButtonOnChangeListener t_ButtonOnChangeListener) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(ViewTool.convertDpToPx(this.context, f2));
        gradientDrawable.setStroke(ViewTool.convertDpToPx(this.context, f3), i3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(8, 8, 8, 8);
        vLinearLayout vlinearlayout = new vLinearLayout(this.context);
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(20, 10, 20, 10);
        vlinearlayout.setClipChildren(true);
        vlinearlayout.setId(AlGuiData.AlguiView.SmallButton.getId());
        vlinearlayout.setGravity(17);
        TextView textView = new TextView(this.context);
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setTextSize(0, ViewTool.convertDpToPx(this.context, f));
        textView.setTextColor(i);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
        vlinearlayout.setOnClickListener(new AnonymousClass100000001(this, gradientDrawable, i2, vlinearlayout, t_ButtonOnChangeListener, textView));
        vlinearlayout.addView(textView);
        return vlinearlayout;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiBubbleNotification$100000001, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000001 implements View.OnClickListener {
        boolean isChecked = true;
        boolean isOne = true;
        private final AlGuiBubbleNotification this$0;
        private final GradientDrawable val$back;
        private final int val$backColor;
        private final vLinearLayout val$button;
        private final TextView val$buttonText;
        private final T_ButtonOnChangeListener val$fun;

        AnonymousClass100000001(AlGuiBubbleNotification alGuiBubbleNotification, GradientDrawable gradientDrawable, int i, vLinearLayout vlinearlayout, T_ButtonOnChangeListener t_ButtonOnChangeListener, TextView textView) {
            this.this$0 = alGuiBubbleNotification;
            this.val$back = gradientDrawable;
            this.val$backColor = i;
            this.val$button = vlinearlayout;
            this.val$fun = t_ButtonOnChangeListener;
            this.val$buttonText = textView;
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            this.isOne = true;
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
            this.val$back.setColor(ViewTool.darkenColor(this.val$backColor, 0.7f));
            animationSet.setAnimationListener(new Animation.AnimationListener(this, this.val$button, this.val$back, this.val$backColor, this.val$fun, view, this.val$buttonText) { // from class: irene.window.algui.AlGuiBubbleNotification.100000001.100000000
                private final AnonymousClass100000001 this$0;
                private final GradientDrawable val$back;
                private final int val$backColor;
                private final vLinearLayout val$button;
                private final TextView val$buttonText;
                private final T_ButtonOnChangeListener val$fun;
                private final View val$v;

                {
                    this.this$0 = this;
                    this.val$button = r2;
                    this.val$back = r3;
                    this.val$backColor = r4;
                    this.val$fun = r5;
                    this.val$v = view;
                    this.val$buttonText = r7;
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
                    this.val$back.setColor(this.val$backColor);
                    if (!this.this$0.isOne || this.val$fun == null) {
                        return;
                    }
                    this.val$fun.onClick(this.val$v, this.val$back, this.val$buttonText, this.this$0.isChecked);
                    this.this$0.isChecked = !this.this$0.isChecked;
                    this.this$0.isOne = false;
                }
            });
            this.val$button.startAnimation(animationSet);
        }
    }

    public void showCustomizeButtonNotification(int i, Bitmap bitmap, int i2, CharSequence charSequence, int i3, CharSequence charSequence2, int i4, CharSequence charSequence3, int i5, int i6, int i7, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, int i8, int i9, int i10, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        boolean z;
        boolean z2;
        vLinearLayout vlinearlayout;
        boolean z3;
        boolean z4;
        if (this.context == null) {
            return;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 10, 0, 10);
        vLinearLayout vlinearlayout2 = new vLinearLayout(this.context);
        vlinearlayout2.setBackColor(i);
        vlinearlayout2.setFilletRadiu(dpToPx(11));
        vlinearlayout2.setLayoutParams(layoutParams);
        vlinearlayout2.setOrientation(0);
        vlinearlayout2.setPadding(20, 20, 20, 20);
        vlinearlayout2.setGravity(17);
        vlinearlayout2.setId(AlGuiData.AlguiNotification.ButtonNotification.getId());
        vlinearlayout2.setAlpha(1.0f);
        ImageView imageView = new ImageView(this.context);
        imageView.setPadding(10, 10, 10, 10);
        if (i2 != 0) {
            imageView.setImageTintList(ColorStateList.valueOf(i2));
        }
        imageView.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(30), dpToPx(30)));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Message()));
        }
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(10, 0, 0, 0);
        vLinearLayout vlinearlayout3 = new vLinearLayout(this.context);
        vlinearlayout3.setBackColor(0);
        vlinearlayout3.setLayoutParams(layoutParams2);
        vlinearlayout3.setOrientation(1);
        TextView textView = new TextView(this.context);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        if (charSequence != null) {
            textView.setText(charSequence);
        }
        textView.setSingleLine(false);
        textView.setMaxEms(11);
        textView.setTextColor(i3);
        textView.setGravity(3);
        textView.setTextSize(0, dpToPx(11));
        textView.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
        TextView textView2 = new TextView(this.context);
        textView2.setMovementMethod(LinkMovementMethod.getInstance());
        if (charSequence2 != null) {
            textView2.setText(charSequence2);
        }
        textView2.setSingleLine(false);
        textView2.setMaxEms(11);
        textView2.setTextColor(i4);
        textView2.setTextSize(0, dpToPx(8));
        textView2.setGravity(3);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(0, 0, 30, 0);
        vLinearLayout vlinearlayout4 = new vLinearLayout(this.context);
        vlinearlayout4.setBackColor(0);
        vlinearlayout4.setLayoutParams(layoutParams3);
        vlinearlayout4.setOrientation(0);
        vlinearlayout4.setGravity(5);
        float f = 8;
        float f2 = 2;
        vLinearLayout addSmallButton = addSmallButton(charSequence3, f, i5, Typeface.create(Typeface.DEFAULT, 1), f2, i6, i10 == 0 ? 0 : 0.7f, i7, new AnonymousClass100000003(this, t_ButtonOnChangeListener, vlinearlayout2));
        vLinearLayout addSmallButton2 = addSmallButton(charSequence4, f, i8, Typeface.create(Typeface.DEFAULT, 1), f2, i9, i10 == 0 ? 0 : 0.7f, i10, new AnonymousClass100000005(this, t_ButtonOnChangeListener2, vlinearlayout2));
        vlinearlayout2.addView(imageView);
        if (charSequence == null) {
            z = false;
        } else {
            vlinearlayout3.addView(textView);
            z = true;
        }
        if (charSequence2 == null) {
            z2 = false;
        } else {
            vlinearlayout3.addView(textView2);
            z2 = true;
        }
        if (charSequence3 == null) {
            vlinearlayout = vlinearlayout4;
            z3 = false;
        } else {
            vlinearlayout = vlinearlayout4;
            vlinearlayout.addView(addSmallButton);
            z3 = true;
        }
        if (charSequence4 == null) {
            z4 = false;
        } else {
            vlinearlayout.addView(addSmallButton2);
            z4 = true;
        }
        if (z3 || z4) {
            vlinearlayout3.addView(vlinearlayout);
        }
        if (z || z2) {
            vlinearlayout2.addView(vlinearlayout3);
        }
        this.mainLayout.addView(vlinearlayout2);
        new Handler(Looper.getMainLooper()).postDelayed(new AnonymousClass100000007(this, vlinearlayout2), 60000);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        vlinearlayout2.startAnimation(alphaAnimation);
        this.wParams.flags = AlGuiData.getLiveStreamFlags() | 8;
        updateW();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiBubbleNotification$100000003, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000003 implements T_ButtonOnChangeListener {
        private final AlGuiBubbleNotification this$0;
        private final T_ButtonOnChangeListener val$cancelFun;
        private final vLinearLayout val$layout;

        AnonymousClass100000003(AlGuiBubbleNotification alGuiBubbleNotification, T_ButtonOnChangeListener t_ButtonOnChangeListener, vLinearLayout vlinearlayout) {
            this.this$0 = alGuiBubbleNotification;
            this.val$cancelFun = t_ButtonOnChangeListener;
            this.val$layout = vlinearlayout;
        }

        @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            if (this.val$cancelFun != null) {
                this.val$cancelFun.onClick(view, gradientDrawable, textView, z);
            }
            if (this.val$layout != null) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(2000);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener(this, this.val$layout) { // from class: irene.window.algui.AlGuiBubbleNotification.100000003.100000002
                    private final AnonymousClass100000003 this$0;
                    private final vLinearLayout val$layout;

                    {
                        this.this$0 = this;
                        this.val$layout = r2;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (this.this$0.this$0.mainLayout.indexOfChild(this.val$layout) != -1) {
                            this.val$layout.setVisibility(8);
                            this.this$0.this$0.mainLayout.removeView(this.val$layout);
                        }
                        if (this.this$0.this$0.mainLayout.findViewById(AlGuiData.AlguiNotification.ButtonNotification.getId()) == null) {
                            this.this$0.this$0.wParams.flags = AlGuiData.getLiveStreamFlags() | 24;
                            this.this$0.this$0.updateW();
                        }
                    }
                });
                this.val$layout.startAnimation(alphaAnimation);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiBubbleNotification$100000005, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000005 implements T_ButtonOnChangeListener {
        private final AlGuiBubbleNotification this$0;
        private final T_ButtonOnChangeListener val$confirmFun;
        private final vLinearLayout val$layout;

        AnonymousClass100000005(AlGuiBubbleNotification alGuiBubbleNotification, T_ButtonOnChangeListener t_ButtonOnChangeListener, vLinearLayout vlinearlayout) {
            this.this$0 = alGuiBubbleNotification;
            this.val$confirmFun = t_ButtonOnChangeListener;
            this.val$layout = vlinearlayout;
        }

        @Override // irene.window.algui.AlGuiBubbleNotification.T_ButtonOnChangeListener
        public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
            if (this.val$confirmFun != null) {
                this.val$confirmFun.onClick(view, gradientDrawable, textView, z);
            }
            if (this.val$layout != null) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(2000);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener(this, this.val$layout) { // from class: irene.window.algui.AlGuiBubbleNotification.100000005.100000004
                    private final AnonymousClass100000005 this$0;
                    private final vLinearLayout val$layout;

                    {
                        this.this$0 = this;
                        this.val$layout = r2;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (this.this$0.this$0.mainLayout.indexOfChild(this.val$layout) != -1) {
                            this.val$layout.setVisibility(8);
                            this.this$0.this$0.mainLayout.removeView(this.val$layout);
                        }
                        if (this.this$0.this$0.mainLayout.findViewById(AlGuiData.AlguiNotification.ButtonNotification.getId()) == null) {
                            this.this$0.this$0.wParams.flags = AlGuiData.getLiveStreamFlags() | 24;
                            this.this$0.this$0.updateW();
                        }
                    }
                });
                this.val$layout.startAnimation(alphaAnimation);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiBubbleNotification$100000007, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000007 implements Runnable {
        private final AlGuiBubbleNotification this$0;
        private final vLinearLayout val$layout;

        AnonymousClass100000007(AlGuiBubbleNotification alGuiBubbleNotification, vLinearLayout vlinearlayout) {
            this.this$0 = alGuiBubbleNotification;
            this.val$layout = vlinearlayout;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.val$layout != null) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(2000);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener(this, this.val$layout) { // from class: irene.window.algui.AlGuiBubbleNotification.100000007.100000006
                    private final AnonymousClass100000007 this$0;
                    private final vLinearLayout val$layout;

                    {
                        this.this$0 = this;
                        this.val$layout = r2;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (this.this$0.this$0.mainLayout.indexOfChild(this.val$layout) != -1) {
                            this.val$layout.setVisibility(8);
                            this.this$0.this$0.mainLayout.removeView(this.val$layout);
                        }
                        if (this.this$0.this$0.mainLayout.findViewById(AlGuiData.AlguiNotification.ButtonNotification.getId()) == null) {
                            this.this$0.this$0.wParams.flags = AlGuiData.getLiveStreamFlags() | 24;
                            this.this$0.this$0.updateW();
                        }
                    }
                });
                this.val$layout.startAnimation(alphaAnimation);
            }
        }
    }

    public void showCustomizeNotification(int i, Bitmap bitmap, int i2, CharSequence charSequence, int i3, CharSequence charSequence2, int i4, long j) {
        boolean z;
        boolean z2;
        if (this.context == null) {
            return;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 10, 0, 10);
        vLinearLayout vlinearlayout = new vLinearLayout(this.context);
        vlinearlayout.setBackColor(i);
        vlinearlayout.setFilletRadiu(dpToPx(11));
        vlinearlayout.setLayoutParams(layoutParams);
        vlinearlayout.setOrientation(0);
        vlinearlayout.setPadding(20, 20, 20, 20);
        vlinearlayout.setGravity(17);
        vlinearlayout.setId(AlGuiData.AlguiNotification.MessageNotification.getId());
        vlinearlayout.setAlpha(1.0f);
        ImageView imageView = new ImageView(this.context);
        imageView.setPadding(10, 10, 10, 10);
        if (i2 != 0) {
            imageView.setImageTintList(ColorStateList.valueOf(i2));
        }
        imageView.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(30), dpToPx(30)));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageBitmap(ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Message()));
        }
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(10, 0, 0, 0);
        vLinearLayout vlinearlayout2 = new vLinearLayout(this.context);
        vlinearlayout2.setBackColor(0);
        vlinearlayout2.setLayoutParams(layoutParams2);
        vlinearlayout2.setOrientation(1);
        TextView textView = new TextView(this.context);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(charSequence);
        textView.setSingleLine(false);
        textView.setMaxEms(11);
        textView.setTextColor(i3);
        textView.setGravity(3);
        textView.setTextSize(0, dpToPx(11));
        textView.setTypeface(Typeface.create(Typeface.DEFAULT, 1));
        TextView textView2 = new TextView(this.context);
        textView2.setMovementMethod(LinkMovementMethod.getInstance());
        textView2.setText(charSequence2);
        textView2.setSingleLine(false);
        textView2.setMaxEms(11);
        textView2.setTextColor(i4);
        textView2.setTextSize(0, dpToPx(8));
        textView2.setGravity(3);
        vlinearlayout.addView(imageView);
        if (charSequence == null) {
            z = false;
        } else {
            vlinearlayout2.addView(textView);
            z = true;
        }
        if (charSequence2 == null) {
            z2 = false;
        } else {
            vlinearlayout2.addView(textView2);
            z2 = true;
        }
        if (z || z2) {
            vlinearlayout.addView(vlinearlayout2);
        }
        this.mainLayout.addView(vlinearlayout);
        new Handler(Looper.getMainLooper()).postDelayed(new AnonymousClass100000009(this, vlinearlayout), j);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        vlinearlayout.startAnimation(alphaAnimation);
        if (this.mainLayout.findViewById(AlGuiData.AlguiNotification.ButtonNotification.getId()) != null) {
            this.wParams.flags = 8 | AlGuiData.getLiveStreamFlags();
        } else {
            this.wParams.flags = AlGuiData.getLiveStreamFlags() | 24;
        }
        updateW();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: irene.window.algui.AlGuiBubbleNotification$100000009, reason: invalid class name */
    /* loaded from: classes.dex */
    public class AnonymousClass100000009 implements Runnable {
        private final AlGuiBubbleNotification this$0;
        private final vLinearLayout val$layout;

        AnonymousClass100000009(AlGuiBubbleNotification alGuiBubbleNotification, vLinearLayout vlinearlayout) {
            this.this$0 = alGuiBubbleNotification;
            this.val$layout = vlinearlayout;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.val$layout != null) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setDuration(2000);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener(this, this.val$layout) { // from class: irene.window.algui.AlGuiBubbleNotification.100000009.100000008
                    private final AnonymousClass100000009 this$0;
                    private final vLinearLayout val$layout;

                    {
                        this.this$0 = this;
                        this.val$layout = r2;
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override // android.view.animation.Animation.AnimationListener
                    public void onAnimationEnd(Animation animation) {
                        if (this.this$0.this$0.mainLayout.indexOfChild(this.val$layout) != -1) {
                            this.val$layout.setVisibility(8);
                            this.this$0.this$0.mainLayout.removeView(this.val$layout);
                        }
                    }
                });
                this.val$layout.startAnimation(alphaAnimation);
            }
        }
    }

    public void showMessageNotification_Exquisite(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MESSAGE_GTA);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Message());
        }
        showCustomizeNotification(-1314069, bitmap, ViewCompat.MEASURED_STATE_MASK, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, j);
    }

    public void showMessageNotification_Exquisite_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MESSAGE_GTA);
        showCustomizeButtonNotification(-1314069, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Message()), ViewCompat.MEASURED_STATE_MASK, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, charSequence3, ViewCompat.MEASURED_STATE_MASK, 0, -6381922, t_ButtonOnChangeListener, charSequence4, -15108398, 0, -6381922, t_ButtonOnChangeListener2);
    }

    public void showSuccessNotification_Exquisite(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_SUCCESS);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Success());
        }
        showCustomizeNotification(-5378639, bitmap, -14834842, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, j);
    }

    public void showSuccessNotification_Exquisite_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_SUCCESS);
        showCustomizeButtonNotification(-5378639, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Success()), -14834842, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, charSequence3, -13730510, 0, -13730510, t_ButtonOnChangeListener, charSequence4, -13730510, 0, -13730510, t_ButtonOnChangeListener2);
    }

    public void showMistakeNotification_Exquisite(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MISTAKE);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Mistake());
        }
        showCustomizeNotification(-2126211, bitmap, -5092028, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, j);
    }

    public void showMistakeNotification_Exquisite_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MISTAKE);
        showCustomizeButtonNotification(-2126211, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Mistake()), -5092028, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, charSequence3, -5092028, 0, -5092028, t_ButtonOnChangeListener, charSequence4, -5092028, 0, -5092028, t_ButtonOnChangeListener2);
    }

    public void showAlertNotification_Exquisite(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_ALERT);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Alert());
        }
        showCustomizeNotification(-993413, bitmap, -3963585, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, j);
    }

    public void showAlertNotification_Exquisite_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_ALERT);
        showCustomizeButtonNotification(-993413, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getExquisiteNotice_Icon_Alert()), -3963585, charSequence, ViewCompat.MEASURED_STATE_MASK, charSequence2, -12434878, charSequence3, -3963585, 0, -3963585, t_ButtonOnChangeListener, charSequence4, -3963585, 0, -3963585, t_ButtonOnChangeListener2);
    }

    public void showMessageNotification_Simplicity(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MESSAGE_GTA);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Message());
        }
        showCustomizeNotification(-13619152, bitmap, 0, charSequence, -1, charSequence2, 1627389951, j);
    }

    public void showMessageNotification_Simplicity_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MESSAGE_GTA);
        showCustomizeButtonNotification(-13619152, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Message()), 0, charSequence, -1, charSequence2, 1627389951, charSequence3, -13619152, -1, 0, t_ButtonOnChangeListener, charSequence4, -1, -15619481, 0, t_ButtonOnChangeListener2);
    }

    public void showSuccessNotification_Simplicity(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_SUCCESS);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Success());
        }
        showCustomizeNotification(-13619152, bitmap, 0, charSequence, -1, charSequence2, 1627389951, j);
    }

    public void showSuccessNotification_Simplicity_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_SUCCESS);
        showCustomizeButtonNotification(-13619152, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Success()), 0, charSequence, -1, charSequence2, 1627389951, charSequence3, -13619152, -1, 0, t_ButtonOnChangeListener, charSequence4, -1, -15619481, 0, t_ButtonOnChangeListener2);
    }

    public void showMistakeNotification_Simplicity(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MISTAKE);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Mistake());
        }
        showCustomizeNotification(-13619152, bitmap, 0, charSequence, -1, charSequence2, 1627389951, j);
    }

    public void showMistakeNotification_Simplicity_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_MISTAKE);
        showCustomizeButtonNotification(-13619152, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Mistake()), 0, charSequence, -1, charSequence2, 1627389951, charSequence3, -13619152, -1, 0, t_ButtonOnChangeListener, charSequence4, -1, -15619481, 0, t_ButtonOnChangeListener2);
    }

    public void showAlertNotification_Simplicity(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, long j) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_ALERT);
        if (bitmap == null) {
            bitmap = ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Alert());
        }
        showCustomizeNotification(-13619152, bitmap, 0, charSequence, -1, charSequence2, 1627389951, j);
    }

    public void showAlertNotification_Simplicity_Button(Bitmap bitmap, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, T_ButtonOnChangeListener t_ButtonOnChangeListener, CharSequence charSequence4, T_ButtonOnChangeListener t_ButtonOnChangeListener2) {
        if (this.context == null) {
            return;
        }
        AlGuiSoundEffect.getAudio(this.context).playSoundEffect(AlGuiSoundEffect.INFORM_ALERT);
        showCustomizeButtonNotification(-13619152, bitmap != null ? bitmap : ImageTool.getBase64Image(AlGuiData.getSimplicityNotice_Icon_Alert()), 0, charSequence, -1, charSequence2, 1627389951, charSequence3, -13619152, -1, 0, t_ButtonOnChangeListener, charSequence4, -1, -15619481, 0, t_ButtonOnChangeListener2);
    }
}
