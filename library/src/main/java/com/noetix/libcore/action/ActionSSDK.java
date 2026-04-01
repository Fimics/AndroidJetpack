package com.noetix.libcore.action;

import com.noetix.libcore.action.support.ActionUtils;
import com.noetix.libcore.serial.constant.DataType;

public class ActionSSDK {
    private static volatile ActionSSDK mInstance;

    public static ActionSSDK getInstance() {
        if (mInstance == null) {
            synchronized (ActionSSDK.class) {
                if (mInstance == null) {
                    mInstance = new ActionSSDK();
                }
            }
        }
        return mInstance;
    }

    public ActionSSDK() {
        ActionUtils.getInstance().setCallBack(result -> {
        });
    }

    /**
     * 发送串口指令
     *
     * @param msg 指令信息
     */
    public void sendData(String msg) {
        ActionUtils.getInstance().sendData(msg, DataType.STRING);
    }
}
