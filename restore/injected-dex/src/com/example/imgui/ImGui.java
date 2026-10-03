package com.example.imgui;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: D:\Work\MuMuP\ArkReCodeHack\build\dex\libArkRe.injected.dex */
public class ImGui {
    private static final String IMGUI_VIEW_TAG = "imgui_overlay_view";
    private static final int MAX_POINTERS = 10;
    private static final String TAG = "ImGui";
    public static String cardKeyText = "";
    public static String classFilterText = "";
    private static GLES3JNIView display = null;
    public static String objectFilterText = "";
    public static String objectNameFilterText = "";
    public static String roleFilterText = "";
    public static int selectedFilterType;
    private WeakReference<Activity> currentActivityRef;
    private boolean[] pointerInImGui = new boolean[MAX_POINTERS];
    private boolean[] pointerInButton = new boolean[MAX_POINTERS];
    private Map<Integer, Integer> gamePointerIdMap = new HashMap();
    private List<Integer> activeGamePointers = new ArrayList();
    private long gameDownTime = 0;
    private int imguiPointerCount = 0;

    private static native float[] nativeGetCircularButtonBounds();

    private static native float[] nativeGetImGuiWindowBounds();

    private void initPointerTracking() {
        for (int i = 0; i < MAX_POINTERS; i++) {
            this.pointerInImGui[i] = false;
            this.pointerInButton[i] = false;
        }
        this.imguiPointerCount = 0;
        this.gamePointerIdMap.clear();
        this.activeGamePointers.clear();
        this.gameDownTime = 0L;
    }

    public static void setupImGuiViewOnMainThread(final Activity activity) {
        Log.i(TAG, "setupImGuiViewOnMainThread called");
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.i(TAG, "Already on main thread, executing directly");
            new ImGui().setupImGuiView(activity);
        } else {
            Log.i(TAG, "Not on main thread, using Handler to switch");
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.example.imgui.ImGui.1
                @Override // java.lang.Runnable
                public void run() {
                    Log.i(ImGui.TAG, "Now on main thread, executing setupImGuiViewImpl");
                    new ImGui().setupImGuiView(activity);
                }
            });
        }
    }

    public static void setSelectedFilterType(int i) {
        selectedFilterType = i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupImGuiView(final Activity activity) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.example.imgui.ImGui.2
                @Override // java.lang.Runnable
                public void run() {
                    ImGui.this.setupImGuiViewInternal(activity);
                }
            });
        } else {
            setupImGuiViewInternal(activity);
        }
    }

    public static void showDirectInputMethodFromNative() {
        try {
            GLES3JNIView gLES3JNIView = display;
            if (gLES3JNIView != null) {
                Context context = gLES3JNIView.getContext();
                if (context instanceof Activity) {
                    final Activity activity = (Activity) context;
                    if (activity.isFinishing()) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() { // from class: com.example.imgui.ImGui.3
                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                final EditText editText = new EditText(activity);
                                editText.setVisibility(0);
                                editText.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
                                String str = "";
                                int i = ImGui.selectedFilterType;
                                if (i == 1) {
                                    str = ImGui.objectFilterText;
                                } else if (i == 2) {
                                    str = ImGui.classFilterText;
                                } else if (i == 3) {
                                    str = ImGui.objectNameFilterText;
                                } else if (i == 4) {
                                    str = ImGui.cardKeyText;
                                } else if (i == 5) {
                                    str = ImGui.roleFilterText;
                                }
                                editText.setText(str);
                                editText.setSelection(str.length());
                                final ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
                                viewGroup.addView(editText);
                                editText.addTextChangedListener(new TextWatcher() { // from class: com.example.imgui.ImGui.3.1
                                    @Override // android.text.TextWatcher
                                    public void afterTextChanged(Editable editable) {
                                    }

                                    @Override // android.text.TextWatcher
                                    public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                                    }

                                    @Override // android.text.TextWatcher
                                    public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
                                        String charSequence2 = charSequence.toString();
                                        int i5 = ImGui.selectedFilterType;
                                        if (i5 == 1) {
                                            ImGui.objectFilterText = charSequence2;
                                            GLES3JNIView.nativeUpdateFilterText(1, charSequence2);
                                            return;
                                        }
                                        if (i5 == 2) {
                                            ImGui.classFilterText = charSequence2;
                                            GLES3JNIView.nativeUpdateFilterText(2, charSequence2);
                                            return;
                                        }
                                        if (i5 == 3) {
                                            ImGui.objectNameFilterText = charSequence2;
                                            GLES3JNIView.nativeUpdateFilterText(3, charSequence2);
                                        } else if (i5 == 4) {
                                            ImGui.cardKeyText = charSequence2;
                                            GLES3JNIView.nativeUpdateFilterText(4, charSequence2);
                                        } else {
                                            if (i5 != 5) {
                                                return;
                                            }
                                            ImGui.roleFilterText = charSequence2;
                                            GLES3JNIView.nativeUpdateFilterText(5, charSequence2);
                                        }
                                    }
                                });
                                editText.requestFocus();
                                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
                                if (inputMethodManager != null) {
                                    inputMethodManager.showSoftInput(editText, 1);
                                }
                                editText.setOnKeyListener(new View.OnKeyListener() { // from class: com.example.imgui.ImGui.3.2
                                    @Override // android.view.View.OnKeyListener
                                    public boolean onKey(View view, int i2, KeyEvent keyEvent) {
                                        if (i2 != 66 || keyEvent.getAction() != 1) {
                                            return false;
                                        }
                                        new Handler().postDelayed(new Runnable() { // from class: com.example.imgui.ImGui.3.2.1
                                            @Override // java.lang.Runnable
                                            public void run() {
                                                editText.clearFocus();
                                                viewGroup.removeView(editText);
                                            }
                                        }, 0L);
                                        return true;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupImGuiViewInternal(Activity activity) {
        try {
            initPointerTracking();
            final FrameLayout frameLayout = (FrameLayout) activity.findViewById(R.id.content);
            if (frameLayout == null) {
                Log.i(TAG, "ImGui Hook: rootView is null");
                return;
            }
            if (frameLayout.findViewWithTag(IMGUI_VIEW_TAG) != null) {
                return;
            }
            this.currentActivityRef = new WeakReference<>(activity);
            GLES3JNIView gLES3JNIView = new GLES3JNIView(activity);
            display = gLES3JNIView;
            gLES3JNIView.setTag(IMGUI_VIEW_TAG);
            display.setZOrderOnTop(true);
            display.getHolder().setFormat(-3);
            display.setOnTouchListener(new View.OnTouchListener() { // from class: com.example.imgui.ImGui.4
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return ImGui.this.handleTouchEvent(view, motionEvent, frameLayout);
                }
            });
            display.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            frameLayout.addView(display);
            Log.i(TAG, "ImGui Hook: Successfully attached to " + activity.getClass().getName());
        } catch (Exception e) {
            Log.i(TAG, "ImGui Hook Error: " + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean handleTouchEvent(View view, MotionEvent motionEvent, ViewGroup viewGroup) {
        int actionMasked = motionEvent.getActionMasked();
        int actionIndex = motionEvent.getActionIndex();
        int pointerCount = motionEvent.getPointerCount();
        int[] iArr = new int[pointerCount];
        float[] fArr = new float[pointerCount];
        float[] fArr2 = new float[pointerCount];
        float[] fArr3 = new float[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            iArr[i] = motionEvent.getPointerId(i);
            fArr[i] = motionEvent.getX(i);
            fArr2[i] = motionEvent.getY(i);
            fArr3[i] = motionEvent.getPressure(i);
        }
        GLES3JNIView.nativeOnTouchEvent(actionMasked, actionIndex, iArr, fArr, fArr2, fArr3, pointerCount);
        if (actionMasked == 0) {
            int pointerId = motionEvent.getPointerId(actionIndex);
            float x = motionEvent.getX(actionIndex);
            float y = motionEvent.getY(actionIndex);
            boolean isImGuiComponentTouched = isImGuiComponentTouched(x, y);
            boolean isCircularButtonTouched = isCircularButtonTouched(x, y);
            this.pointerInImGui[pointerId] = isImGuiComponentTouched;
            this.pointerInButton[pointerId] = isCircularButtonTouched;
            if (!isImGuiComponentTouched && !isCircularButtonTouched) {
                return false;
            }
            this.imguiPointerCount++;
            if (isImGuiComponentTouched) {
                GLES3JNIView.MotionEventClick(true, x, y);
            }
            return true;
        }
        if (actionMasked == 1) {
            int pointerId2 = motionEvent.getPointerId(actionIndex);
            float x2 = motionEvent.getX(actionIndex);
            float y2 = motionEvent.getY(actionIndex);
            boolean z = this.pointerInImGui[pointerId2];
            boolean z2 = this.pointerInButton[pointerId2];
            if (z) {
                GLES3JNIView.MotionEventClick(false, x2, y2);
                this.imguiPointerCount--;
            } else if (z2) {
                this.imguiPointerCount--;
            } else if (this.activeGamePointers.contains(Integer.valueOf(pointerId2))) {
                dispatchGameEvent(motionEvent, 1, pointerId2, viewGroup);
                removeGamePointer(pointerId2);
            }
            this.pointerInImGui[pointerId2] = false;
            this.pointerInButton[pointerId2] = false;
            if (this.imguiPointerCount == 0 && this.activeGamePointers.isEmpty()) {
                this.gameDownTime = 0L;
            }
            return z || z2;
        }
        if (actionMasked == 2) {
            for (int i2 = 0; i2 < pointerCount; i2++) {
                int pointerId3 = motionEvent.getPointerId(i2);
                float x3 = motionEvent.getX(i2);
                float y3 = motionEvent.getY(i2);
                if (this.pointerInImGui[pointerId3]) {
                    GLES3JNIView.MotionEventClick(true, x3, y3);
                }
            }
            if (!this.activeGamePointers.isEmpty()) {
                dispatchGameMoveEvent(motionEvent, viewGroup);
            }
            return this.imguiPointerCount > 0 || !this.activeGamePointers.isEmpty();
        }
        if (actionMasked == 3) {
            for (int i3 = 0; i3 < pointerCount; i3++) {
                int pointerId4 = motionEvent.getPointerId(i3);
                if (this.pointerInImGui[pointerId4]) {
                    GLES3JNIView.MotionEventClick(false, motionEvent.getX(i3), motionEvent.getY(i3));
                }
                this.pointerInImGui[pointerId4] = false;
                this.pointerInButton[pointerId4] = false;
            }
            if (!this.activeGamePointers.isEmpty()) {
                dispatchGameCancelEvent(motionEvent, viewGroup);
            }
            this.imguiPointerCount = 0;
            this.gamePointerIdMap.clear();
            this.activeGamePointers.clear();
            this.gameDownTime = 0L;
            return false;
        }
        if (actionMasked == 5) {
            int pointerId5 = motionEvent.getPointerId(actionIndex);
            float x4 = motionEvent.getX(actionIndex);
            float y4 = motionEvent.getY(actionIndex);
            boolean isImGuiComponentTouched2 = isImGuiComponentTouched(x4, y4);
            boolean isCircularButtonTouched2 = isCircularButtonTouched(x4, y4);
            this.pointerInImGui[pointerId5] = isImGuiComponentTouched2;
            this.pointerInButton[pointerId5] = isCircularButtonTouched2;
            if (isImGuiComponentTouched2 || isCircularButtonTouched2) {
                this.imguiPointerCount++;
                if (isImGuiComponentTouched2) {
                    GLES3JNIView.MotionEventClick(true, x4, y4);
                }
            } else {
                addGamePointer(pointerId5);
                dispatchGameEvent(motionEvent, 0, pointerId5, viewGroup);
            }
            return this.imguiPointerCount > 0 || this.activeGamePointers.size() > 0;
        }
        if (actionMasked != 6) {
            return false;
        }
        int pointerId6 = motionEvent.getPointerId(actionIndex);
        float x5 = motionEvent.getX(actionIndex);
        float y5 = motionEvent.getY(actionIndex);
        boolean z3 = this.pointerInImGui[pointerId6];
        boolean z4 = this.pointerInButton[pointerId6];
        if (z3) {
            GLES3JNIView.MotionEventClick(false, x5, y5);
            this.imguiPointerCount--;
        } else if (z4) {
            this.imguiPointerCount--;
        } else if (this.activeGamePointers.contains(Integer.valueOf(pointerId6))) {
            dispatchGameEvent(motionEvent, this.activeGamePointers.size() == 1 ? 1 : 6, pointerId6, viewGroup);
            removeGamePointer(pointerId6);
        }
        this.pointerInImGui[pointerId6] = false;
        this.pointerInButton[pointerId6] = false;
        return this.imguiPointerCount > 0 || !this.activeGamePointers.isEmpty();
    }

    private void addGamePointer(int i) {
        if (this.activeGamePointers.contains(Integer.valueOf(i))) {
            return;
        }
        this.gamePointerIdMap.put(Integer.valueOf(i), Integer.valueOf(this.activeGamePointers.size()));
        this.activeGamePointers.add(Integer.valueOf(i));
        if (this.activeGamePointers.size() == 1) {
            this.gameDownTime = SystemClock.uptimeMillis();
        }
    }

    private void removeGamePointer(int i) {
        this.activeGamePointers.remove(Integer.valueOf(i));
        this.gamePointerIdMap.remove(Integer.valueOf(i));
        this.gamePointerIdMap.clear();
        for (int i2 = 0; i2 < this.activeGamePointers.size(); i2++) {
            this.gamePointerIdMap.put(this.activeGamePointers.get(i2), Integer.valueOf(i2));
        }
    }

    private void dispatchGameEvent(MotionEvent motionEvent, int i, int i2, ViewGroup viewGroup) {
        int i3;
        int i4 = i;
        try {
            int size = this.activeGamePointers.size();
            if (size == 0) {
                return;
            }
            int indexOf = this.activeGamePointers.indexOf(Integer.valueOf(i2));
            if (indexOf == -1 && i4 == 0) {
                indexOf = size - 1;
            }
            MotionEvent.PointerProperties[] pointerPropertiesArr = new MotionEvent.PointerProperties[size];
            MotionEvent.PointerCoords[] pointerCoordsArr = new MotionEvent.PointerCoords[size];
            for (int i5 = 0; i5 < size; i5++) {
                int findPointerIndex = findPointerIndex(motionEvent, this.activeGamePointers.get(i5).intValue());
                pointerPropertiesArr[i5] = new MotionEvent.PointerProperties();
                pointerCoordsArr[i5] = new MotionEvent.PointerCoords();
                if (findPointerIndex >= 0) {
                    motionEvent.getPointerProperties(findPointerIndex, pointerPropertiesArr[i5]);
                    motionEvent.getPointerCoords(findPointerIndex, pointerCoordsArr[i5]);
                } else {
                    pointerPropertiesArr[i5].id = i5;
                    pointerPropertiesArr[i5].toolType = 1;
                    pointerCoordsArr[i5].x = 0.0f;
                    pointerCoordsArr[i5].y = 0.0f;
                    pointerCoordsArr[i5].pressure = 1.0f;
                    pointerCoordsArr[i5].size = 1.0f;
                }
                pointerPropertiesArr[i5].id = i5;
            }
            if (i4 == 0) {
                if (size == 1) {
                    i3 = 0;
                    MotionEvent obtain = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), i3, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
                    dispatchToChildren(viewGroup, obtain);
                    obtain.recycle();
                }
                i4 = (indexOf << 8) | 5;
                i3 = i4;
                MotionEvent obtain2 = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), i3, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
                dispatchToChildren(viewGroup, obtain2);
                obtain2.recycle();
            }
            if (i4 == 1) {
                i3 = 1;
                MotionEvent obtain22 = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), i3, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
                dispatchToChildren(viewGroup, obtain22);
                obtain22.recycle();
            }
            if (i4 == 6) {
                i4 = (indexOf << 8) | 6;
            }
            i3 = i4;
            MotionEvent obtain222 = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), i3, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
            dispatchToChildren(viewGroup, obtain222);
            obtain222.recycle();
        } catch (Exception e) {
            Log.i(TAG, "ImGui Hook: dispatchGameEvent error - " + e.getMessage());
        }
    }

    private void dispatchGameMoveEvent(MotionEvent motionEvent, ViewGroup viewGroup) {
        try {
            int size = this.activeGamePointers.size();
            if (size == 0) {
                return;
            }
            MotionEvent.PointerProperties[] pointerPropertiesArr = new MotionEvent.PointerProperties[size];
            MotionEvent.PointerCoords[] pointerCoordsArr = new MotionEvent.PointerCoords[size];
            for (int i = 0; i < size; i++) {
                int findPointerIndex = findPointerIndex(motionEvent, this.activeGamePointers.get(i).intValue());
                pointerPropertiesArr[i] = new MotionEvent.PointerProperties();
                pointerCoordsArr[i] = new MotionEvent.PointerCoords();
                if (findPointerIndex >= 0) {
                    motionEvent.getPointerProperties(findPointerIndex, pointerPropertiesArr[i]);
                    motionEvent.getPointerCoords(findPointerIndex, pointerCoordsArr[i]);
                }
                pointerPropertiesArr[i].id = i;
            }
            MotionEvent obtain = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), 2, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
            dispatchToChildren(viewGroup, obtain);
            obtain.recycle();
        } catch (Exception e) {
            Log.i(TAG, "ImGui Hook: dispatchGameMoveEvent error - " + e.getMessage());
        }
    }

    private void dispatchGameCancelEvent(MotionEvent motionEvent, ViewGroup viewGroup) {
        try {
            int size = this.activeGamePointers.size();
            if (size == 0) {
                return;
            }
            MotionEvent.PointerProperties[] pointerPropertiesArr = new MotionEvent.PointerProperties[size];
            MotionEvent.PointerCoords[] pointerCoordsArr = new MotionEvent.PointerCoords[size];
            for (int i = 0; i < size; i++) {
                int findPointerIndex = findPointerIndex(motionEvent, this.activeGamePointers.get(i).intValue());
                pointerPropertiesArr[i] = new MotionEvent.PointerProperties();
                pointerCoordsArr[i] = new MotionEvent.PointerCoords();
                if (findPointerIndex >= 0) {
                    motionEvent.getPointerProperties(findPointerIndex, pointerPropertiesArr[i]);
                    motionEvent.getPointerCoords(findPointerIndex, pointerCoordsArr[i]);
                }
                pointerPropertiesArr[i].id = i;
            }
            MotionEvent obtain = MotionEvent.obtain(this.gameDownTime, motionEvent.getEventTime(), 3, size, pointerPropertiesArr, pointerCoordsArr, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags());
            dispatchToChildren(viewGroup, obtain);
            obtain.recycle();
        } catch (Exception e) {
            Log.i(TAG, "ImGui Hook: dispatchGameCancelEvent error - " + e.getMessage());
        }
    }

    private int findPointerIndex(MotionEvent motionEvent, int i) {
        for (int i2 = 0; i2 < motionEvent.getPointerCount(); i2++) {
            if (motionEvent.getPointerId(i2) == i) {
                return i2;
            }
        }
        return -1;
    }

    private void dispatchToChildren(ViewGroup viewGroup, MotionEvent motionEvent) {
        for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = viewGroup.getChildAt(childCount);
            if (!IMGUI_VIEW_TAG.equals(childAt.getTag())) {
                try {
                    motionEvent.getX(0);
                    motionEvent.getY(0);
                    if (childAt.getVisibility() == 0) {
                        motionEvent.offsetLocation(-childAt.getLeft(), -childAt.getTop());
                        boolean dispatchTouchEvent = childAt.dispatchTouchEvent(motionEvent);
                        motionEvent.offsetLocation(childAt.getLeft(), childAt.getTop());
                        if (dispatchTouchEvent) {
                            return;
                        }
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    Log.i(TAG, "ImGui Hook: dispatchToChildren error - " + e.getMessage());
                }
            }
        }
    }

    public static boolean isImGuiComponentTouched(float f, float f2) {
        float[] nativeGetImGuiWindowBounds = nativeGetImGuiWindowBounds();
        if (nativeGetImGuiWindowBounds != null && nativeGetImGuiWindowBounds.length != 0) {
            int length = nativeGetImGuiWindowBounds.length / 4;
            for (int i = 0; i < length; i++) {
                int i2 = i * 4;
                float f3 = nativeGetImGuiWindowBounds[i2];
                float f4 = nativeGetImGuiWindowBounds[i2 + 1];
                float f5 = nativeGetImGuiWindowBounds[i2 + 2];
                float f6 = nativeGetImGuiWindowBounds[i2 + 3];
                if (f >= f3 && f <= f5 && f2 >= f4 && f2 <= f6) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCircularButtonTouched(float f, float f2) {
        float[] nativeGetCircularButtonBounds = nativeGetCircularButtonBounds();
        if (nativeGetCircularButtonBounds != null && nativeGetCircularButtonBounds.length != 0) {
            int i = (int) nativeGetCircularButtonBounds[0];
            for (int i2 = 0; i2 < i; i2++) {
                int i3 = i2 * 3;
                int i4 = i3 + 1;
                int i5 = i3 + 3;
                if (i5 >= nativeGetCircularButtonBounds.length) {
                    break;
                }
                float f3 = nativeGetCircularButtonBounds[i4];
                float f4 = nativeGetCircularButtonBounds[i3 + 2];
                float f5 = nativeGetCircularButtonBounds[i5];
                float f6 = f - f3;
                float f7 = f2 - f4;
                if ((f6 * f6) + (f7 * f7) <= f5 * f5) {
                    return true;
                }
            }
        }
        return false;
    }
}
