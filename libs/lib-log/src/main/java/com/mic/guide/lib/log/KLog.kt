package com.mic.guide.lib.log

import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mic.guide.lib.common.AppGlobals
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文件日志门面（迁移自 libcore `KLog`，转 Kotlin 包名）。
 *
 * 基于 elvishew xlog：控制台 + 按天落盘（应用私有目录，无需权限）+ 调用栈定位 + JSON 美化。
 * 全工程统一日志入口是 [Logger]，[Logger] 内部委托本类，二者已合并（不再各写一套）。
 */
@Suppress("unused")
object KLog {

    data class LogConfig(
        val tag: String = "AiGuide",
        val logDirectory: String? = null,
        val useDetailedFormat: Boolean = false,
        val enableStackTrace: Boolean = true,
        val stackDepth: Int = 1,
        val retentionDaysDebug: Long = 7,
        val retentionDaysRelease: Long = 3,
        val maxFileSize: Long = 10 * 1024 * 1024,
    )

    private var isInitialized = false
    private var config: LogConfig = LogConfig()
    private lateinit var logDirectory: File

    private val currentPackageName: String by lazy {
        KLog::class.java.`package`?.name ?: "com.mic.guide.lib.log"
    }
    private val currentClassName: String by lazy { KLog::class.java.name }

    private class DailyFileNameGenerator :
        com.elvishew.xlog.printer.file.naming.FileNameGenerator {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        override fun isFileNameChangeable(): Boolean = true
        override fun generateFileName(logLevel: Int, timestamp: Long): String =
            "${dateFormat.format(Date(timestamp))}.log"
    }

    private val customFlattener = object : ClassicFlattener() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        override fun flatten(time: Long, logLevel: Int, tag: String?, message: String): String =
            "${dateFormat.format(Date(time))} ${levelShort(logLevel)}/${tag ?: config.tag}: $message"
    }

    private val stackTraceFlattener = object : ClassicFlattener() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        override fun flatten(time: Long, logLevel: Int, tag: String?, message: String): String {
            val timeStr = dateFormat.format(Date(time))
            val threadName = Thread.currentThread().name
            return "[$timeStr] [${levelShort(logLevel)}] [$threadName] ${tag ?: config.tag}: $message"
        }
    }

    private fun levelShort(logLevel: Int): String = when (logLevel) {
        LogLevel.VERBOSE -> "V"
        LogLevel.DEBUG -> "D"
        LogLevel.INFO -> "I"
        LogLevel.WARN -> "W"
        LogLevel.ERROR -> "E"
        else -> "?"
    }

    @JvmStatic
    @JvmOverloads
    fun init(context: Context = AppGlobals.getApplication(), config: LogConfig = LogConfig()) {
        KLog.config = config
        logDirectory = if (config.logDirectory != null) {
            File(config.logDirectory, "Log")
        } else {
            File(context.getExternalFilesDir(null), "AiGuide/Log")
        }
        if (!logDirectory.exists()) logDirectory.mkdirs()

        val logConfig = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .tag(config.tag)
            .build()

        val flattener = if (config.enableStackTrace) stackTraceFlattener else customFlattener
        val filePrinter = FilePrinter.Builder(logDirectory.absolutePath)
            .fileNameGenerator(DailyFileNameGenerator())
            .flattener(flattener)
            .backupStrategy(NeverBackupStrategy())
            .cleanStrategy(FileLastModifiedCleanStrategy(config.retentionDaysDebug * 24 * 60 * 60 * 1000))
            .build()

        XLog.init(logConfig, AndroidPrinter(true), filePrinter)
        isInitialized = true
        i("KLog initialized, dir=${logDirectory.absolutePath}")
    }

    @JvmStatic
    @JvmOverloads
    fun initSimple(context: Context = AppGlobals.getApplication(), tag: String = "AiGuide") {
        init(context, LogConfig(tag = tag))
    }

    private fun checkInitialized() {
        if (!isInitialized) init()
    }

    private fun callerTag(): String {
        for (element in Thread.currentThread().stackTrace) {
            val className = element.className
            if (isSystemClass(className) || className.startsWith("com.elvishew.xlog")) continue
            if (className.startsWith(currentPackageName)) continue
            return className.substringAfterLast(".")
        }
        return config.tag
    }

    private fun isSystemClass(className: String): Boolean =
        className.startsWith("java.lang.Thread") ||
            className.startsWith("dalvik.") ||
            className.startsWith("java.lang.reflect") ||
            className.startsWith("android.os.") ||
            className.contains(".VMStack") ||
            className.startsWith("java.util.concurrent")

    @JvmStatic fun v(msg: String) { checkInitialized(); XLog.tag(callerTag()).v(msg) }
    @JvmStatic fun d(msg: String) { checkInitialized(); XLog.tag(callerTag()).d(msg) }
    @JvmStatic fun i(msg: String) { checkInitialized(); XLog.tag(callerTag()).i(msg) }
    @JvmStatic fun w(msg: String) { checkInitialized(); XLog.tag(callerTag()).w(msg) }

    @JvmStatic
    @JvmOverloads
    fun e(msg: String, throwable: Throwable? = null) {
        checkInitialized()
        val full = if (throwable != null) "$msg\n${throwable.stackTraceToString()}" else msg
        XLog.tag(callerTag()).e(full)
    }

    @JvmStatic fun v(tag: String, msg: String) { checkInitialized(); XLog.tag(tag).v(msg) }
    @JvmStatic fun d(tag: String, msg: String) { checkInitialized(); XLog.tag(tag).d(msg) }
    @JvmStatic fun i(tag: String, msg: String) { checkInitialized(); XLog.tag(tag).i(msg) }
    @JvmStatic fun w(tag: String, msg: String) { checkInitialized(); XLog.tag(tag).w(msg) }

    @JvmStatic
    @JvmOverloads
    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        checkInitialized()
        val full = if (throwable != null) "$msg\n${throwable.stackTraceToString()}" else msg
        XLog.tag(tag).e(full)
    }

    @JvmStatic
    fun json(json: String) {
        checkInitialized()
        runCatching { XLog.tag(callerTag()).json(formatJson(json)) }
            .onFailure { XLog.tag(callerTag()).d("Invalid JSON: $json") }
    }

    private fun formatJson(json: String): String = try {
        val trimmed = json.trim()
        val gson = GsonBuilder().setPrettyPrinting().create()
        when {
            trimmed.startsWith("{") -> gson.toJson(JsonParser.parseString(trimmed).asJsonObject)
            trimmed.startsWith("[") -> gson.toJson(JsonParser.parseString(trimmed).asJsonArray)
            else -> json
        }
    } catch (e: Exception) {
        json
    }

    // ---------- 日志文件管理 ----------

    @JvmStatic
    @JvmOverloads
    fun getLogDirectory(context: Context = AppGlobals.getApplication()): File =
        if (::logDirectory.isInitialized) logDirectory
        else File(context.getExternalFilesDir(null), "AiGuide/Log")

    @JvmStatic
    @JvmOverloads
    fun getAllLogFiles(context: Context = AppGlobals.getApplication()): List<File> {
        val dir = getLogDirectory(context)
        return if (dir.isDirectory) dir.listFiles { f -> f.isFile && f.name.endsWith(".log") }?.toList().orEmpty()
        else emptyList()
    }

    @JvmStatic
    @JvmOverloads
    fun cleanLogs(context: Context = AppGlobals.getApplication()) {
        getLogDirectory(context).takeIf { it.isDirectory }?.listFiles()?.forEach { it.delete() }
    }
}
