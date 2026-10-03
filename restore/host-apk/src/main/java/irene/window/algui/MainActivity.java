package irene.window.algui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/* loaded from: classes.dex
 *
 * AlGui 框架自带的示例 Activity。
 *
 * 还原说明：原始反编译结果是
 *     setContentView(com.Riruriru.Sx.R.attr.actionBarDivider);
 * 这是 jadx 的还原错误 —— setContentView(int) 要的是 layout 资源 ID，
 * 不可能传 attr。原意应为某个示例布局或 0。
 * 该类在宿主里没有被任何地方引用，仅作框架示例存在，这里改为不设布局。
 */
public class MainActivity extends Activity {
    public static Context context;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;
        Loading.start(this);
    }
}
