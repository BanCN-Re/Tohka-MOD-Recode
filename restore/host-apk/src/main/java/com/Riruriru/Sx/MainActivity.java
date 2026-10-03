package com.Riruriru.Sx;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.location.LocationRequestCompat;

/* loaded from: classes3.dex */
public class MainActivity extends Activity {
    @Override // android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatStartService.load(this);
        m86();
        Button btnQq = (Button) findViewById(R.id.btn_qq_group);
        btnQq.setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.MainActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                boolean success = MainActivity.this.joinQQGroup("ptE_5bcttkaXYz4YxpE9OH7oD8Th-o3Y");
                if (!success) {
                    Toast.makeText(MainActivity.this, "未安装QQ或版本过低", 0).show();
                }
            }
        });
        findViewById(R.id.btn_bilibili).setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.MainActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.openUrl("https://space.bilibili.com/1742678512");
            }
        });
        findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() { // from class: com.Riruriru.Sx.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.openUrl("https://shop.xiaoman.top/links/50F4486F");
            }
        });
    }

    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openUrl(String url) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开链接", 0).show();
        }
    }

    /* renamed from: 储存权限, reason: contains not printable characters */
    public void m86() {
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY);
        }
    }
}
