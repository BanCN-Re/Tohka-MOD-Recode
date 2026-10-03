package com.Riruriru.Sx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/* loaded from: classes3.dex */
public class FloatControlView extends LinearLayout {
    private ImageView controlView;
    private float downX;
    private float downY;
    private FloatContentView floatContentView;
    private boolean isView;
    private Context mContext;
    private float moveX;
    private float moveY;
    private ValueAnimator rotationAnimator;
    private int screenHeight;
    private int screenWidth;
    private int signX;
    private int signY;
    private WindowManager wManager;
    private WindowManager.LayoutParams wParams;

    public FloatControlView(Context context) {
        super(context);
        this.mContext = context;
        initView();
    }

    private void initView() {
        this.controlView = new ImageView(this.mContext);
        int iconId = this.mContext.getResources().getIdentifier("ic_launcher", "drawable", this.mContext.getPackageName());
        if (iconId != 0) {
            this.controlView.setImageResource(iconId);
        }
        this.controlView.setBackgroundColor(0);
        addView(this.controlView, 120, 120);
        startRotationAnimation();
        this.wManager = (WindowManager) this.mContext.getSystemService("window");
        DisplayMetrics metrics = new DisplayMetrics();
        this.wManager.getDefaultDisplay().getRealMetrics(metrics);
        this.screenWidth = metrics.widthPixels;
        this.screenHeight = metrics.heightPixels;
        this.wParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 26) {
            this.wParams.type = 2038;
        } else {
            this.wParams.type = 2003;
        }
        this.wParams.flags = 8;
        this.wParams.gravity = 51;
        this.wParams.x = 0;
        this.wParams.y = 0;
        this.wParams.width = -2;
        this.wParams.height = -2;
        this.wParams.format = 1;
        this.floatContentView = new FloatContentView(this.mContext);
        this.controlView.setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.FloatControlView.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                FloatControlView.this.floatContentView.showView();
            }
        });
        this.controlView.setOnTouchListener(new View.OnTouchListener() { // from class: com.Riruriru.Sx.FloatControlView.2
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case 0:
                        FloatControlView.this.signX = FloatControlView.this.wParams.x;
                        FloatControlView.this.signY = FloatControlView.this.wParams.y;
                        FloatControlView.this.downX = event.getRawX();
                        FloatControlView.this.downY = event.getRawY();
                        return false;
                    case 1:
                    default:
                        return false;
                    case 2:
                        FloatControlView.this.moveX = event.getRawX();
                        FloatControlView.this.moveY = event.getRawY();
                        FloatControlView.this.wParams.x = FloatControlView.this.signX + ((int) (FloatControlView.this.moveX - FloatControlView.this.downX));
                        FloatControlView.this.wParams.y = FloatControlView.this.signY + ((int) (FloatControlView.this.moveY - FloatControlView.this.downY));
                        FloatControlView.this.updateView();
                        return false;
                }
            }
        });
    }

    private void startRotationAnimation() {
        this.rotationAnimator = ValueAnimator.ofFloat(0.0f, 360.0f);
        this.rotationAnimator.setDuration(10000L);
        this.rotationAnimator.setInterpolator(new LinearInterpolator());
        this.rotationAnimator.setRepeatCount(-1);
        this.rotationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.Riruriru.Sx.FloatControlView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float rotation = ((Float) animation.getAnimatedValue()).floatValue();
                FloatControlView.this.controlView.setRotation(rotation);
            }
        });
        this.rotationAnimator.start();
    }

    public void showView() {
        if (!this.isView) {
            this.isView = true;
            this.wManager.addView(this, this.wParams);
            startShowAnimation();
        }
    }

    public void updateView() {
        this.wManager.updateViewLayout(this, this.wParams);
    }

    public void clearView() {
        if (this.isView) {
            startHideAnimation(new Runnable() { // from class: com.Riruriru.Sx.FloatControlView.4
                @Override // java.lang.Runnable
                public void run() {
                    FloatControlView.this.isView = false;
                    FloatControlView.this.wManager.removeView(FloatControlView.this);
                }
            });
        }
    }

    private void startShowAnimation() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        alphaAnimator.setDuration(500L);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(this, "translationY", -200.0f, 0.0f);
        translationAnimator.setDuration(500L);
        alphaAnimator.start();
        translationAnimator.start();
    }

    private void startHideAnimation(final Runnable endAction) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
        alphaAnimator.setDuration(500L);
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(this, "translationY", 0.0f, -200.0f);
        translationAnimator.setDuration(500L);
        alphaAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.Riruriru.Sx.FloatControlView.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                endAction.run();
            }
        });
        alphaAnimator.start();
        translationAnimator.start();
    }
}
