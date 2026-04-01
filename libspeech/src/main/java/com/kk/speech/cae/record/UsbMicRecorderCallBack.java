package com.kk.speech.cae.record;

public interface UsbMicRecorderCallBack {

     void onAudioDataChanged(byte[] data, int len);
}
