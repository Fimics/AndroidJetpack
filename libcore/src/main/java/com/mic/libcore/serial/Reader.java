package com.mic.libcore.serial;

import android.text.TextUtils;


import com.mic.libcore.serial.callback.ISerialCallBack;
import com.mic.libcore.serial.constant.DataType;
import com.mic.libcore.utils.ByteUtils;
import com.mic.libcore.utils.KLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 使用RxJava实现的串口数据读取
 */
public class Reader {
    // 数据读取对象
    private final BufferedInputStream mInputStream;
    // 返回数据的类型
    private final DataType mDataType;
    // 数据返回接口
    private final ISerialCallBack mCallBack;
    // 缓冲区大小
    private static final int BUFFER_SIZE = 512;
    // 数据读取线程
    private Disposable mReadDisposable;

    public Reader(InputStream is, ISerialCallBack callBack, DataType dataType) {
        this.mCallBack = callBack;
        this.mDataType = dataType;
        this.mInputStream = new BufferedInputStream(is);
    }

    /**
     * 开始读取串口数据
     */
    public void startReading() {
        byte[] buffer = new byte[BUFFER_SIZE];
        mReadDisposable = Observable.interval(10, TimeUnit.MILLISECONDS)
                .flatMap(tick -> Observable.fromCallable(() -> {
                            // 在IO线程执行耗时操作
                            String result = "";
                            if (mInputStream.available() > 0) {
                                int size = mInputStream.read(buffer);
                                if (size > 0) {
                                    result = onProcessData(buffer, size);
                                }
                            }
                            return result;
                        }).subscribeOn(Schedulers.io())
                ).observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    // 在主线程显示结果
                    if (!TextUtils.isEmpty(result) && mCallBack != null) {
                        KLog.d("串口读取到数据:" + result);
                        mCallBack.onResult(result);
                    }
                }, throwable -> KLog.e("读取数据异常:" + throwable.getMessage()));
    }

    /**
     * 停止读取串口数据
     */
    public void stopReading() {
        if (mReadDisposable != null) {
            mReadDisposable.dispose();
            mReadDisposable = null;
            try {
                mInputStream.close();
            } catch (IOException e) {
                KLog.e("停止读取异常:" + e.getMessage());
            }
        }
    }

    /**
     * 处理接收到的数据
     */
    private String onProcessData(byte[] received, int size) {
        if (mDataType == DataType.STRING) {
            return new String(received, 0, size);
        } else if (mDataType == DataType.HEX) {
            return ByteUtils.bytes2HexStr(received, 0, size);
        }
        return "";
    }
}