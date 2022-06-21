package com.mic.image.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import java.io.*

/**
 * describe
 * @author lipengju
 */
class NotifyUtils {
    /**
     * 通知系统相册更新
     */
    companion object {

        @JvmStatic
        fun refreshSystemPic(context: Context, destFile: File) {

                val value = ContentValues()
                value.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                value.put(MediaStore.Images.Media.DATA, destFile.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
                val contentUri = FileProviderUtil.getFileUri(
                    context,
                    destFile,
                    context.packageName + ".fileProvider"
                );
                context.sendBroadcast(
                        Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                contentUri
                        )
                )
        }


        /**
         * Android Q以后向系统相册插入图片
         */
        @JvmStatic
        private fun insertPicInAndroidQ(context: Context, insertFile: File) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DESCRIPTION, insertFile.name)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, insertFile.name)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            values.put(MediaStore.Images.Media.TITLE, "Image.jpg")

            val external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val resolver: ContentResolver = context.contentResolver
            val insertUri = resolver.insert(external, values)
            var inputStream: BufferedInputStream?
            var os: OutputStream? = null
            try {
                inputStream = BufferedInputStream(FileInputStream(insertFile))
                if (insertUri != null) {
                    os = resolver.openOutputStream(insertUri)
                }
                if (os != null) {
                    val buffer = ByteArray(1024 * 4)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        os.write(buffer, 0, len)
                    }
                    os.flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                os?.close()
            }
        }
    }

}
