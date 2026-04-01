package com.kk.speech.wakeup;

import com.kk.speech.aiui.AIUISDK;
import com.kk.speech.wakeup.callback.IWakeUpCallBack;
import com.noetix.libcore.constant.WakeUpType;

public class WakeUpSDK {
    private static volatile WakeUpSDK mInstance;
    // 唤醒回调
    private IWakeUpCallBack iWakeUpCallBack;

    public static synchronized WakeUpSDK getInstance() {
        if (mInstance == null) {
            synchronized (WakeUpSDK.class) {
                if (mInstance == null) {
                    mInstance = new WakeUpSDK();
                }
            }
        }
        return mInstance;
    }

    /**
     * 关闭唤醒
     */
    public void stopWakeUp() {
        AIUISDK.getInstance().stopWakeUpCallBack();
    }

    /**
     * 开启唤醒
     */
    public void startWakeUp() {
        AIUISDK.getInstance().setWakeUpCallBack((angle, beam) -> {
            if (iWakeUpCallBack != null) {
                iWakeUpCallBack.onWakeUp(WakeUpType.VOICE, angle);
            }
        });
    }

    /**
     * 设置唤醒回调
     *
     * @param callBack 唤醒结果回调
     */
    public void setWakeUpCallBack(IWakeUpCallBack callBack) {
        this.iWakeUpCallBack = callBack;
    }

}
