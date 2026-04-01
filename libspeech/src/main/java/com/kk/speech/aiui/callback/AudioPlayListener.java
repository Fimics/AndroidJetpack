package com.kk.speech.aiui.callback;

public interface AudioPlayListener {
    void onPlaybackBegin();

    void onPlaybackCompleted();

    void onPlaybackError(String error);
}
