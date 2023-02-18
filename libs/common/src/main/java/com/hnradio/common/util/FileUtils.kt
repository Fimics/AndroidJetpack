package com.hnradio.common.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.hnradio.common.util.zxing.encoding.EncodingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.*
import java.text.DecimalFormat
import java.util.*
import kotlin.Comparator
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @Description: 文件&图片的处理
 * @Author: huqiang
 * @CreateDate: 2021-09-27 11:33
 * @Version: 1.0
 */
object FileUtils {
    /**
     * 获取图片bitmap
     */
    suspend fun getNetWorkImage(context: Context, imgUrl: String, needCompression: Boolean = true) =
        withContext(Dispatchers.IO) {
            supervisorScope {
                try {
                    val builder = Glide.with(context)
                        .asBitmap()
                        .load(imgUrl)
                    if (needCompression) {
                        builder.submit(480, 480).get()
                    } else {
                        builder.submit().get()
                    }
                } catch (e: Exception) {
                    null
                }


            }
        }

    suspend fun getNetWorkImageFile(context: Context, imgUrl: String): String =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                Glide.with(context)
                    .downloadOnly()
                    .load(imgUrl)
                    .listener(object : RequestListener<File> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<File>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: File?,
                            model: Any?,
                            target: Target<File>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            continuation.resume(resource?.absolutePath.toString())
                            return false
                        }
                    })
                    .preload()
            }
        }

    /**
     * 保存bitmap
     */
    suspend fun saveImage(bmp: Bitmap, context: Context, fileName: String) =
        withContext(Dispatchers.IO) {
            supervisorScope {
                // 首先保存图片
                val file_path = context.externalCacheDir.toString() + File.separator + fileName
                File(file_path).mkdirs()
                val fileName =
                    file_path + File.separator + System.currentTimeMillis().toString() + ".jpg"
                val file = File(fileName)
                try {
                    val fos = FileOutputStream(file)
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                    fos.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                file
            }
        }

    /**
     *  生成二维码
     */
    suspend fun getQRCodeImage(url: String, widthPix: Int, heightPix: Int, logoBm: Bitmap?) =
        withContext(Dispatchers.IO) {
            supervisorScope {
                try {
                    EncodingUtils.createQRCode(url, widthPix, heightPix, logoBm)
                } catch (e: Exception) {
                    null
                }
            }
        }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    fun getFileByPath(filePath: String?): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    private fun isSpace(s: String?): Boolean {
        if (s == null) {
            return true
        }
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }


    fun getFileSize(size: Long): String? {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }


    fun listDirFiles(path: String?, filter: FileFilter?): List<File?>? {
        val directory = File(path)
        val files = directory.listFiles(filter) ?: return ArrayList()
        val result: List<File?> = ArrayList(Arrays.asList(*files))
        Collections.sort(result, Comparator { f1, f2 ->
            if (f1 === f2) {
                return@Comparator 0
            }
            if (f1!!.isDirectory && f2!!.isFile) {
                // Show directories above files
                return@Comparator -1
            }
            if (f1.isFile && f2!!.isDirectory) {
                // Show files below directories
                1
            } else f1.name.compareTo(f2!!.name, ignoreCase = true)
            // Sort the directories alphabetically
        })
        return result
    }
}