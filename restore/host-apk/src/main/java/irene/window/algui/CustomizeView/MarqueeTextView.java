package irene.window.algui.CustomizeView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

/* loaded from: classes.dex */
public class MarqueeTextView extends TextView {
    public static final String TAG = "MarqueeTextView";
    private Context mContext;
    Animation marqueeAnimation;

    public MarqueeTextView(Context context) {
        super(context);
        this.mContext = context;
        setMarqueeAnimation();
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        setMarqueeAnimation();
    }

    public MarqueeTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
        setMarqueeAnimation();
    }

    private void setMarqueeAnimation() {
        super.setEllipsize(null);
        super.setSelected(true);
        super.setGravity(17);
        float f = 0;
        this.marqueeAnimation = new TranslateAnimation(2, 1.0f, 2, -1.0f, 2, f, 2, f);
        this.marqueeAnimation.setDuration(8000);
        this.marqueeAnimation.setRepeatCount(-1);
        this.marqueeAnimation.setInterpolator(new LinearInterpolator());
        super.startAnimation(this.marqueeAnimation);
    }

    public Animation getMarqueeAnimation() {
        return this.marqueeAnimation;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        super.startAnimation(this.marqueeAnimation);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        super.clearAnimation();
    }
}
