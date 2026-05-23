package com.mic.libcore.action.callback;

/**
 * 触摸唤醒回调
 * Created by onlykk on 2025-01-08
 */
public interface IWakeUpCallBack {
    /**
     * 触摸唤醒
     *
     * @param type 唤醒类型:{TL}左臂,{TR}右臂
     */
    void onWakeUp(String type);
}
