package com.kk.speech.aiui.callback;

/**
 * 语音播报状态回调
 * Created by onlykk on 2024-12-08
 */
public interface ITTSCallBack {
    /**
     * 语音播报开始
     */
    void onSpeakBegin();

    /**
     * 语音播报结束
     */
    void onCompleted();

    /**
     * 语音播报错误
     *
     * @param error 错误信息
     */
    void onError(String error);

    /**
     * 语音暂停播报
     */
    void onPause(boolean isUser);

    /**
     * 语音继续播报
     */
    void onResume(boolean isUser);

    /**
     * 语音播报进度
     *
     * @param percent 当前播报的进度百分比
     */
    void onProgress(int percent);
}
