//package com.hnradio.common.util.bdaudio
//
//import android.text.TextUtils
//import com.hnradio.common.AppContext
//import com.hnradio.common.util.L
//import com.hnradio.common.util.bdasr.BdASRRecognizer
//import com.white.audio.Mp3Encoder
//import java.io.File
//import java.util.concurrent.ArrayBlockingQueue
//import java.util.concurrent.atomic.AtomicBoolean
//
///**
// * @author ytf
// * Created by on 2021/11/03 15:25
// */
//class RealtimeAudioSender {
//
//    private var bdRecognizer : BdASRRecognizer = BdASRRecognizer.getInstance()
//    private lateinit var transformThread : TransformThread
//
//    fun prepare(){
//        bdRecognizer.registerListener(bdCallback)
//        bdRecognizer.init(AppContext.getContext())
//        transformThread = TransformThread()
//        transformThread.start()
//    }
//
//    fun start(){
//        bdRecognizer.start()
//    }
//
//    fun cancel(){
//        bdRecognizer.cancel()
//    }
//
//    fun stop(){
//        bdRecognizer.stop()
//    }
//
//    private val bdCallback = object : BdASRRecognizer.Callback(){
//
//        override fun onSuccess(result: String?) {
//
//        }
//
//        override fun onPortraitResult(s: String?) {
//
//        }
//
//        override fun onFailed(msg: String?, code: Int, subCode: Int) {
//
//        }
//
//        override fun onComplete() {
//            transformThread.putCmd(PCM.buildCompleteData())
//        }
//
//        override fun onAsrStart() {
//            super.onAsrStart()
//            transformThread.putCmd(PCM.buildStartData())
//        }
//
//        override fun onAsrAudio(data: ByteArray?, offset: Int, length: Int) {
//            if(length > 2){
//                 val temp = data!!.copyOfRange(offset, length)
//                val size = temp.size shr 1
//                val sa = ShortArray(size)
//                for (i in 0 until size){
//                    //安卓大端转小端
//                    sa[i] = ((temp[i*2+1].toInt() shl 8) or (temp[i*2].toInt() and 0xFF)).toShort()
//                }
//                transformThread.putData(sa)
//            }
//        }
//    }
//
//    private inner class TransformThread : Thread() {
//
//        private val mQueue = ArrayBlockingQueue<PCM>(30)
//        private val encoder = Mp3Encoder()
//        private val isExit = AtomicBoolean(false)
//        private var canTransform = false
//        private var currentPath : String? = null
//
//        override fun run() {
//            try {
//                while (!isExit.get()) {
//                    val (size, shortData) = mQueue.take()
//                    if (size == -1) {
//                        //开始转换
//                        currentPath = mp3SavePath
//                        if (!TextUtils.isEmpty(currentPath)) {
//                            //16K 单声道 2字节采样
//                            encoder.initRealtime(16000, 1, 16, currentPath!!)
//                            canTransform = true
//                        }
//                    } else if (size == -2) {
//                        //结束转换
//                        if (canTransform) {
//                            encoder.lameSetMp3TagFid()
//                            encoder.lameDestroy()
//                            currentPath?.let {
//                                if(!isExit.get()){
//                                    val file = File(it)
//                                    if(file.length() > 0){
////                                        callback?.onSuccess(duration, file)
//                                    }else{
////                                        callback?.onError("转换失败")
//                                    }
//                                }
//                            }
//                        }
//                        canTransform = false
//                    } else if (size == 0) {
//                        //结束线程
//                        isExit.set(true)
//                    } else {
//                        //转换中
//                        if (canTransform) {
//                            encoder.lameEncodeRealtime(shortData, size)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                L.e("mp3转换过程出现异常：" + e.localizedMessage)
//            }
//            L.e("mp3转换线程结束")
//        }
//
//        private val mp3SavePath: String
//            private get() = try {
//                val dirFile = AppContext.getContext().getExternalFilesDir(null)
//                val mp3Dir = File(dirFile, "live_voice")
//                if (!mp3Dir.exists()) {
//                    mp3Dir.mkdirs()
//                }
//                val mp3 = File(mp3Dir, "bd_live_voice.mp3")
//                if (mp3.exists()) {
//                    mp3.delete()
//                }
//                mp3.createNewFile()
//                mp3.absolutePath
//            } catch (e: Exception) {
//                ""
//            }
//
//        fun exit() {
//            try {
//                mQueue.put(PCM.buildEmptyData())
//                isExit.set(true)
//            } catch (e: Exception) {
//            }
//        }
//
//        fun putCmd(cmd : PCM){
//            try {
//
//                mQueue.put(cmd)
//            }catch (e : Exception){
//
//            }
//        }
//
//        fun putData(data: ShortArray) {
//            try {
//                val size = data.size
//                val cp = ShortArray(size)
//                System.arraycopy(data, 0, cp, 0, size)
//                val p = PCM(size, cp)
//                mQueue.put(p)
//            } catch (e: Exception) {
//            }
//        }
//    }
//
//
//    fun release(){
//        bdRecognizer.unregisterListener(bdCallback)
//        bdRecognizer.destroy()
//        transformThread.exit()
//    }
//}