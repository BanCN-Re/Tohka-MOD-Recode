package irene.window.algui.CustomizeView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import irene.window.algui.Tools.ViewTool;

/* loaded from: classes.dex */
public class vFrameLayout extends FrameLayout {
    public static final String TAG = "vFrameLayout";
    private int mRadius;
    private GradientDrawable mainBackground;
    private int tBorderColor;
    private int tBorderSize;

    public vFrameLayout(Context context) {
        super(context);
        this.mRadius = 0;
        this.tBorderSize = ViewTool.convertDpToPx(context, 1.2f);
        this.tBorderColor = -1;
        this.mainBackground = new GradientDrawable();
        setBackground(this.mainBackground);
    }

    public vFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRadius = 0;
        this.tBorderSize = ViewTool.convertDpToPx(context, 1.2f);
        this.tBorderColor = -1;
        this.mainBackground = new GradientDrawable();
        setBackground(this.mainBackground);
    }

    public vFrameLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRadius = 0;
        this.tBorderSize = ViewTool.convertDpToPx(context, 1.2f);
        this.tBorderColor = -1;
        this.mainBackground = new GradientDrawable();
        setBackground(this.mainBackground);
    }

    public void setFilletRadiu(int i) {
        this.mRadius = i;
        this.mainBackground.setCornerRadius(i);
    }

    public void setBackColor(int i) {
        this.mainBackground.setColor(i);
    }

    public void setBorder(int i, int i2) {
        this.tBorderSize = i;
        this.tBorderColor = i2;
        this.mainBackground.setStroke(this.tBorderSize, this.tBorderColor);
        setPadding(this.tBorderSize, this.tBorderSize, this.tBorderSize, this.tBorderSize);
    }

    public void setBorderSize(int i) {
        this.tBorderSize = i;
        this.mainBackground.setStroke(this.tBorderSize, this.tBorderColor);
        setPadding(this.tBorderSize, this.tBorderSize, this.tBorderSize, this.tBorderSize);
    }

    public void setBorderColor(int i) {
        this.tBorderColor = i;
        this.mainBackground.setStroke(this.tBorderSize, this.tBorderColor);
    }

    public GradientDrawable getBack() {
        return this.mainBackground;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        RectF rectF = new RectF();
        float f = 0;
        rectF.set(f, f, getWidth(), getHeight());
        path.addRoundRect(rectF, this.mRadius, this.mRadius, Path.Direction.CW);
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
    }
}
