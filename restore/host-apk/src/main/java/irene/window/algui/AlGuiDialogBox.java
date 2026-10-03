package irene.window.algui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import irene.window.algui.AlGui;
import irene.window.algui.AlGuiData;
import irene.window.algui.CustomizeView.vLinearLayout;
import irene.window.algui.Tools.AppPermissionTool;

/* loaded from: classes.dex */
public class AlGuiDialogBox {
    public static final String TAG = "AlGuiDialogBox";
    private static AlertDialog dialog;

    public static AlertDialog showDiaLog(Context context, int i, float f, View... viewArr) {
        if (context == null) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(f);
        gradientDrawable.setColor(i);
        vLinearLayout vlinearlayout = new vLinearLayout(context);
        vlinearlayout.setOrientation(1);
        vlinearlayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 100.0f));
        vlinearlayout.setBackground(gradientDrawable);
        vlinearlayout.setPadding(40, 40, 40, 40);
        for (View view : viewArr) {
            if (view != null) {
                view.getId();
                AlGuiData.AlguiView.SmallButton.getId();
                vlinearlayout.addView(view);
            }
        }
        builder.setView(vlinearlayout);
        AlertDialog create = builder.create();
        create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        create.getWindow().addFlags(AlGuiData.getLiveStreamFlags());
        if (!AppPermissionTool.isAndroidManifestPermissionExist(context, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Toast.makeText(context, "开发者未在AndroidManifest.xml文件中声明悬浮窗权限因此无法显示对话框，只有声明了此权限才能显示对话框！你也可以手动去安装包此文件中声明android.permission.SYSTEM_ALERT_WINDOW", 1).show();
        } else if (AppPermissionTool.checkOverlayPermission(context)) {
            create.getWindow().setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2003);
            create.show();
        }
        return create;
    }

    public static AlertDialog showTextDiaLog(Context context, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        if (context != null) {
            dialog = showDiaLog(context, -822412550, 50.0f, AlGui.GUI(context).addLinearLayout(17, 1, -1, -2, AlGui.GUI(context).addTextView(charSequence, 18, -14606047, Typeface.create(Typeface.DEFAULT, 1)), AlGui.GUI(context).addTextView(charSequence2, 15.0f, -12434878, null)), AlGui.GUI(context).addButton(charSequence3, 15, -1, null, 50, -12627531, 0, ViewCompat.MEASURED_STATE_MASK, -1, -2, new AlGui.T_ButtonOnChangeListener() { // from class: irene.window.algui.AlGuiDialogBox.100000000
                @Override // irene.window.algui.AlGui.T_ButtonOnChangeListener
                public void onClick(View view, GradientDrawable gradientDrawable, TextView textView, boolean z) {
                    AlGuiDialogBox.dialog.dismiss();
                }
            }));
            return dialog;
        }
        return null;
    }
}
