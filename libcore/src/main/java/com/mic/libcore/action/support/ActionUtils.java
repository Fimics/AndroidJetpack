package com.mic.libcore.action.support;

import android.annotation.SuppressLint;
import android.text.TextUtils;


import com.kk.core.utils.KLog;
import com.mic.libcore.serial.SerialUtils;
import com.mic.libcore.serial.callback.ISerialCallBack;
import com.mic.libcore.serial.constant.Baudrate;
import com.mic.libcore.serial.constant.DataType;
import com.mic.libcore.utils.ByteUtils;
import org.json.JSONArray;

/**
 * 手臂串口操作工具类
 * (业务层无需关注)
 */
public class ActionUtils {
    private static volatile ActionUtils mInstance;
    // 串口操作工具类
    private SerialUtils mSerialUtils;
    // 唤醒回调监听
    private volatile ISerialCallBack mCallBack;

    /**
     * 获取串口工具单例
     */
    public static ActionUtils getInstance() {
        if (mInstance == null) {
            synchronized (ActionUtils.class) {
                if (mInstance == null) {
                    mInstance = new ActionUtils();
                }
            }
        }
        return mInstance;
    }

    @SuppressLint("CheckResult")
    private ActionUtils() {
        mSerialUtils = new SerialUtils("/dev/ttyS7", Baudrate.B9600);
        startReadData(DataType.STRING);
    }

    /**
     * 设置回调
     *
     * @param callBack 回调
     */
    public void setCallBack(ISerialCallBack callBack) {
        this.mCallBack = callBack;
    }

    /**
     * 移除回调监听
     */
    public void removeCallBack() {
        this.mCallBack = null;
    }

    /**
     * 向串口写数据
     *
     * @param order 要写的数据
     * @param type  写入的数据类型
     */
    public void sendData(String order, DataType type) {
        if (mSerialUtils == null) {
            return;
        }
        if (type == DataType.STRING) {
            mSerialUtils.setData(order.getBytes());
        } else if (type == DataType.HEX) {
            mSerialUtils.setData(ByteUtils.hexStr2bytes(order));
        }
        KLog.d("发送串口指令：" + order + "," + type);
    }

    /**
     * 开启串口指令接收
     */
    private void startReadData(DataType type) {
        // 开始读线程
        mSerialUtils.startReadData(result -> {
            if (!TextUtils.isEmpty(result)) {
                if (mCallBack != null) {
                    KLog.d("接收串口数据：" + result);
                    mCallBack.onResult(result);
                }
            }
        }, type);
    }

    /**
     * 格式化角度
     */
    public static String onFormatQAngle(String angle, int minLength) {
        return angle.length() < minLength ? "0" + angle : angle;
    }

    /**
     * H1手臂解析返回数据
     */
    public static String onAnalysisDataH1(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        if (data.contains("8B01")) {
            if (data.contains("FC550305")) {
                return "{TL1}";
            } else if (data.contains("FC558305")) {
                return "{TR1}";
            }
        }
        return "";
    }

    /**
     * Q1手臂生成角度指令
     */
    public static String onArmAngleQ1(boolean isLeft, int angle) {
        int convert = angle + 90;
        String format = onFormatQAngle(String.valueOf(convert), 3);
        return (isLeft ? "{SL" : "{SR") + format + "300}";
    }

    /**
     * Q2手臂生成角度指令
     */
    public static String onArmAngleQ2(boolean isLeft, int angle) {
        if (angle < -30) {
            angle = -30;
        }
        if (angle > 150) {
            angle = 150;
        }
        // 转换为硬件对应角度
        int convert = 4096 / 360 * angle;
        // 手臂0度为2048,左臂正转右臂反转
        convert = isLeft ? (2048 + convert) : (2048 - convert);
        // 组合指令
        byte[] data = new byte[14];
        data[0] = (byte) 0xff;// 前缀
        data[1] = (byte) 0xff;// 前缀
        data[2] = (byte) (isLeft ? 0x01 : 0x02);// 左臂或右臂
        data[3] = 0x0A;// 指令长度
        data[4] = 0x03;// 指令
        data[5] = 0x29;// 指令首地址
        data[6] = 0x1E;// 加速度字节
        data[7] = (byte) (convert & 0x00ff);// 角度低位
        data[8] = (byte) (convert >> 8);// 角度高位
        data[9] = 0x00;// 时间低位
        data[10] = 0x00;// 时间高位
        data[11] = 0x0320 & 0x00ff;// 速度低位
        data[12] = 0x0320 >> 8;// 速度高位
        data[13] = (byte) (initVerifySum(data) ^ 0xff);// 校验位
        return ByteUtils.bytes2HexStr(data);
    }

    /**
     * 生成校验位,去掉命令头尾后求和
     */
    private static int initVerifySum(byte[] bytes) {
        int sum = 0;
        for (int i = 2; i < bytes.length - 1; i++) {
            sum = sum + (bytes[i] < 0 ? 256 + bytes[i] : bytes[i]);
        }
        return sum % 256;
    }

    /**
     * H1手臂配置文件JsonArray转String数组
     */
    public static String[] onFormatDataH1(JSONArray arrays) {
        int length = arrays.length();
        String[] strings = new String[length];
        for (int i = 0; i < length; i++) {
            strings[i] = arrays.optString(i);
        }
        return strings;
    }
}
