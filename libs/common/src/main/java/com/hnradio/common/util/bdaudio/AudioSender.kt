package com.hnradio.common.util.bdaudio

import android.os.Handler
import com.hnradio.common.AppContext
import com.hnradio.common.util.L
import com.hnradio.common.util.OSSUtils
import java.io.File

class AudioSender {


    private var mp3Record : Mp3Recorder? = null
    private val mHandler = Handler()

    fun prepare(){
        mp3Record = Mp3Recorder().also {
            it.callback = object : Mp3Recorder.Callback{
                override fun onStart() {
                    L.e("开始了")
                    mHandler.post { mCallback?.onAudioStart() }
                }

                override fun onStop() {
                    L.e("结束了")
                    mHandler.post {mCallback?.onAudioStop()}

                }

                override fun onError(msg: String) {
                    L.e("错误了 ==> $msg")
                    mHandler.post { mCallback?.onError(msg)  }

                }

                override fun onMp3Success(duration : Int, mp3: File) {
                    L.e("成功了 ==> ${mp3.absolutePath}")
                    mHandler.post {
                        if(dontSave){
                            mCallback?.onMp3UploadSuccess(Pair(0,  ""))
                        }else{
//                        mCallback?.onFileSuccess(Pair(duration,  mp3.absolutePath))
                            upload(duration, mp3.absolutePath)
                        }
                    }

                }

                override fun onPcmSuccess(duration: Int, pcm: File) {
                    L.e("pcm 保存成功")
                    mHandler.post {
                        if(!dontSave) {
                            mCallback?.onPcmSaveSuccess(Pair(duration, pcm.absolutePath))
                        }
                    }
                }
            }
            it.prepare()
        }
        BdAudioTransformer.instance.registerCallback(TC)
    }

    private val TC = object : BdAudioTransformer.Callback{
        override fun onError(key: Int, msg: String) {
            mHandler.post { mCallback?.onRecorgnizeError(key, msg) }

        }

        override fun onSuccess(key: Int, ret: Pair<Int, String>) {
            mHandler.post { mCallback?.onRecorgnizeSuccess(key, ret) }

        }
    }

    private fun upload(duration : Int, file : String){
        mCallback?.onUploading(0)
        OSSUtils.upLoadFile(AppContext.getContext(), file, uploadFileCallback = object : OSSUtils.UploadFileCallback {
            override fun onUploadSuccess(url: String) {
                mHandler.post { mCallback?.onMp3UploadSuccess(Pair(duration,  url)) }

                //交给前台去提交识别
            }

            override fun onUploadFailure() {
                mHandler.post { mCallback?.onError("上传失败") }

            }

        }, progressCallback = {
            mHandler.post { mCallback?.onUploading(it) }

        })
    }

    fun start(){
        dontSave = false
        mp3Record?.start()
    }

    private var dontSave = false
    fun cancel(){
        dontSave = true
        mp3Record?.stop()
    }

    fun stop(){
        mp3Record?.stop()
    }

    fun query(key : Int, url : String){
        BdAudioTransformer.instance.query(key, url)
    }

    fun fastQuery(userToken : String, key : Int, pcmPath : String){
        BdAudioTransformer.instance.fastQuery(userToken, key, pcmPath)
    }

    var mCallback : Callback? = null

    interface Callback{

        fun onAudioStart()

        fun onAudioStop()

        fun onUploading(progress : Int)

        fun onError(msg : String)

        fun onMp3UploadSuccess(ret : Pair<Int, String>)
        fun onPcmSaveSuccess(ret : Pair<Int, String>)

        fun onRecorgnizeError(key : Int, reason : String)
        fun onRecorgnizeSuccess(key : Int, ret : Pair<Int, String>)
    }

    fun release(){
        mp3Record?.release()
        BdAudioTransformer.instance.unregisterCallback(TC)
    }
}