package com.mic.guide.lib.common

import android.content.Context
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

/**
 * 文件工具（**合并自 libcore 的 `FileUtils` / `FileIOUtils` / `FileUtil`(静态) /
 * `FucUtil` / `CopyAssetsUtils` / `FileTools`**，去重后转 Kotlin）。
 *
 * 原先 6 个类有大量重复的「判断存在 / 创建 / 删除 / 读写 / 拷贝 assets」逻辑，这里统一成一处。
 * 流式 PCM 写入这类有状态的能力见 [PcmWriter]。
 */
object FileUtils {

    private const val BUFFER_SIZE = 8192

    // ---------- 基础判断 / 创建 ----------

    fun isFileExists(path: String?): Boolean = path?.let { File(it).exists() } ?: false
    fun isFileExists(file: File?): Boolean = file != null && file.exists()

    fun isDir(file: File?): Boolean = file != null && file.exists() && file.isDirectory
    fun isFile(file: File?): Boolean = file != null && file.exists() && file.isFile

    fun createOrExistsDir(file: File?): Boolean =
        file != null && (if (file.exists()) file.isDirectory else file.mkdirs())

    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile
        if (!createOrExistsDir(file.parentFile)) return false
        return try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun rename(file: File?, newName: String): Boolean {
        if (file == null || !file.exists() || newName.isBlank()) return false
        if (newName == file.name) return true
        val newFile = File(file.parent, newName)
        return !newFile.exists() && file.renameTo(newFile)
    }

    // ---------- 删除 ----------

    fun deleteFile(path: String?): Boolean = path?.let { deleteFile(File(it)) } ?: false
    fun deleteFile(file: File?): Boolean =
        file != null && (!file.exists() || (file.isFile && file.delete()))

    fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        if (!dir.exists()) return true
        if (!dir.isDirectory) return false
        dir.listFiles()?.forEach { child ->
            val ok = if (child.isFile) child.delete() else deleteDir(child)
            if (!ok) return false
        }
        return dir.delete()
    }

    /** 删除目录下内容（保留目录本身）。 */
    fun deleteFilesInDir(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return dir == null || true
        if (!dir.isDirectory) return false
        dir.listFiles()?.forEach { child ->
            val ok = if (child.isFile) child.delete() else deleteDir(child)
            if (!ok) return false
        }
        return true
    }

    // ---------- 列举 / 大小 ----------

    @JvmOverloads
    fun listFilesInDir(dir: File?, recursive: Boolean = false): List<File> {
        if (!isDir(dir)) return emptyList()
        val list = mutableListOf<File>()
        dir!!.listFiles()?.forEach { file ->
            list.add(file)
            if (recursive && file.isDirectory) list.addAll(listFilesInDir(file, true))
        }
        return list
    }

    fun getDirLength(dir: File?): Long {
        if (!isDir(dir)) return -1
        var len = 0L
        dir!!.listFiles()?.forEach { len += if (it.isDirectory) getDirLength(it) else it.length() }
        return len
    }

    fun getFileLength(file: File?): Long = if (isFile(file)) file!!.length() else -1

    /** 字节数 → 友好大小（KB/MB/GB，保留两位）。 */
    fun formatSize(byteNum: Long): String = when {
        byteNum < 0 -> ""
        byteNum < 1024 -> "%.2fB".format(byteNum.toDouble())
        byteNum < 1024 * 1024 -> "%.2fKB".format(byteNum / 1024.0)
        byteNum < 1024 * 1024 * 1024 -> "%.2fMB".format(byteNum / 1024.0 / 1024.0)
        else -> "%.2fGB".format(byteNum / 1024.0 / 1024.0 / 1024.0)
    }

    // ---------- 文件名解析 ----------

    fun getFileName(path: String?): String =
        path?.substringAfterLast(File.separatorChar).orEmpty()

    fun getFileNameNoExtension(path: String?): String =
        getFileName(path).substringBeforeLast('.', getFileName(path))

    fun getFileExtension(path: String?): String {
        val name = getFileName(path)
        return if (name.contains('.')) name.substringAfterLast('.') else ""
    }

    // ---------- 读 ----------

    fun readFile2String(file: File?): String? =
        if (isFileExists(file)) runCatching { file!!.readText() }.getOrNull() else null

    fun readFile2String(path: String?): String? = readFile2String(path?.let { File(it) })

    fun readFile2Bytes(file: File?): ByteArray? =
        if (isFileExists(file)) runCatching { file!!.readBytes() }.getOrNull() else null

    fun readFile2List(file: File?): List<String> =
        if (isFileExists(file)) runCatching { file!!.readLines() }.getOrDefault(emptyList()) else emptyList()

    // ---------- 写 ----------

    @JvmOverloads
    fun writeFile(file: File?, bytes: ByteArray?, append: Boolean = false): Boolean {
        if (bytes == null || !createOrExistsFile(file)) return false
        return try {
            BufferedOutputStream(FileOutputStream(file, append)).use { it.write(bytes) }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    @JvmOverloads
    fun writeFile(path: String, bytes: ByteArray?, append: Boolean = false): Boolean =
        writeFile(File(path), bytes, append)

    @JvmOverloads
    fun writeFile(file: File?, content: String?, append: Boolean = false): Boolean {
        if (content == null || !createOrExistsFile(file)) return false
        return try {
            if (append) file!!.appendText(content) else file!!.writeText(content)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    @JvmOverloads
    fun writeFileFromIS(file: File?, input: InputStream?, append: Boolean = false): Boolean {
        if (input == null || !createOrExistsFile(file)) return false
        return try {
            BufferedOutputStream(FileOutputStream(file, append)).use { os ->
                input.copyTo(os, BUFFER_SIZE)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            runCatching { input.close() }
        }
    }

    // ---------- MD5 ----------

    fun getFileMD5(file: File?): String? {
        if (!isFileExists(file)) return null
        return try {
            val md = MessageDigest.getInstance("MD5")
            file!!.inputStream().use { fis ->
                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (fis.read(buffer).also { len = it } > 0) md.update(buffer, 0, len)
            }
            md.digest().joinToString("") { "%02X".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------- 拷贝 ----------

    fun copyFile(src: File?, dest: File?): Boolean {
        if (src == null || dest == null || !src.exists() || !src.isFile) return false
        if (!createOrExistsDir(dest.parentFile)) return false
        return try {
            src.copyTo(dest, overwrite = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ---------- assets ----------

    /** 读取 assets 文本内容（合并自 FucUtil.readAssetFile / FileUtil.readFile）。 */
    @JvmOverloads
    fun readAssetText(context: Context, assetPath: String, charset: String = "UTF-8"): String =
        runCatching {
            context.assets.open(assetPath).use { String(it.readBytes(), charset(charset)) }
        }.getOrDefault("")

    /** 读取 assets 字节内容。 */
    fun readAssetBytes(context: Context, assetPath: String): ByteArray? =
        runCatching { context.assets.open(assetPath).use { it.readBytes() } }.getOrNull()

    /**
     * 拷贝 assets 文件或整个目录到目标路径（合并自 FucUtil/CopyAssetsUtils/FileTools 的多份实现）。
     * @return 是否全部成功
     */
    fun copyAssets(context: Context, assetPath: String, destPath: String): Boolean {
        return try {
            val children = context.assets.list(assetPath)
            if (children.isNullOrEmpty()) {
                copyAssetFile(context, assetPath, destPath)
            } else {
                File(destPath).mkdirs()
                children.all {
                    copyAssets(context, "$assetPath/$it", destPath + File.separator + it)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun copyAssetFile(context: Context, assetName: String, destPath: String): Boolean = try {
        val outFile = File(destPath)
        outFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
        context.assets.open(assetName).use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
