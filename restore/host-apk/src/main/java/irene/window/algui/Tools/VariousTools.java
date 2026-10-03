package irene.window.algui.Tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import java.util.Locale;

/* loaded from: classes.dex */
public class VariousTools {
    public static final String TAG = "VariousTools";
    private static TextToSpeech textToSpeech;

    public static boolean copyToClipboard(Context context, String str) {
        if (context == null || str == null) {
            return false;
        }
        ((ClipboardManager) context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Copied Text", str));
        return true;
    }

    public static boolean gotoWeb(Context context, String str) {
        if (context == null) {
            return false;
        }
        if (str == null) {
            str = "https://cn.bing.com/";
        }
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
        return true;
    }

    public static boolean joinQQGroup(Context context, String str) {
        if (context == null) {
            return false;
        }
        Intent intent = new Intent();
        intent.setData(Uri.parse(new StringBuffer().append("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D").append(str).toString()));
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean convertTextToSpeech(Context context, String str, Locale locale) {
        if (context == null || str == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction("android.speech.tts.engine.CHECK_TTS_DATA");
        if (packageManager.resolveActivity(intent, 65536) != null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener(locale, str, context) { // from class: irene.window.algui.Tools.VariousTools.100000000
                private final Locale val$language;
                private final Context val$tContext;
                private final String val$text;

                {
                    this.val$language = locale;
                    this.val$text = str;
                    this.val$tContext = context;
                }

                @Override // android.speech.tts.TextToSpeech.OnInitListener
                public void onInit(int i) {
                    Locale locale2;
                    if (i == 0) {
                        VariousTools.textToSpeech.setEngineByPackageName(VariousTools.textToSpeech.getDefaultEngine());
                        if (this.val$language != null) {
                            locale2 = this.val$language;
                        } else {
                            locale2 = Locale.getDefault();
                        }
                        if (VariousTools.textToSpeech.isLanguageAvailable(locale2) == 0) {
                            VariousTools.textToSpeech.setLanguage(locale2);
                            VariousTools.textToSpeech.speak(this.val$text, 0, null, null);
                            return;
                        }
                        Toast.makeText(this.val$tContext, "该语言无法识别此内容", 1).show();
                    }
                }
            });
            textToSpeech.shutdown();
            return true;
        }
        Toast.makeText(context, "未安装系统自带的TTS引擎", 1).show();
        return false;
    }
}
