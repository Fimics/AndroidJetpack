package com.noetix.robotics.common.speech

import com.kk.speech.aiui.AIUISDK
import com.kk.speech.aiui.callback.ITTSCallBack
import com.noetix.libcore.utils.KLog

/**
 * 语音播报统一封装类
 */
object SpeechHelper {
    private const val TAG = "SpeechHelper"
    private var currentCallback: SpeechCallback? = null

    // 预留给外部接收音频数据的接口
    private var audioDataListener: ((ByteArray, Int) -> Unit)? = null

    interface SpeechCallback {
        fun onStart() {}
        fun onEnd(error: String?) {}
        fun onProgress(percent: Int) {}
    }

    /** 设置音频数据监听器（供外部驱动口型/脖子使用）*/
    fun setAudioDataListener(listener: (ByteArray, Int) -> Unit) {
        this.audioDataListener = listener
    }

    /** 初始化 TTS 引擎 */
    fun init() {
        KLog.i(TAG, "SpeechHelper 初始化 (AIUISDK 仅连TTS)")
        AIUISDK.getInstance().initForTTS()
    }

    /** 播报文本 */
    fun speak(text: String, callback: SpeechCallback? = null) {
        this.currentCallback = callback
        
        KLog.d(TAG, "准备播报: $text")
        AIUISDK.getInstance().startSpeaking(text, object : ITTSCallBack {
            override fun onSpeakBegin() {
                KLog.d(TAG, "SpeechHelper: onSpeakBegin")
                currentCallback?.onStart()
            }

            override fun onCompleted() {
                KLog.d(TAG, "SpeechHelper: onCompleted")
                currentCallback?.onEnd(null)
                currentCallback = null
            }

            override fun onError(error: String) {
                KLog.e(TAG, "SpeechHelper: onError - $error")
                currentCallback?.onEnd(error)
                currentCallback = null
            }

            override fun onPause(isUser: Boolean) {}

            override fun onResume(isUser: Boolean) {}

            override fun onProgress(percent: Int) {
                currentCallback?.onProgress(percent)
            }
        })
    }

    /** 停止播报 */
    fun stop() {
        AIUISDK.getInstance().stopSpeaking()
        currentCallback?.onEnd("Stopped")
        currentCallback = null
    }

    /** 释放引擎 */
    fun release() {
        stop()
    }
}
