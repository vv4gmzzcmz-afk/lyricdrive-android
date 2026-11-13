
package com.lyricdrive.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView songTitle;
    private TextView artistName;
    private TextView lyricsText;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        createInterface();
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 12, 24, 12);
        root.setBackgroundColor(Color.rgb(10, 15, 25));

        TextView header = makeText(
            "♫  LYRICDRIVE", 18, "#38BDF8"
        );
        header.setGravity(Gravity.CENTER);
        root.addView(header);

        songTitle = makeText(
            "No song detected", 24, "#FFFFFF"
        );
        songTitle.setGravity(Gravity.CENTER);
        root.addView(songTitle);

        artistName = makeText(
            "Waiting for music...", 15, "#94A3B8"
        );
        artistName.setGravity(Gravity.CENTER);
        root.addView(artistName);

        lyricsText = makeText(
            "♪\n\nPlay a song to display lyrics",
            22, "#FFFFFF"
        );
        lyricsText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lyricsParams =
            new LinearLayout.LayoutParams(
                -1, 0, 1f
            );

        root.addView(lyricsText, lyricsParams);

        statusText = makeText(
            "LyricDrive v0.1 | Ready",
            12, "#64748B"
        );
        statusText.setGravity(Gravity.CENTER);
        root.addView(statusText);

        setContentView(root);
    }

    private TextView makeText(
        String text, float size, String color
    ) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.parseColor(color));
        view.setPadding(4, 8, 4, 8);
        return view;
    }

    public void updateSong(
        String title, String artist
    ) {
        runOnUiThread(() -> {
            songTitle.setText(title);
            artistName.setText(artist);
        });
    }

    public void updateLyrics(String lyrics) {
        runOnUiThread(() -> {
            lyricsText.setText(lyrics);
        });
    }

    public void updateStatus(String status) {
        runOnUiThread(() -> {
            statusText.setText(status);
        });
    }
}
