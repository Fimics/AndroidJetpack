package com.mic.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream


class FileTools {

    interface DataSourceCallBack{
        fun onCompleted()
    }

    companion object {
        val TAG:String="data"
        //TODO  如果已经有最新的数据就不用再次拷贝了 MD5实现
        fun copyDir(dir: String, context: Context,callback:DataSourceCallBack) {
            var array: Array<String> = context.resources.assets.list(dir) as Array<String>
            array.forEach {

                val fileName = StringBuilder(dir).append("/").append(it).toString()
                var data = File(context?.let { it1 -> getStorageDir() }, fileName)
                if(data!=null &&data.exists()){
                    Log.d(TAG, data.absolutePath)
                    Log.d(TAG, data.readText())
                }else{
                    copyAssetsFile(dir, it, context)
                    Log.d(TAG,it+" onCompleted...")
                }

            }
            callback.onCompleted();
            Log.d(TAG,"all data copy onCompleted...")
        }

        fun copyAssetsFile(dir: String, fileName: String, content: Context): Boolean {
            try {
                val cacheDir = File(getStorageDir(), dir)
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }

                val outFile = File(cacheDir, fileName)
                if (!outFile.exists()) {
                    val res = outFile.createNewFile()
                    if (!res) {
                        return false
                    }
                } else {
                    if (outFile.length() > 10) {
                        return true
                    }
                }

                val fileName = StringBuilder(dir).append("/").append(fileName).toString()
                val inputStream = content.resources.assets.open(fileName)
                val fos = FileOutputStream(outFile)
                var buffer = ByteArray(1024 * 4)
                var byteCount: Int

                while (inputStream.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }

                fos.flush()
                inputStream.close()
                fos.close()
                return true

            } catch (e: Exception) {
                print(e.message)
            }
            return false
        }

        fun getStorageDir(): String {
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
            } else {
                Environment.getExternalStorageDirectory().absolutePath
            }
        }
    }
}