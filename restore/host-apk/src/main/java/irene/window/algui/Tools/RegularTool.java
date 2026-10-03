package irene.window.algui.Tools;

import android.text.TextUtils;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
public class RegularTool {
    private static final String REGEX_CHZ = "^[\\u4e00-\\u9fa5]+$";
    private static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    private static final String REGEX_HTML_TEXT = ".*<.+>.*<\\/.+>.*";
    private static final String REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
    private static final String REGEX_MOBILE_EXACT = "^((13[0-9])|(14[5,7])|(15[0-3,5-8])|(17[0,3,5-8])|(18[0-9])|(147))\\d{8}$";
    private static final String REGEX_MOBILE_SIMPLE = "^[1]\\d{10}$";
    private static final String REGEX_TEL = "^0\\d{2,3}[- ]?\\d{7,8}";
    private static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?";
    private static final String REGEX_USERNAME = "^[\\w\\u4e00-\\u9fa5]{6,20}(?<!_)$";
    public static final String TAG = "RegularTool";

    public static boolean isHtmlText(String str) {
        return isMatch(REGEX_HTML_TEXT, str);
    }

    public static boolean isMobileSimple(String str) {
        return isMatch(REGEX_MOBILE_SIMPLE, str);
    }

    public static boolean isMobileExact(String str) {
        return isMatch(REGEX_MOBILE_EXACT, str);
    }

    public static boolean isTel(String str) {
        return isMatch(REGEX_TEL, str);
    }

    public static boolean isEmail(String str) {
        return isMatch(REGEX_EMAIL, str);
    }

    public static boolean isURL(String str) {
        return isMatch(REGEX_URL, str);
    }

    public static boolean isChz(String str) {
        return isMatch(REGEX_CHZ, str);
    }

    public static boolean isUsername(String str) {
        return isMatch(REGEX_USERNAME, str);
    }

    public static boolean isMatch(String str, String str2) {
        return !TextUtils.isEmpty(str2) && Pattern.matches(str, str2);
    }
}
