package com.mic.nav.player;

import androidx.annotation.NonNull;

import com.noetix.libnoetix.entity.AudioFrame;

public class NXPlayer {

    private static final String TAG = "NXPlayer";
    private final AudioPlayer audioPlayer;

    private NXPlayer() {
        audioPlayer = new AudioPlayer();
    }

    private static final class Holder {
        private static final NXPlayer instance = new NXPlayer();
    }

    public static NXPlayer getInstance() {
        return Holder.instance;
    }

    public void start() {
        audioPlayer.start();
    }

    public void stop() {
        audioPlayer.stop();
    }

    public void enqueueFrame(@NonNull AudioFrame frame) {
        audioPlayer.enqueueFrame(frame);
    }

    public void interrupt() {
        audioPlayer.emergencyStop("NXPlayer");
    }
}
