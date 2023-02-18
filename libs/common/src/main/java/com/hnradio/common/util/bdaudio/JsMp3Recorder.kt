package com.hnradio.common.util.bdaudio

import android.Manifest
import android.os.SystemClock
import androidx.fragment.app.FragmentActivity
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.ToastUtils
import com.orhanobut.logger.Logger
import com.permissionx.guolindev.PermissionX
import java.io.File

/**
 *
 * @Description: h5录音回调
 * @Author: huqiang
 * @CreateDate: 2022-01-13 11:24
 * @Version: 1.0
 */
object JsMp3Recorder {
    var isPermission = false
    private var startTime = 0L
    private var mp3Recorder: Mp3Recorder? = null
    private var recorderCallback: RecorderCallback? = null

    fun configAudio(jsCallback: RecorderCallback) {
        if (mp3Recorder == null) {
            mp3Recorder = Mp3Recorder()
        }
        recorderCallback = jsCallback
        mp3Recorder?.prepare()
        mp3Recorder?.callback = object : Mp3Recorder.Callback {
            override fun onStart() {
                Logger.d("Mp3Recorder.Callback -- 结束开始")
            }

            override fun onStop() {
                Logger.d("Mp3Recorder.Callback -- 结束录音")
            }

            override fun onError(msg: String) {
                recorderCallback?.onError(-3, msg)
            }

            override fun onMp3Success(duration: Int, mp3: File) {


            }

            override fun onPcmSuccess(duration: Int, pcm: File) {
                val tooShort = duration < 1
                if (tooShort) {
                    recorderCallback?.onError(-2, "录音太短啦")
                } else {
                    val filePath = pcm.absolutePath
                    recorderCallback?.onRecordSuccess(0, filePath)
                }
            }
        }
    }

    fun startRecorder() {
        if (mp3Recorder != null) {
            startTime = SystemClock.uptimeMillis()
            mp3Recorder?.start()
        } else {
            recorderCallback?.onError(-1, "未初始化")
        }
    }

    fun endRecorder() {
        mp3Recorder?.stop()
    }

    fun audioTrans(filePath: String) {
        BdAudioTransformer.instance.registerCallback(TC)
        BdAudioTransformer.instance.fastQuery(
            UserManager.getToken() ?: "",
            1,
            filePath
        )
    }

    private val TC = object : BdAudioTransformer.Callback {
        override fun onError(key: Int, msg: String) {
            recorderCallback?.onError(-4, "语音转换失败")
        }

        override fun onSuccess(key: Int, ret: Pair<Int, String>) {
            recorderCallback?.onTransSuccess(0, ret.second)
        }
    }

    fun release() {
        mp3Recorder?.release()
        mp3Recorder = null
    }
}

interface RecorderCallback {
    fun onError(code: Int, msg: String)
    fun onRecordSuccess(code: Int, filePath: String)
    fun onTransSuccess(code: Int, translateStr: String)
}