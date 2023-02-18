package com.hnradio.common.util.bdaudio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.hnradio.common.AppContext;

/**
 * Created by ytf on 2018/3/27.
 * Description:
 */

public class MP3UrlPlayer implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer mPlayer;
    private String mp3Url, lastMp3Url;
    private boolean hasPrepared;
    private boolean hasReleased;
    /**
     * is normal played
     */
    private boolean hasNormalPlayed;
    private boolean isPaused;
    private int currentPosition;
    private int allDuration;

    private Callback callback;

    public void setUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        this.mp3Url = url;
//        if (!mp3Url.equals(lastMp3Url))
//        {
//            lastMp3Url = mp3Url;
        release();
        prepare();
//        }
    }

    public void release() {
        resetState();
        if (mPlayer != null)
            mPlayer.release();
    }

    public boolean isPlaying(){
        return hasPrepared && mPlayer.isPlaying();
    }

    private void prepare() {
        try {
            mAm = (AudioManager) AppContext.getContext().getSystemService(Context.AUDIO_SERVICE);
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
            mPlayer.setOnBufferingUpdateListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setDataSource(mp3Url);
            mPlayer.prepareAsync();
            requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioManager mAm;

    private boolean requestFocus() {
        // Request audio focus for playback
        int result = mAm.requestAudioFocus(
                new AudioManager.OnAudioFocusChangeListener() {
                    public void onAudioFocusChange(int focusChange) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            // Pause playback
//                                                       pause();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            // Resume playback
//                                                       start();
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                            mAm.abandonAudioFocus(this);
                            // Stop playback
//                                                       stop();
                        }
                    }
                },
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        return result == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
    }

    private void resetState() {
        hasReleased = true;
        hasPrepared = false;
        hasNormalPlayed = false;
        isPaused = true;
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    public void pause() {
        if (mPlayer != null && hasPrepared) {
            if (mPlayer.isPlaying()) {
                isPaused = true;
                mPlayer.pause();
            }
        }
    }

    public void restart() {
        new Thread() {

            @Override
            public void run() {
                if (mPlayer != null && hasPrepared) {
                    if (mPlayer.isPlaying()) {
                        mPlayer.pause();
                        mPlayer.seekTo(0);
                        mPlayer.start();
                    } else {
                        if (isPaused) {
                            mPlayer.seekTo(0);
                            mPlayer.start();
                            isPaused = false;
                        } else {
                            MP3UrlPlayer.this.start();
                        }
                    }
                }
            }
        }.start();
    }

    public void bindPause() {
        if (hasNormalPlayed) {
            pause();
        }
    }

    public void bindResume() {
        if (hasNormalPlayed && isPaused)
            start();
    }

    public boolean hasPrepared() {
        return hasPrepared;
    }

    public void start() {
        if (mPlayer != null && hasPrepared) {
            if (!mPlayer.isPlaying()) {
                isPaused = false;
                mPlayer.start();
                hasNormalPlayed = true;
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        hasReleased = false;
        hasPrepared = true;
        isPaused = true;
        allDuration = mediaPlayer.getDuration();
        currentPosition = 0;
        start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        currentPosition = 0;
        if (callback != null)
            callback.onComplete();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
    }

    public void stop() {
        if (mPlayer != null) {
            try {
                //正常播放过才做以下操作
                if (hasNormalPlayed) {
                    mPlayer.stop();
                    mPlayer.seekTo(0);//须将播放时间设置到0；这样才能在下次播放是重新开始，否则会继续上次播放
                }
                isPaused = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setCallback(Callback c) {
        callback = c;
    }

    public interface Callback {

        void onComplete();
    }
}
