package irene.window.algui.Tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

/* loaded from: classes.dex */
public class ImageTool {
    public static final String TAG = "ImageTool";

    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return createBitmap;
    }

    public static Bitmap getAssetsImage(Context context, String str) {
        Bitmap bitmap = null;
        try {
            InputStream open = context.getAssets().open(str);
            bitmap = BitmapFactory.decodeStream(open);
            open.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap getBase64Image(String str) {
        byte[] decode = Base64.decode(str, 0);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    public static Bitmap fastblur(Bitmap bitmap, int i) {
        long currentTimeMillis = System.currentTimeMillis();
        Matrix matrix = new Matrix();
        matrix.postScale(0.25f, 0.25f);
        Bitmap blur = blur(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true), i);
        Log.i("FastBlurUtility", new StringBuffer().append("=====blur time:").append(System.currentTimeMillis() - currentTimeMillis).toString());
        Matrix matrix2 = new Matrix();
        matrix2.postScale(4.0f, 4.0f);
        return Bitmap.createBitmap(blur, 0, 0, blur.getWidth(), blur.getHeight(), matrix2, true);
    }

    public static Bitmap blur(Bitmap bitmap, int i) {
        int i2;
        int i3 = i;
        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
        if (i3 < 1) {
            return null;
        }
        int width = copy.getWidth();
        int height = copy.getHeight();
        int i4 = width * height;
        int[] iArr = new int[i4];
        copy.getPixels(iArr, 0, width, 0, 0, width, height);
        int i5 = width - 1;
        int i6 = height - 1;
        int i7 = i3 + i3 + 1;
        int[] iArr2 = new int[i4];
        int[] iArr3 = new int[i4];
        int[] iArr4 = new int[i4];
        int[] iArr5 = new int[Math.max(width, height)];
        int i8 = (i7 + 1) >> 1;
        int i9 = i8 * i8;
        int i10 = i9 * 256;
        int[] iArr6 = new int[i10];
        int i11 = 0;
        int i12 = 0;
        while (i12 < i10) {
            iArr6[i12] = i12 / i9;
            i12++;
            i3 = i;
        }
        char c = 2;
        int[][] iArr7 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, i7, 3);
        int i13 = i3 + 1;
        int i14 = 0;
        int i15 = 0;
        int i16 = 0;
        while (i14 < height) {
            int[] iArr8 = iArr4;
            char c2 = c;
            Bitmap bitmap2 = copy;
            int i17 = -i3;
            int i18 = 0;
            int i19 = 0;
            int i20 = 0;
            int i21 = 0;
            int i22 = 0;
            int i23 = 0;
            int i24 = 0;
            int i25 = 0;
            int i26 = 0;
            while (i17 <= i3) {
                int i27 = i18;
                int i28 = iArr[i15 + Math.min(i5, Math.max(i17, 0))];
                int[] iArr9 = iArr7[i17 + i];
                iArr9[0] = (i28 & 16711680) >> 16;
                iArr9[1] = (i28 & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                iArr9[c2] = i28 & 255;
                int abs = i13 - Math.abs(i17);
                i19 += iArr9[0] * abs;
                i18 = i27 + (iArr9[1] * abs);
                i20 += iArr9[c2] * abs;
                if (i17 > 0) {
                    i26 += iArr9[0];
                    i22 += iArr9[1];
                    i21 += iArr9[c2];
                } else {
                    i23 += iArr9[0];
                    i24 += iArr9[1];
                    i25 += iArr9[c2];
                }
                i17++;
                i3 = i;
            }
            int i29 = i3;
            int i30 = 0;
            while (i30 < width) {
                iArr2[i15] = iArr6[i19];
                iArr3[i15] = iArr6[i18];
                iArr8[i15] = iArr6[i20];
                int[] iArr10 = iArr7[((i29 - i3) + i7) % i7];
                int i31 = iArr10[0];
                int i32 = iArr10[1];
                int i33 = iArr10[c2];
                if (i14 == 0) {
                    iArr5[i30] = Math.min(i30 + i3 + 1, i5);
                }
                int i34 = iArr[i16 + iArr5[i30]];
                iArr10[0] = (i34 & 16711680) >> 16;
                iArr10[1] = (i34 & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                iArr10[c2] = i34 & 255;
                int i35 = i26 + iArr10[0];
                int i36 = i22 + iArr10[1];
                int i37 = i21 + iArr10[c2];
                i19 = (i19 - i23) + i35;
                i18 = (i18 - i24) + i36;
                i20 = (i20 - i25) + i37;
                i29 = (i29 + 1) % i7;
                int[] iArr11 = iArr7[i29 % i7];
                i23 = (i23 - i31) + iArr11[0];
                i24 = (i24 - i32) + iArr11[1];
                i25 = (i25 - i33) + iArr11[c2];
                i26 = i35 - iArr11[0];
                i22 = i36 - iArr11[1];
                i21 = i37 - iArr11[c2];
                i15++;
                i30++;
                i3 = i;
            }
            i16 += width;
            i14++;
            c = c2;
            iArr4 = iArr8;
            copy = bitmap2;
            i11 = 0;
        }
        int i38 = i11;
        while (i38 < width) {
            int i39 = -i3;
            int i40 = i11;
            int i41 = i40;
            int i42 = i41;
            int i43 = i42;
            int i44 = i43;
            int i45 = i44;
            int i46 = i45;
            int[] iArr12 = iArr4;
            char c3 = c;
            int i47 = i39;
            int i48 = i39 * width;
            int i49 = i46;
            int i50 = i49;
            while (i47 <= i3) {
                Bitmap bitmap3 = copy;
                int i51 = i11;
                int max = Math.max(i51, i48) + i38;
                int[] iArr13 = iArr7[i47 + i3];
                iArr13[i51] = iArr2[max];
                iArr13[1] = iArr3[max];
                iArr13[c3] = iArr12[max];
                int abs2 = i13 - Math.abs(i47);
                i49 += iArr2[max] * abs2;
                i50 += iArr3[max] * abs2;
                i40 += iArr12[max] * abs2;
                if (i47 > 0) {
                    i43 += iArr13[0];
                    i42 += iArr13[1];
                    i41 += iArr13[c3];
                } else {
                    i44 += iArr13[0];
                    i45 += iArr13[1];
                    i46 += iArr13[c3];
                }
                if (i47 < i6) {
                    i48 += width;
                }
                i47++;
                copy = bitmap3;
                i11 = 0;
            }
            int i52 = i11;
            int i53 = i3;
            int i54 = i38;
            while (i52 < height) {
                iArr[i54] = (iArr[i54] & ViewCompat.MEASURED_STATE_MASK) | (iArr6[i49] << 16) | (iArr6[i50] << 8) | iArr6[i40];
                int[] iArr14 = iArr7[((i53 - i3) + i7) % i7];
                int i55 = iArr14[i11];
                int i56 = iArr14[1];
                int i57 = iArr14[c3];
                if (i38 != 0) {
                    i2 = i52;
                } else {
                    i2 = i52;
                    iArr5[i2] = Math.min(i2 + i13, i6) * width;
                }
                int i58 = iArr5[i2] + i38;
                iArr14[i11] = iArr2[i58];
                iArr14[1] = iArr3[i58];
                iArr14[c3] = iArr12[i58];
                int i59 = i43 + iArr14[i11];
                int i60 = i42 + iArr14[1];
                int i61 = i41 + iArr14[c3];
                i49 = (i49 - i44) + i59;
                i50 = (i50 - i45) + i60;
                i40 = (i40 - i46) + i61;
                i53 = (i53 + 1) % i7;
                int[] iArr15 = iArr7[i53];
                i44 = (i44 - i55) + iArr15[i11];
                i45 = (i45 - i56) + iArr15[1];
                i46 = (i46 - i57) + iArr15[c3];
                i43 = i59 - iArr15[i11];
                i42 = i60 - iArr15[1];
                i41 = i61 - iArr15[c3];
                i54 += width;
                i52 = i2 + 1;
            }
            i38++;
            c = c3;
            iArr4 = iArr12;
        }
        copy.setPixels(iArr, 0, width, 0, 0, width, height);
        return copy;
    }
}
