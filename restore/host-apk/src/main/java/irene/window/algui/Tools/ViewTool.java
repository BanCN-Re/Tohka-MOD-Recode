package irene.window.algui.Tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.TextView;
import kotlin.jvm.internal.ByteCompanionObject;

/* loaded from: classes.dex */
public class ViewTool {
    public static final String TAG = "ViewTool";

    public static int getByteCount(byte b) {
        if ((b & ByteCompanionObject.MIN_VALUE) == 0) {
            return 1;
        }
        if ((b & 224) == 192) {
            return 2;
        }
        if ((b & 240) == 224) {
            return 3;
        }
        return (b & 248) == 240 ? 4 : 1;
    }

    public static CharSequence wrapText(CharSequence charSequence, int i) {
        if (charSequence != null) {
            StringBuilder sb = new StringBuilder();
            int length = charSequence.length();
            float f = 0;
            float f2 = f;
            for (int i2 = 0; i2 < length; i2++) {
                char charAt = charSequence.charAt(i2);
                if ((charAt >= 19968 && charAt <= 40869) || charAt == 12295) {
                    float f3 = 1;
                    if (f2 + f3 > i) {
                        sb.append("\n");
                        f2 = f;
                    }
                    sb.append(charAt);
                    f2 += f3;
                } else {
                    if (f2 + 0.5d > i) {
                        sb.append("\n");
                        f2 = f;
                    }
                    sb.append(charAt);
                    f2 = (float) (f2 + 0.5d);
                }
            }
            return sb.toString();
        }
        return charSequence;
    }

    public static int createLayeredColor(int i, float f) {
        return Color.argb((int) (Color.alpha(i) * f), Color.red(i), Color.green(i), Color.blue(i));
    }

    public static int calculateColorInverse(int i) {
        return Color.rgb(255 - Color.red(i), 255 - Color.green(i), 255 - Color.blue(i));
    }

    public static int darkenColor(int i, float f) {
        return Color.argb(Color.alpha(i), Math.min((int) (Color.red(i) * f), 255), Math.min((int) (Color.green(i) * f), 255), Math.min((int) (Color.blue(i) * f), 255));
    }

    public static int brightenColor(int i) {
        Color.colorToHSV(i, r0);
        float[] fArr = {0.0f, fArr[1] * 1.2f, fArr[2] * 1.2f};
        return Color.HSVToColor(fArr);
    }

    public static int convertDpToPx(Context context, float f) {
        if (context == null) {
            return (int) f;
        }
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int dip2px(Context context, float f) {
        if (context == null) {
            return (int) f;
        }
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int px2dip(Context context, float f) {
        if (context == null) {
            return (int) f;
        }
        return (int) ((f / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static TextView setTextViewShadow(TextView textView, float f, float f2, float f3, int i) {
        if (textView != null) {
            textView.setLayerType(1, null);
            textView.setShadowLayer(f, f2, f3, i);
        }
        return textView;
    }

    public static Drawable setGradientBackground(ViewGroup viewGroup, int[] iArr, int i) {
        if (viewGroup == null || iArr == null) {
            return null;
        }
        Drawable background = viewGroup.getBackground();
        if (background == null) {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, iArr);
            gradientDrawable.setGradientType(i);
            viewGroup.setBackground(gradientDrawable);
            return gradientDrawable;
        }
        if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable2 = (GradientDrawable) background;
            gradientDrawable2.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            gradientDrawable2.setColors(iArr);
            gradientDrawable2.setGradientType(i);
            return gradientDrawable2;
        }
        return background;
    }
}
