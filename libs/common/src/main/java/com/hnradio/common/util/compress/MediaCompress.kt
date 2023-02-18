package com.hnradio.common.util.compress

//import com.iceteck.silicompressorr.SiliCompressor

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.hnradio.common.util.L
import com.iceteck.silicompressorr.SiliCompressor
import com.iceteck.silicompressorr.VideoController
import java.io.File
import java.util.concurrent.Executors


class MediaCompress private constructor(){

    private val pool = Executors.newFixedThreadPool(1)
    private val mHandler = Handler(Looper.getMainLooper())

    companion object{
        val instance : MediaCompress by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MediaCompress() }
    }

    fun compressVideo(context:  Context, path : String, callback : (ret : String)->Unit, progress : (p : Int)->Unit = {}){
        pool.execute {

            val dir = File(context.getExternalFilesDir(null), "silicom")
            if(!dir.exists()){
                dir.mkdirs()
            }
            val compressFile = File(dir, "compress.mp4")
            if(compressFile.exists()){
                compressFile.createNewFile()
            }else{
                compressFile.delete()
            }

            val ret = VideoController.getInstance().convertVideo(path, compressFile.absolutePath,
                VideoController.COMPRESS_QUALITY_LOW) { percent ->
                mHandler.post { progress(percent.toInt()) }
            }
            mHandler.post { callback(if(ret) compressFile.absolutePath else "") }

//            try {
//                val filePath = SiliCompressor.with(context).compressVideo(Uri.fromFile(File(path)), dir.absolutePath)
//                mHandler.post { callback(filePath) }
//                L.e("compress video path = $filePath")
//            }catch (E : Exception){
//                L.e("compress error ${E.localizedMessage}")
//            }
        }

        /*try {
            val dir = File(context.getExternalFilesDir(null), "compress")
            if(!dir.exists()){
                dir.mkdirs()
            }
            val compressFile = File(dir, "compress.mp4")
            if(compressFile.exists()){
                compressFile.createNewFile()
            }

            val listener: VideoCompressor.Listener = object : VideoCompressor.Listener {

                override fun onTranscodeProgress(pg: Double) {
                    mHandler.post { progress((pg * 100).toInt()) }
                }

                override fun onTranscodeCompleted() {
                    val compressPath: String = compressFile.getAbsolutePath()
                    mHandler.post { callback(compressPath) }
                }

                override fun onTranscodeCanceled() {}

                override fun onTranscodeFailed(exception: Exception) {}
            }
            VideoCompressor.with().asyncTranscodeVideo(path, compressFile.absolutePath,
                MediaFormatStrategyPresets.createAndroid480pFormatStrategy(), listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }
}