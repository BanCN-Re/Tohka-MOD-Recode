package irene.window.algui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    public static Context context;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // 还原修正: jadx 把布局 ID 误还原成 R.attr（setContentView 参数必须是 layout）
        // 该类是 AlGui 的示例 Activity，宿主未引用，这里不设布局
        Loading.start(this);
    }
}
