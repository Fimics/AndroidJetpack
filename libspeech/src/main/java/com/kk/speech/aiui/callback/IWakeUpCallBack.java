package com.kk.speech.aiui.callback;

/**
 * 语音唤醒回调
 * Created by onlykk on 2024-12-08
 */
public interface IWakeUpCallBack {
    /**
     * 被语音唤醒词唤醒
     *
     * @param angle 唤醒声音来源角度:正前方为0,逆时针0-360度
     * @param beam  唤醒声音来源麦克风序号(业务层无需关注)
     */
    void onWakeUp(int angle, int beam);
}
