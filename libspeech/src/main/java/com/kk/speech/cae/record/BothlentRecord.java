package com.kk.speech.cae.record;

import android.content.Context;
import android.util.Log;

import com.iflytek.alsa.bothlentrecorder.AlsaRecorder;
import com.iflytek.alsa.bothlentrecorder.IPcmCallBack;
import com.iflytek.alsa.bothlentrecorder.ResultType;
import com.kk.speech.cae.bean.PcmDevice;

public class BothlentRecord extends BaseRecord {
    private static final String TAG = "BothlentRecord";
    private final PcmDevice mPcmDevice;
    private AlsaRecorder mAlsaRecorder;

    public BothlentRecord(PcmDevice mPcmDevice) {
        this.mPcmDevice = mPcmDevice;
    }

    @Override
    public void init(Context context) {
        sample = mPcmDevice.getmPcmSampleRate();
        channel = mPcmDevice.getmPcmChannel();

        int micCount = 4;

        mAlsaRecorder = AlsaRecorder.createInstance(mPcmDevice.getmPcmCard(), mPcmDevice.getmPcmDevice(), mPcmDevice.getmPcmChannel(),
                mPcmDevice.getmPcmSampleRate(), mPcmDevice.getmPcmPeriodSize(), mPcmDevice.getmPcmPeriodCount(), mPcmDevice.getmPcmFormat(), micCount);
        mAlsaRecorder.setLogShow(false);
    }

    private boolean isSuccess = false;

    @Override
    public boolean startRecord() {
        mAlsaRecorder.startRecording(new IPcmCallBack() {
            @Override
            public void onPcmData(byte[] bytes, int i) {
                onAudioCallBack(bytes, i);
            }

            @Override
            public void onState(ResultType resultType) {
                Log.e(TAG, "onState: " + resultType);
                switch (resultType) {
                    case OPEN_ERROR:
                    case READ_DEVICE_ERROR:
                        isSuccess = false;
                        break;
                    case OPEN_SUCCESS:
                        isSuccess = true;
                }
            }
        });
        return isSuccess;
    }

    @Override
    public void stopRecord() {
        if (mAlsaRecorder != null) {
            mAlsaRecorder.stopRecording();
        }
    }
}
