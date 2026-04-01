package com.kk.speech.wakeup.callback;

import com.noetix.libcore.constant.WakeUpType;

/**
 * 唤醒回调
 * Created by onlykk on 2025-04-01
 */
public interface IWakeUpCallBack {
    /**
     * 唤醒
     */
    void onWakeUp(WakeUpType type, int angle);
}
