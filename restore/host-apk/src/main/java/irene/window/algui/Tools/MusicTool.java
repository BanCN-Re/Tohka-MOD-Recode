package irene.window.algui.Tools;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import java.io.IOException;

/* loaded from: classes.dex */
public class MusicTool {
    public static final String TAG = "MusicTool";
    private static MediaPlayer mediaPlayer;

    public static void playAssetMp3(Context context, String str) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            mediaPlayer.reset();
        }
        try {
            AssetFileDescriptor openFd = context.getAssets().openFd(str);
            mediaPlayer.setDataSource(openFd.getFileDescriptor(), openFd.getStartOffset(), openFd.getLength());
            mediaPlayer.setAudioStreamType(3);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, new StringBuffer().append("Failed to play asset mp3: ").append(e.getMessage()).toString());
        }
    }
}
