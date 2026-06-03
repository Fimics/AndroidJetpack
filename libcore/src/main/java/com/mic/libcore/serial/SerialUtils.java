package com.mic.libcore.serial;

import android.serialport.SerialPort;
import com.mic.libcore.serial.callback.ISerialCallBack;
import com.mic.libcore.serial.constant.Baudrate;
import com.mic.libcore.serial.constant.DataType;
import com.mic.libcore.utils.KLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口辅助
 * Created by onlykk on 2024-12-08
 */
public class SerialUtils {
    /**
     * 串口对象
     */
    private SerialPort mSerialPort;
    /**
     * 读取数据的线程
     */
    private Reader mReadThread;

    /**
     * 构造函数
     *
     * @param path     串口的物理地址
     * @param baudrate 串口的波特率
     */
    public SerialUtils(String path, Baudrate baudrate) {
        try {
            mSerialPort = SerialPort
                    .newBuilder(path, baudrate.getBaudrate()).build();
        } catch (IOException e) {
            KLog.e("串口打开失败：" + e.getMessage());
            mSerialPort = null;
        }
    }

    /**
     * 开始读取数据
     */
    public void startReadData(ISerialCallBack back, DataType dataType) {
        if (mSerialPort != null) {
            InputStream inputStream = mSerialPort.getInputStream();
            // 开始读取数据
            if (mReadThread == null) {
                mReadThread = new Reader(inputStream, back, dataType);
            }
            mReadThread.startReading();
        }
    }

    /**
     * 停止读取数据
     */
    public void stopReadData() {
        // 关闭读取线程
        if (mReadThread != null) {
            mReadThread.stopReading();
            mReadThread = null;
        }
        if (mSerialPort != null) {
            mSerialPort.tryClose();
        }
    }

    /**
     * 向串口上写数据
     *
     * @param data 显示的16进制的字符串
     */
    public synchronized void setData(byte[] data) {
        if (mSerialPort != null) {
            OutputStream outputStream = mSerialPort.getOutputStream();
            try {
                outputStream.write(data);
            } catch (Exception e) {
                KLog.i("写入数据异常：" + e.getMessage());
            }
        }
    }
}
