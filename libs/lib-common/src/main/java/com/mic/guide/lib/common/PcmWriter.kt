package com.mic.guide.lib.common

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * PCM 流式写入器（**合并自 libcore `FileUtil` / `PcmFileUtils` 的有状态部分**，转 Kotlin）。
 *
 * 两种用法：
 * 1. 单文件：[create] → [write] → [close]（按时间戳命名）。
 * 2. 多路并发：[writeById] 按 messageId 各自追加到独立文件、各自加锁，[close](id) / [closeAll]。
 *
 * 语音采集/录音场景常用；目录由构造参数指定。
 */
class PcmWriter(private val writeDir: String) {

    private var fos: FileOutputStream? = null
    private val streams = ConcurrentHashMap<String, FileOutputStream>()
    private val locks = ConcurrentHashMap<String, Any>()

    /** 创建单文件输出流（时间戳命名）。 */
    fun create() {
        ensureDir()
        if (fos != null) return
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA).format(Date())
        val pcm = File(writeDir, "$name$SUFFIX")
        try {
            if (pcm.createNewFile()) fos = FileOutputStream(pcm)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun write(data: ByteArray, offset: Int = 0, len: Int = data.size) {
        try {
            fos?.write(data, offset, len)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun close() {
        runCatching { fos?.close() }
        fos = null
    }

    /** 按 messageId 流式追加。 */
    fun writeById(messageId: String, data: ByteArray, offset: Int, len: Int) {
        if (messageId.isEmpty() || len <= 0) return
        ensureDir()
        val id = sanitize(messageId)
        synchronized(locks.getOrPut(id) { Any() }) {
            val stream = streams.getOrPut(id) {
                FileOutputStream(File(writeDir, "$id$SUFFIX"), /* append = */ true)
            }
            try {
                stream.write(data, offset, len)
                stream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                close(messageId)
            }
        }
    }

    fun close(messageId: String) {
        val id = sanitize(messageId)
        synchronized(locks.getOrPut(id) { Any() }) {
            streams.remove(id)?.let { runCatching { it.close() } }
        }
    }

    fun closeAll() {
        streams.keys.toList().forEach { close(it) }
        streams.clear()
    }

    private fun ensureDir() {
        File(writeDir).takeIf { !it.exists() }?.mkdirs()
    }

    private fun sanitize(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")

    companion object {
        private const val SUFFIX = ".pcm"
    }
}
