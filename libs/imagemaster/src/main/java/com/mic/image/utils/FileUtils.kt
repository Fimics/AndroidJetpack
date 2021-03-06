package com.mic.image.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.mic.image.utils.NotifyUtils.Companion.refreshSystemPic
import java.io.*
import java.math.BigDecimal

/**
 * describe
 * @author lipengju
 */
class FileUtils {

    companion object {
        /**
         * 创建需要保存的文件
         * @param isUseExternalFilesDir 是否使用getExternalFilesDir,false为保存在sdcard根目录下
         * @param fileName 保存文件名
         * @param folderName 保存在sdcard根目录下的文件夹名（isUseExternalFilesDir=false时需要）
         */

        @JvmStatic
        fun savaFileUtils(
            context: Context,
            isUseExternalFilesDir: Boolean,
            fileName: String,
            folderName: String = ""
        ): File {
            val filePath = if (isUseExternalFilesDir) {
                    context.getExternalFilesDir(folderName)?.absolutePath!!
                } else {
                    Environment.getExternalStorageDirectory().absolutePath
                }

            return if (isUseExternalFilesDir) {
                File(filePath, fileName)
            } else {
                val file = File(filePath, folderName!!)
                if (!file.exists()) {
                    file.mkdirs()
                }
                File(file, fileName)
            }
        }


        /**
         * bitmap保存到File
         */
        @JvmStatic
        fun saveBitmap2File(
            context: Context,
            bitmap: Bitmap,
            file: File
        ) {
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            //通知系统图库更新
            refreshSystemPic(context, file)
        }

        /**
         * 复制文件
         *
         * @param source 输入文件
         * @param target 输出文件
         */
        @JvmStatic
        fun copy(source: File?, target: File?) {
            var fileInputStream: FileInputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                fileInputStream = FileInputStream(source)
                fileOutputStream = FileOutputStream(target)
                val buffer = ByteArray(1024)
                while (fileInputStream.read(buffer) > 0) {
                    fileOutputStream.write(buffer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    source?.delete()
                    fileInputStream?.close()
                    fileOutputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        @JvmStatic
        @Throws(Exception::class)
        fun getFolderSize(file: File): Long {
            var size: Long = 0
            try {
                val fileList = file.listFiles()
                for (aFileList in fileList) {
                    size = if (aFileList.isDirectory) {
                        size + getFolderSize(aFileList)
                    } else {
                        size + aFileList.length()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return size
        }

        @JvmStatic
        fun getFormatSize(size: Double): String {
            val kiloByte = size / 1024
            if (kiloByte < 1) {
                return size.toString() + "Byte"
            }
            val megaByte = kiloByte / 1024
            if (megaByte < 1) {
                val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
                return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
            }
            val gigaByte = megaByte / 1024
            if (gigaByte < 1) {
                val result2 = BigDecimal(java.lang.Double.toString(megaByte))
                return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
            }
            val teraBytes = gigaByte / 1024
            if (teraBytes < 1) {
                val result3 = BigDecimal(java.lang.Double.toString(gigaByte))
                return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
            }
            val result4 = BigDecimal(teraBytes)
            return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
        }
    }
}


