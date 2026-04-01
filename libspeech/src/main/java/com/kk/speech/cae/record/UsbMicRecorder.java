package com.kk.speech.cae.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.noetix.libcore.utils.KLog;
import com.noetix.libcore.utils.PcmFileUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class UsbMicRecorder extends BaseRecord {
    private static final String TAG = "UsbMicRecorder";
    private static final int SUCCESS =1;
    private AudioRecord audioRecord;
    private Thread recordingThread;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    public  int sampleRate=16000;
//    public  int channelConfig=AudioFormat.CHANNEL_IN_STEREO;
    public  int channel= AudioFormat.CHANNEL_IN_MONO;
    public  int audioFormat=AudioFormat.ENCODING_PCM_16BIT;
    public  int bufferSize;
    private volatile PcmFileUtils mTtsFileUtils;
    private UsbMicRecorderCallBack usbMicRecorderCallBack;

    public UsbMicRecorder() {
        mTtsFileUtils = new PcmFileUtils("/sdcard/robot/audio6/");
    }
     public void setUsbMicRecorderCallBack(UsbMicRecorderCallBack usbMicRecorderCallBack) {
         this.usbMicRecorderCallBack = usbMicRecorderCallBack;
     }

    @SuppressLint({"MissingPermission", "InlinedApi"})
    @Override
    public void init(Context context) {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)*4;
        KLog.d(TAG,"sampleRate "+sampleRate +" channel "+channel+" audioFormat "+audioFormat+"  bufferSize "+bufferSize);
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.UNPROCESSED,
                sampleRate,
                channel,
                audioFormat,
                bufferSize);
    }

    @Override
    public boolean startRecord() {
        if (isRecording.get()) {
            Log.w(TAG, "Recording is already in progress");
            return true;
        }

        try {

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioRecord initialization failed");
            }

            isRecording.set(true);
            audioRecord.startRecording();

            recordingThread = new Thread(this::onAudioDataChanged, "AudioRecorder Thread");
            recordingThread.start();
            Log.d(TAG, "Recording started ..." );
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting recording", e);
        }
        return true;
    }

    @Override
    public void stopRecord() {
        if (!isRecording.get()) {
            return;
        }

        isRecording.set(false);

        try {
            if (audioRecord != null) {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
                audioRecord = null;
            }

            if (recordingThread != null) {
                recordingThread.interrupt();
                recordingThread.join();
                recordingThread = null;
            }

            Log.d(TAG, "Recording stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping recording", e);
        }
    }


    public boolean isRecording() {
        return isRecording.get();
    }

    private void  onAudioDataChanged () {
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

        while (isRecording.get()) {
            int bytesRead = audioRecord.read(buffer, bufferSize);
            if (bytesRead > 0) {
                byte[] data = new byte[bytesRead];
                buffer.rewind();
                buffer.get(data);
                if (usbMicRecorderCallBack != null) {
                    usbMicRecorderCallBack.onAudioDataChanged(data, data.length);
                }
            }
        }
    }

}
