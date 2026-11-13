
package com.lyricdrive.app;

import android.service.notification.NotificationListenerService;
import android.media.session.MediaSessionManager;
import android.media.session.MediaController;
import android.media.MediaMetadata;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

public class MusicNotificationListener
        extends NotificationListenerService {

    private static final String TAG = "LyricDrive";

    private MediaSessionManager sessionManager;
    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {
            scanActiveSessions();
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        sessionManager = (MediaSessionManager)
                getSystemService(
                    Context.MEDIA_SESSION_SERVICE
                );

        handler.removeCallbacks(scanner);
        handler.post(scanner);
    }

    @Override
    public void onListenerDisconnected() {
        handler.removeCallbacks(scanner);
        super.onListenerDisconnected();
    }

    private void scanActiveSessions() {

        if (sessionManager == null) return;

        try {
            ComponentName component =
                    new ComponentName(
                        this,
                        MusicNotificationListener.class
                    );

            List<MediaController> controllers =
                    sessionManager.getActiveSessions(component);

            for (MediaController controller : controllers) {

                if (controller.getPlaybackState() == null)
                    continue;

                if (controller.getPlaybackState().getState()
                        != android.media.session.PlaybackState.STATE_PLAYING)
                    continue;

                MediaMetadata metadata =
                        controller.getMetadata();

                if (metadata == null) continue;

                String title = metadata.getString(
                    MediaMetadata.METADATA_KEY_TITLE
                );

                String artist = metadata.getString(
                    MediaMetadata.METADATA_KEY_ARTIST
                );

                if (title != null && !title.isEmpty()) {
                    Log.d(TAG, "Now playing: "
                            + title + " - " + artist);
                    return;
                }
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Media access denied", e);

        } catch (Exception e) {
            Log.e(TAG, "Media detection error", e);
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(scanner);
        super.onDestroy();
    }
}
