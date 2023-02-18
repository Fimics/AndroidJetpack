package com.hnradio.common.util.bdaudio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.hnradio.common.AppContext
import com.hnradio.common.util.L.e
import com.white.audio.Mp3Encoder
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class Mp3Recorder {


    private var mAudioRecord : AudioRecord? = null
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private var mBufferSizeInBytes = 0
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private val mSampleRateInHz = 16000
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private val mChannelConfig = AudioFormat.CHANNEL_IN_MONO
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private val mAudioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val mAudioSource = MediaRecorder.AudioSource.MIC
    private var transformThread : TransformThread

    private val pool = Executors.newSingleThreadExecutor()

    private val audioManager : AudioManager
    init {
        transformThread = TransformThread()
        transformThread.start()
        audioManager = AppContext.getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun prepare(){
        pool.execute {
            SystemClock.sleep(200)
            try {
                mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat)//计算最小缓冲区
                //创建AudioRecord。AudioRecord类实际上不会保存捕获的音频，因此需要手动创建文件并保存下载。
                mAudioRecord = AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes)//创建AudioRecorder对象
            }catch (e : Exception){
                callback?.onError(e.localizedMessage)
            }
        }
    }


    private fun requestFocus(): Boolean {
        val result: Int = audioManager.requestAudioFocus(
            object : OnAudioFocusChangeListener {
                override fun onAudioFocusChange(focusChange: Int) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        // Pause playback
//                      pause();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Resume playback
//                      start();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // mAm.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                        audioManager.abandonAudioFocus(this)
                        // Stop playback
//                      stop();
                    }
                }
            },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )
        return result == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
    }

    fun start(){
        val state = mAudioRecord?.state
        val recordingState = mAudioRecord?.recordingState

        Log.e("s", "audio state = ${state}   recording state = ${recordingState}")

        if(state == AudioRecord.STATE_INITIALIZED && recordingState == AudioRecord.RECORDSTATE_STOPPED){
            requestFocus()
            mAudioRecord?.startRecording()
            isRecording.set(true)
            pool.execute(RecordingThread())
        }else{
            callback?.onStop()
        }
    }

    fun stop(){
        if(mAudioRecord?.state == AudioRecord.STATE_INITIALIZED &&
            mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING){
            isRecording.set(false)
        }
    }

    private val isRecording = AtomicBoolean(false)
    private var duration = 0
    private inner class RecordingThread : Runnable{

        private var hasInvoked = false
        private var currentPath = ""
        override fun run() {

            try {
                duration = 0
                val buffer = ByteArray(mBufferSizeInBytes)
                var dos : DataOutputStream? = null
                val start = SystemClock.uptimeMillis()
                while (isRecording.get() && mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING){
                    val len = mAudioRecord?.read(buffer,0, mBufferSizeInBytes)?:0
                    if(!hasInvoked){
                        hasInvoked = true
                        callback?.onStart()
                        currentPath = pcmSavePath
                        if (!TextUtils.isEmpty(currentPath)) {
                            dos = DataOutputStream(BufferedOutputStream(FileOutputStream(File(currentPath))))
                        }
                        transformThread.putCmd(PCM.buildStartData())
                    }
                    dos?.write(buffer, 0, len)
                    if(len > 2){
                        transformThread.putData(toShortArray(buffer.copyOfRange(0, len))!!)
                    }
                }
                duration = ((SystemClock.uptimeMillis() - start) / 1000).toInt()
                transformThread.putCmd(PCM.buildCompleteData())
                mAudioRecord?.stop()
                callback?.onStop()
                val file = File(currentPath)
                if(file.length() > 0){
                    callback?.onPcmSuccess(duration, file)
                }else{
                    callback?.onError("录音失败")
                }
            }catch (e : Exception){
                callback?.onError(e.localizedMessage?:"")
            }
        }

        private val pcmSavePath: String
            private get() = try {
                val dirFile = AppContext.getContext().getExternalFilesDir(null)
                val mp3Dir = File(dirFile, "live_voice")
                if (!mp3Dir.exists()) {
                    mp3Dir.mkdirs()
                }
                val mp3 = File(mp3Dir, "live_voice.pcm")
                if (mp3.exists()) {
                    mp3.delete()
                }
                mp3.createNewFile()
                mp3.absolutePath
            } catch (e: Exception) {
                ""
            }

        private fun toShortArray(src : ByteArray) : ShortArray? {

            if(src.size > 2){
                val size = src.size shr 1
                val sa = ShortArray(size)
                for (i in 0 until size){
                    //安卓大端转小端
                    sa[i] = ((src[i*2+1].toInt() shl 8) or (src[i*2].toInt() and 0xFF)).toShort()
                }
                return sa;
            }
            return null
        }
    }


    private inner class TransformThread : Thread() {

        private val mQueue = ArrayBlockingQueue<PCM>(30)
        private val encoder = Mp3Encoder()
        private val isExit = AtomicBoolean(false)
        private var canTransform = false
        private var currentPath : String? = null

        override fun run() {
            try {
                while (!isExit.get()) {
                    val (size, shortData) = mQueue.take()
                    if (size == -1) {
                        //开始转换
                        currentPath = mp3SavePath
                        if (!TextUtils.isEmpty(currentPath)) {
                            //16K 单声道 2字节采样
                            encoder.initRealtime(16000, 1, 16, currentPath!!)
                            canTransform = true
                        }
                    } else if (size == -2) {
                        //结束转换
                        if (canTransform) {
                            encoder.lameSetMp3TagFid()
                            encoder.lameDestroy()
                            currentPath?.let {
                                if(!isExit.get()){
                                    val file = File(it)
                                    if(file.length() > 0){
                                        //确保先回调onPcmSaveSuccess
                                        SystemClock.sleep(350)
                                        callback?.onMp3Success(duration, file)
                                    }else{
                                        callback?.onError("转换失败")
                                    }
                                }
                            }
                        }
                        canTransform = false
                    } else if (size == 0) {
                        //结束线程
                        isExit.set(true)
                    } else {
                        //转换中
                        if (canTransform) {
                            encoder.lameEncodeRealtime(shortData, size)
                        }
                    }
                }
            } catch (e: Exception) {
                e("mp3转换过程出现异常：" + e.localizedMessage)
            }
            e("mp3转换线程结束")
        }

        private val mp3SavePath: String
            private get() = try {
                val dirFile = AppContext.getContext().getExternalFilesDir(null)
                val mp3Dir = File(dirFile, "live_voice")
                if (!mp3Dir.exists()) {
                    mp3Dir.mkdirs()
                }
                val mp3 = File(mp3Dir, "live_voice.mp3")
                if (mp3.exists()) {
                    mp3.delete()
                }
                mp3.createNewFile()
                mp3.absolutePath
            } catch (e: Exception) {
                ""
            }

        fun exit() {
            try {
                mQueue.put(PCM.buildEmptyData())
                isExit.set(true)
            } catch (e: Exception) {
            }
        }

        fun putCmd(cmd : PCM){
            try {

                mQueue.put(cmd)
            }catch (e : Exception){

            }
        }

        fun putData(data: ShortArray) {
            try {
                val size = data.size
                val cp = ShortArray(size)
                System.arraycopy(data, 0, cp, 0, size)
                val p = PCM(size, cp)
                mQueue.put(p)
            } catch (e: Exception) {
            }
        }
    }

    fun release(){
        transformThread.exit()
        stop()
        pool.execute {
            SystemClock.sleep(200)
            mAudioRecord?.release()
        }
    }

    var callback : Callback? = null

    interface Callback{
        fun onStart()
        fun onStop()
        fun onError(msg : String)
        fun onMp3Success(duration : Int, mp3 : File)
        fun onPcmSuccess(duration : Int, pcm : File)
    }
}