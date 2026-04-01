package com.kk.speech.aiui.callback;

/**
 * 语音识别状态和数据回调
 * Created by onlykk on 2024-12-08
 */
public interface IASRCallBack {
    /**
     * 识别道开始说话
     */
    void onBeginOfSpeech();

    /**
     * 识别到音量变化
     *
     * @param volume 音量级别
     */
    void onVolumeChanged(int volume);

    /**
     * 动态修正数据
     *
     * @param result 语音识别动态修正数据
     */
    void dynamicResult(String result);

    /**
     * 识别到说话结束
     *
     * @param result 语音识别最终结果
     * @param error  错误信息
     */
    void onEndOfSpeech(String result, String error);
}
