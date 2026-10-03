package com.Riruriru.Sx;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/* loaded from: classes3.dex */
public class FloatStartService {
    private static Context mContext;

    public static void load(Context context) {
        mContext = context;
        applyPermission();
        mContext.startService(new Intent(mContext, (Class<?>) FloatServiceView.class));
    }

    private static void applyPermission() {
        if (!Settings.canDrawOverlays(mContext)) {
            Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        }
    }
}
