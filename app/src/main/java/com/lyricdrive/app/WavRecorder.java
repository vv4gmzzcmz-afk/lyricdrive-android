package com.lyricdrive.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavRecorder {

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int BITS = 16;
    private static final int DURATION_MS = 10000;

    public static File record(Context context)
            throws Exception {

        if (context.checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException(
                "Microphone permission required");
        }

        int minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        if (minBuffer <= 0) {
            throw new IOException(
                "Unsupported audio configuration");
        }

        int bufferSize = Math.max(minBuffer * 2, 4096);

        AudioRecord recorder = null;
        File wavFile = new File(
            context.getFilesDir(), "radio_sample.wav"
        );

        boolean completed = false;

        try {
            recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            );

            if (recorder.getState() !=
                    AudioRecord.STATE_INITIALIZED) {
                throw new IOException(
                    "AudioRecord initialization failed");
            }

            try (FileOutputStream output =
                    new FileOutputStream(wavFile)) {

                // Reserve 44 bytes for the WAV header.
                output.write(new byte[44]);

                byte[] buffer = new byte[bufferSize];
                long totalBytes = 0;

                recorder.startRecording();

                if (recorder.getRecordingState() !=
                        AudioRecord.RECORDSTATE_RECORDING) {
                    throw new IOException(
                        "Recording could not start");
                }

                long endTime =
                    SystemClock.elapsedRealtime()
                    + DURATION_MS;

                while (SystemClock.elapsedRealtime()
                        < endTime) {

                    int read = recorder.read(
                        buffer, 0, buffer.length
                    );

                    if (read < 0) {
                        throw new IOException(
                            "Audio read error: " + read);
                    }

                    if (read == 0) continue;

                    output.write(buffer, 0, read);
                    totalBytes += read;
                }

                if (totalBytes == 0) {
                    throw new IOException(
                        "No audio data recorded");
                }

                if (totalBytes > 0xFFFFFFFFL - 36) {
                    throw new IOException(
                        "WAV file too large");
                }

                output.flush();
                writeWavHeader(wavFile, totalBytes);
                completed = true;
            }

            return wavFile;

        } finally {
            if (recorder != null) {
                try {
                    if (recorder.getRecordingState() ==
                            AudioRecord.RECORDSTATE_RECORDING) {
                        recorder.stop();
                    }
                } catch (Exception ignored) {
                }
                recorder.release();
            }

            if (!completed && wavFile.exists()) {
                wavFile.delete();
            }
        }
    }

    private static void writeWavHeader(
            File file, long dataSize) throws IOException {

        try (java.io.RandomAccessFile wav =
                new java.io.RandomAccessFile(file, "rw")) {

            wav.seek(0);

            wav.writeBytes("RIFF");
            writeIntLE(wav, 36 + dataSize);
            wav.writeBytes("WAVE");

            wav.writeBytes("fmt ");
            writeIntLE(wav, 16);
            writeShortLE(wav, 1);
            writeShortLE(wav, CHANNELS);
            writeIntLE(wav, SAMPLE_RATE);

            int byteRate =
                SAMPLE_RATE * CHANNELS * BITS / 8;

            writeIntLE(wav, byteRate);
            writeShortLE(wav, CHANNELS * BITS / 8);
            writeShortLE(wav, BITS);

            wav.writeBytes("data");
            writeIntLE(wav, dataSize);
        }
    }

    private static void writeIntLE(
            java.io.RandomAccessFile file,
            long value) throws IOException {

        file.write((int)(value & 0xFF));
        file.write((int)((value >> 8) & 0xFF));
        file.write((int)((value >> 16) & 0xFF));
        file.write((int)((value >> 24) & 0xFF));
    }

    private static void writeShortLE(
            java.io.RandomAccessFile file,
            int value) throws IOException {

        file.write(value & 0xFF);
        file.write((value >> 8) & 0xFF);
    }
}
