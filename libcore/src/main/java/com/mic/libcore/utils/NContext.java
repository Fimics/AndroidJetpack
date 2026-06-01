package com.mic.libcore.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.kk.core.utils.KLog;
import com.mic.libcore.constant.ArmType;


public class NContext {
    private static final String TAG = "NContext";
    // 机器人软件ID
    public static String ROBOT_ID;
    public static ArmType ARM_TYPE;
    private String robotSn;
    private String sessionId;
    private String clientId;
    private String lastMessageId;
    private String lastDts;
    public static boolean isUseExtMic = true;

    //Vivi 2.0
    private String timbreName="zh_female_vv_uranus_bigtts";
    private int timbreSpeed=50;

    private NContext() {}

    // 静态内部类持有实例
    private static class Holder {
        private static final NContext INSTANCE = new NContext();
    }

    // 获取唯一实例
    public static NContext getInstance() {
        return Holder.INSTANCE;
    }

    public String getRobotSn(){
        if (TextUtils.isEmpty(robotSn)){
//            robotSn = SerialNumber.getSN();
        }
        return robotSn;
    }

    @SuppressLint("HardwareIds")
    public void init(String appid, ArmType armType) {
        KLog.d(TAG, "init appid:" + appid + " armType:" + armType);
        ROBOT_ID = appid;
        ARM_TYPE = armType;
    }

    // ===== Getter / Setter =====

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastDts() {
        return lastDts;
    }

    public void setLastDts(String lastDts) {
        this.lastDts = lastDts;
    }
    public String getTimbreName() {
        return timbreName;
    }

    public void setTimbreName(String timbreName) {
        this.timbreName = timbreName;
    }

    public int getTimbreSpeed() {
        return timbreSpeed;
    }

    public void setTimbreSpeed(int timbreSpeed) {
        this.timbreSpeed = timbreSpeed;
    }

}
