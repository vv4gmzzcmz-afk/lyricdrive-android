package com.lyricdrive.app;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int MIC_REQUEST = 101;

    private TextView songTitle;
    private TextView artistName;
    private TextView lyricsText;
    private TextView statusText;
    private Button micButton;

    private volatile boolean testing = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

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
            "♫ LYRICDRIVE v0.2", 18, "#38BDF8"
        );
        root.addView(header);

        songTitle = makeText(
            "Microphone Test", 24, "#FFFFFF"
        );
        root.addView(songTitle);

        artistName = makeText(
            "Radio FM Recognition Setup",
            15, "#94A3B8"
        );
        root.addView(artistName);

        lyricsText = makeText(
            "Tap the button to test microphone",
            20, "#FFFFFF"
        );
        lyricsText.setGravity(Gravity.CENTER);

        root.addView(
            lyricsText,
            new LinearLayout.LayoutParams(-1, 0, 1f)
        );

        micButton = new Button(this);
        micButton.setText("TEST MICROPHONE");
        micButton.setOnClickListener(v ->
            checkMicrophonePermission()
        );
        root.addView(micButton);

        statusText = makeText(
            "Ready to test microphone",
            14, "#94A3B8"
        );
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
        view.setGravity(Gravity.CENTER);
        view.setPadding(4, 8, 4, 8);
        return view;
    }

    private void checkMicrophonePermission() {

        if (testing) return;

        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{
                    Manifest.permission.RECORD_AUDIO
                },
                MIC_REQUEST
            );
            return;
        }

        startMicrophoneTest();
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        );

        if (requestCode == MIC_REQUEST) {
            if (grantResults.length > 0 &&
                grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                startMicrophoneTest();

            } else {
                updateStatus("Microphone permission denied");
            }
        }
    }

    
    private void startMicrophoneTest() {

        if (testing) return;

        testing = true;
        micButton.setEnabled(false);

        updateStatus("Recording WAV for 10 seconds...");

        new Thread(() -> {

            String result;

            try {

                java.io.File wavFile =
                    WavRecorder.record(
                        getApplicationContext()
                    );

                long fileSize = wavFile.length();

                if (fileSize <= 44) {
                    result = "Recording failed: Empty WAV";
                } else {
                    result = "WAV saved: "
                        + wavFile.getName()
                        + " (" + fileSize
                        + " bytes)";
                }

            } catch (Exception e) {

                result = "Recording error: "
                    + e.getMessage();

            }

            final String message = result;

            runOnUiThread(() -> {

                testing = false;

                micButton.setEnabled(true);

                updateStatus(message);

            });

        }).start();
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
        runOnUiThread(() ->
            lyricsText.setText(lyrics)
        );
    }

    public void updateStatus(String status) {
        runOnUiThread(() ->
            statusText.setText(status)
        );
    }
}
