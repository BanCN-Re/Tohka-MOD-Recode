package irene.window.algui.Tools;

import android.text.TextUtils;
import android.util.Base64;
import java.io.UnsupportedEncodingException;

/* loaded from: classes.dex */
public class DataTool {
    public static final String TAG = "DataTool";

    private String base64Encode(String str, String str2) {
        if (TextUtils.isEmpty(str2)) {
            str2 = "UTF-8";
        }
        try {
            return Base64.encodeToString(str.getBytes(str2), 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String base64Decode(String str, String str2) {
        if (TextUtils.isEmpty(str2)) {
            str2 = "UTF-8";
        }
        try {
            return new String(Base64.decode(str, 0), str2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
