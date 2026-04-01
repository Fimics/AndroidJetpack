package com.mic.nav.player;

import android.media.AudioManager;
import android.media.AudioTrack;

import androidx.annotation.NonNull;

import com.noetix.libcore.utils.KLog;
import com.noetix.libnoetix.TTSHelper;
import com.noetix.libnoetix.entity.AudioFrame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    // 16000 * 1 * 2，等于32000字节每秒  10FPS 每个Frame 3200

    // 配置常量优化
    private static final int CHUNK_MS = 100;
    private static final int SAMPLES_PER_CHUNK = TTSHelper.SAMPLE_RATE * CHUNK_MS / 1000; // 1600 ,50ms
    private static final int BYTES_PER_CHUNK = SAMPLES_PER_CHUNK * 2; //100ms  一个Frame
    private static final int BUFFER_CHUNK_MULTIPLIER = 4; // 增大缓冲区倍数  (10-4) 6个Frame

    // 单例实现
    private static final Object INSTANCE_LOCK = new Object();

    // 核心组件
    private AudioTrack audioTrack;
    private final LinkedBlockingQueue<AudioFrame> dataQueue = new LinkedBlockingQueue<>(1000); // 增大队列容量
    private final ExecutorService executor;
    private volatile boolean isStarted = false;


    public AudioPlayer() {
        // 创建高优先级播放线程
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "NXPlayer-Thread");
            thread.setPriority(Thread.MAX_PRIORITY);
            return thread;
        });
        initAudioDevice();
    }

    public synchronized void start() {

        if (audioTrack == null) {
            initAudioDevice();
        }

        setStarted(true);
        audioTrack.play();
        executor.submit(this::playbackLoop);
        KLog.i(TAG, "Playback started | Queue capacity:" + dataQueue.remainingCapacity());
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public synchronized void stop() {
        try {
            if (audioTrack != null) {
                setStarted(false);
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
        } catch (Exception e) {
            KLog.e(TAG, "Stop error: " + e.getMessage());
        }
    }

    public void emergencyStop(String form) {
        dataQueue.clear();
        audioTrack.flush();
        KLog.w(TAG, "Emergency stop from:" + form);
    }

    public void enqueueFrame(@NonNull AudioFrame frame) {
//        KLog.d(TAG,"enqueueFrame frameID : "+frame.getId() );

         //TODO
//        if (AppContext.isSaveEnqueueAudioFAudio){
//            byte audio [] = frame.getData();
//            if (audio!=null && audio.length>0){
//                FileUtil.writeFile(audio, "/sdcard/tts_enqueue_data.pcm");
//            }
//        }

        dataQueue.add(frame);
    }

    private synchronized void initAudioDevice() {
        try {
            if (audioTrack != null) {
                audioTrack.release();
            }

            int minBuffer = AudioTrack.getMinBufferSize(
                    TTSHelper.SAMPLE_RATE,
                    TTSHelper.CHANNEL,
                    TTSHelper.AUDIO_FORMAT);


            // 增大缓冲区计算
            int bufferSize = Math.max(minBuffer, BYTES_PER_CHUNK * BUFFER_CHUNK_MULTIPLIER);
            KLog.d(TAG,"minBuffer "+minBuffer);

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    TTSHelper.SAMPLE_RATE,
                    TTSHelper.CHANNEL,
                    TTSHelper.AUDIO_FORMAT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);

            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioTrack init failed");
            }

            KLog.i(TAG, "Audio device ready | Buffer:" + bufferSize + " bytes");
        } catch (Exception e) {
            KLog.e(TAG, "Audio init failed: " + e.getMessage());
            throw new RuntimeException("Audio unavailable", e);
        }
    }
    // endregion

    private long time;
    private void playbackLoop() {
        KLog.i(TAG, "Playback thread start");

        while (true) {
            try {
                AudioFrame frame = dataQueue.take(); // 阻塞式获取


                  //TODO
//                if (AppContext.isSavePlaybackAudio){
//                    byte audio [] = frame.getData();
//                    if (audio!=null && audio.length>0) {
//                        FileUtil.writeFile(audio, "/sdcard/tts_playback_data.pcm");
//                    }
//                }

                long now = System.currentTimeMillis();
                long diffTime = now -time;
                time = now;

                // 增强写入可靠性
                byte[] data = frame.getData();
                int offset = 0;
//                KLog.w(TAG,"playbackLoop frameId :  "+frame.getId()+ "   diffTime : "+diffTime +" offset "+offset +" data.length "+data.length+"  SAMPLES_PER_CHUNK "+SAMPLES_PER_CHUNK);
//                if (offset >= data.length){
//                    KLog.w(TAG,"因 offset < data.length 丢帧------------->offset "+offset +" data.length "+data.length);
//                }
                while (offset < data.length) {
                    long timeBaseOffset = System.nanoTime();
                    int bytesWritten = audioTrack.write(data, offset, data.length - offset);
//                    KLog.d(TAG,"bytesWritten  写入后时间->"+bytesWritten  + "  用时 "  +(System.nanoTime() - timeBaseOffset) /1_000_000 +" ms");
                    if (bytesWritten < 0) {
                        KLog.e(TAG, "Audio write error:" + bytesWritten);
//                        handlePlaybackError(new RuntimeException("Audio write failed"));
                        break;
                    }
                    offset += bytesWritten;
                }

               dataQueue.remove(frame);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                handlePlaybackError(e);
            }
        }

        KLog.i(TAG, "Playback thread exit");
    }


    private void handlePlaybackError(Exception e) {
        KLog.e(TAG, "Playback error: " + e.getMessage());
        stop();
        try {
            Thread.sleep(200);
            start(); // 自动恢复
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

}
