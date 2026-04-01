package com.kk.speech.cae.record;

public interface IAudioCallBack {
    void onAudio(byte[] data, int len);
}
