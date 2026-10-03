package com.example.imgui;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/* loaded from: D:\Work\MuMuP\ArkReCodeHack\build\dex\libArkRe.injected.dex */
public class GLES3JNIView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String TAG = "GLES3JNIView";
    public static byte[] fontData;

    public static native void MotionEventClick(boolean z, float f, float f2);

    public static native void imgui_Shutdown();

    public static native void init(Object obj);

    public static native boolean isImGuiComponentTouched(float f, float f2);

    public static native void nativeOnTouchEvent(int i, int i2, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3, int i3);

    public static native void nativeUpdateFilterText(int i, String str);

    public static native void resize(int i, int i2);

    public static native void step();

    public static native void updateHexInput(String str);

    public GLES3JNIView(Context context) {
        super(context);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(3);
        setRenderer(this);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        init(getHolder().getSurface());
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        GLES20.glViewport(0, 0, i, i2);
        resize(i, i2);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(16640);
        step();
    }

    @Override // android.opengl.GLSurfaceView, android.view.SurfaceView, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        imgui_Shutdown();
    }
}
