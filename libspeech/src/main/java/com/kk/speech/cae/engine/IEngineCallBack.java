package com.kk.speech.cae.engine;

import com.kk.speech.cae.bean.WakeInfo;

public interface IEngineCallBack {
    void onWakeup(WakeInfo wakeInfo);


    void onAudioCallback(byte[] bytes, int len);
}
