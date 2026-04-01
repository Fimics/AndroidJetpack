package com.kk.speech.aiui.support;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.kk.speech.aiui.callback.AudioPlayListener;
import com.noetix.libcore.utils.KLog;

import java.io.ByteArrayOutputStream;

public class PcmAudioPlayer {
    private static final String TAG = "PcmAudioPlayer";
    private static volatile PcmAudioPlayer mInstance;
    private volatile AudioTrack mAudioTrack;
    private AudioAttributes mAudioAttributes;
    private AudioFormat mAudioFormat;
    private int mMinBufferSize;
    private boolean isPlaying = false;

    private Handler mAudioHandler;
    private Handler mMainHandler;
    private AudioPlayListener mPlayListener;
    // 字节缓冲区，用于存储临时数据
    private ByteArrayOutputStream mByteBuffer = new ByteArrayOutputStream();
    // 同步锁对象
    private final Object mByteLock = new Object();

    public static PcmAudioPlayer getInstance() {
        if (mInstance == null) {
            synchronized (PcmAudioPlayer.class) {
                if (mInstance == null) {
                    mInstance = new PcmAudioPlayer();
                }
            }
        }
        return mInstance;
    }

    public PcmAudioPlayer() {
        HandlerThread mAudioThread = new HandlerThread("PcmAudioPlayer");
        mAudioThread.start();
        mAudioHandler = new Handler(mAudioThread.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper());
        int sampleRate = 16000;
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        int channel = AudioFormat.CHANNEL_OUT_MONO;
        mAudioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA).build();
        mAudioFormat = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(encoding)
                .setChannelMask(channel).build();
        mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, channel, encoding);
    }

    private void initAudioTrack() {
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(mAudioAttributes, mAudioFormat, mMinBufferSize,
                    AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        }
        mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                stopPlayAudio(true);
            }
        });
        mAudioTrack.play();
        KLog.d(TAG, "initAudioTrack");
    }

    public void setPlayListener(AudioPlayListener listener) {
        this.mPlayListener = listener;
    }

    public synchronized void startPlayAudio() {
        KLog.d(TAG, "开始播放");
        isPlaying = true;
        initAudioTrack();
        if (mPlayListener != null) {
            mMainHandler.post(() -> mPlayListener.onPlaybackBegin());
        }
    }

    public synchronized void stopPlayAudio(boolean completed) {
        KLog.d(TAG, "停止播放：" + completed);
        isPlaying = false;
        if (mAudioTrack != null) {
            try {
                mAudioTrack.pause();
                mAudioTrack.flush();
                mAudioTrack.stop();
                mAudioTrack.release();
            } finally {
                mAudioTrack = null;
            }
        }
        if (completed && mPlayListener != null) {
            mMainHandler.post(() -> mPlayListener.onPlaybackCompleted());
        }
        // TODO: 待同事集成SDK后，在此处停止口型动画:
        // IRobotSDKManager.getInstance().stopAudio2Face();
        // IRobotSDKManager.getInstance().interrupt();
        // IRobotSDKManager.getInstance().resetFaceAngles();
    }

    private void cacheAudioData(byte[] data) {
        synchronized (mByteLock) {
            mByteBuffer.write(data, 0, data.length);
        }
    }

    private void sendBufferedData(int dts) {
        synchronized (mByteLock) {
            if (mByteBuffer.size() > 0) {
                // TODO: 将缓冲数据转发给 SDK (驱动口型/脖子)
                // IRobotSDKManager.getInstance().processAudioStream(mByteBuffer.toByteArray(), dts);
                
                if (dts == 2 || dts == 3) {
                    clearBufferedData();
                }
                KLog.d("监控DTS：sendBufferedData" + dts);
            }
        }
    }

    private void clearBufferedData() {
        synchronized (mByteLock) {
            mByteBuffer.reset();
        }
    }

    private void sendData(byte[] datas, int dts) {
        synchronized (mByteLock) {
            // TODO: 将数据转发给 SDK (驱动口型/脖子)
            // IRobotSDKManager.getInstance().processAudioStream(datas, dts);
            KLog.d("监控DTS：sendData" + dts);
        }
    }

    public synchronized void playAudioData(byte[] datas, int dts) {
        // 直接添加到音频轨道播放
        addAudioData(datas, dts);
        
        // 提取缓冲发送的逻辑（源TTSHelper逻辑保留以适配将来接入口型动画）
        KLog.d("监控DTS：playAudioData" + dts);
        if (dts == 3) {
            sendData(datas, dts);
        } else {
            if (dts == 0) {
                clearBufferedData();
                cacheAudioData(datas);
                if (mByteBuffer.size() >= 32000) {
                    sendBufferedData(0);
                }
            } else if (dts == 1) {
                if (mByteBuffer.size() < 32000) {
                    cacheAudioData(datas);
                    if (mByteBuffer.size() >= 32000) {
                        sendBufferedData(0);
                    }
                } else {
                    sendData(datas, dts);
                }
            } else {
                if (mByteBuffer.size() < 32000) {
                    cacheAudioData(datas);
                    sendBufferedData(3);
                } else {
                    sendData(datas, dts);
                }
            }
        }
    }

    @SuppressLint({"CheckResult", "NewApi"})
    public synchronized void addAudioData(byte[] datas, int dts) {
        if (mAudioTrack == null || !isPlaying) {
            return;
        }
        KLog.d("监控DTS：addAudioData" + dts);
        mAudioHandler.post(() -> {
            int written = 0;
            while (written < datas.length) {
                int toWrite = Math.min(4096, datas.length - written);
                if (mAudioTrack == null || !isPlaying) {
                    break;
                }
                int ret = mAudioTrack.write(datas, written, toWrite);
                if (ret < 0) {
                    break;
                }
                written += ret;
            }
            if (mAudioTrack != null && (dts == 2 || dts == 3)) {
                try {
                    mAudioTrack.setNotificationMarkerPosition(mAudioTrack.getBufferSizeInFrames());
                } catch (IllegalStateException e) {
                    KLog.e(TAG, "设置标记位置失败:" + e.getMessage());
                }
            }
        });
    }

    public boolean isPlaying() {
        return isPlaying && mAudioTrack != null
                && mAudioTrack.getPlayState() == android.media.AudioTrack.PLAYSTATE_PLAYING;
    }

    public void getPositionTimer() {
        if (mAudioTrack == null) return;
        int currentFrame = mAudioTrack.getPlaybackHeadPosition();
        KLog.d(TAG, "getPositionTimer:currentFrame=" + currentFrame);
        int rate = mAudioTrack.getPlaybackRate();
        KLog.d(TAG, "getPositionTimer:rate=" + rate);
        if (rate > 0) {
            float playTime = currentFrame * 1.0f / rate;
            long currentPlayTimeMs = (long) (1000 * playTime);
            KLog.d(TAG, "getPositionTimer:currentPlayTimeMs=" + currentPlayTimeMs);
        }
    }

    private volatile int mCurrentFrame;

    public synchronized void pausePlayAudio() {
        if (mAudioTrack == null) return;
        mCurrentFrame = mAudioTrack.getPlaybackHeadPosition();
        KLog.e(TAG, "暂停播放进度："+ mCurrentFrame);
    }
}
