package irene.window.algui.CustomizeView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;
import androidx.core.view.ViewCompat;

/* loaded from: classes.dex */
public class GradientTextView extends TextView {
    public static final String TAG = "GradientTextView";
    private static DisplayMetrics sDisplayMetrics;
    private float colorSpace;
    private float colorSpeed;
    private int[] colors;
    private boolean isDynamic;
    private LinearGradient mLinearGradient;
    private Matrix mMatrix;
    private float mTranslate;

    /* JADX WARN: 'this' call moved to the top of the method (can break code semantics) */
    public GradientTextView(Context context) {
        this(context, null);
    }

    public GradientTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GradientTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.colors = new int[]{-65332, -13312, -16711732, -65434};
        this.isDynamic = true;
        setLayerType(1, null);
        float f = 0;
        setShadowLayer(10, f, f, ViewCompat.MEASURED_STATE_MASK);
        this.colorSpace = dp2px(100);
        this.colorSpeed = dp2px(1);
        this.mMatrix = new Matrix();
        initPaint();
    }

    private void initPaint() {
        if (this.colors == null || this.colors.length <= 0) {
            return;
        }
        float f = 0;
        this.mLinearGradient = new LinearGradient(f, f, this.colorSpace, f, this.colors, (float[]) null, Shader.TileMode.MIRROR);
        getPaint().setShader(this.mLinearGradient);
    }

    public int dp2px(float f) {
        return Math.round(f * getDisplayMetrics().density);
    }

    public DisplayMetrics getDisplayMetrics() {
        if (sDisplayMetrics == null) {
            sDisplayMetrics = Resources.getSystem().getDisplayMetrics();
        }
        return sDisplayMetrics;
    }

    public int getScreenHeight() {
        return getDisplayMetrics().heightPixels;
    }

    public int getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    public void setColors(int... iArr) {
        if (iArr == null || iArr.length <= 0) {
            return;
        }
        this.colors = iArr;
        initPaint();
    }

    public void setDynamicEnabled(boolean z) {
        this.isDynamic = z;
        postInvalidateOnAnimation();
    }

    public boolean getDynamicEnabled() {
        return this.isDynamic;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onDraw(Canvas canvas) {
        if (this.mMatrix == null) {
            this.mMatrix = new Matrix();
        }
        this.mTranslate += this.colorSpeed;
        this.mMatrix.setTranslate(this.mTranslate, 0);
        if (this.mLinearGradient != null) {
            this.mLinearGradient.setLocalMatrix(this.mMatrix);
        }
        super.onDraw(canvas);
        if (this.isDynamic) {
            postInvalidateOnAnimation();
        }
    }
}
