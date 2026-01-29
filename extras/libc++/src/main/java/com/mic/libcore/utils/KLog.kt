package com.mic.libcore.utils

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
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
object KLog {

    // 配置选项
    data class LogConfig(
        val tag: String = "TLog",
        val logDirectory: String? = null,  // 改为可空,使用应用私有目录时为 null
        val useDetailedFormat: Boolean = false,
        val enableStackTrace: Boolean = true,
        val stackDepth: Int = 1,
        val retentionDaysDebug: Long = 7,
        val retentionDaysRelease: Long = 3,
        val maxFileSize: Long = 10 * 1024 * 1024
    )

    private var isInitialized = false
    private var config: LogConfig = LogConfig()
    private lateinit var logDirectory: File  // 实际使用的日志目录

    // 获取当前类的包名,用于动态过滤
    private val currentPackageName: String by lazy {
        KLog::class.java.`package`?.name ?: "com.kk.core.utils"
    }

    // 获取当前类名
    private val currentClassName: String by lazy {
        KLog::class.java.name
    }

    // 自定义文件名生成器,按天生成日志文件
    private class DailyFileNameGenerator : com.elvishew.xlog.printer.file.naming.FileNameGenerator {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        override fun isFileNameChangeable(): Boolean = true

        override fun generateFileName(logLevel: Int, timestamp: Long): String {
            return "${dateFormat.format(Date(timestamp))}.log"
        }
    }

    // 自定义格式化器,包含时间戳
    private val customFlattener = object : ClassicFlattener() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        override fun flatten(time: Long, logLevel: Int, tag: String?, message: String): String {
            return "${dateFormat.format(Date(time))} ${getLevelString(logLevel)}/${tag ?: config.tag}: $message"
        }

        private fun getLevelString(logLevel: Int): String {
            return when (logLevel) {
                LogLevel.VERBOSE -> "V"
                LogLevel.DEBUG -> "D"
                LogLevel.INFO -> "I"
                LogLevel.WARN -> "W"
                LogLevel.ERROR -> "E"
                else -> "?"
            }
        }
    }

    // 详细格式化器
    private val detailedFlattener = object : ClassicFlattener() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        override fun flatten(time: Long, logLevel: Int, tag: String?, message: String): String {
            val timeStr = dateFormat.format(Date(time))
            val levelStr = getLevelName(logLevel)
            val threadName = Thread.currentThread().name
            return "[$timeStr] [$levelStr] [${threadName}] ${tag ?: config.tag}: $message"
        }

        private fun getLevelName(logLevel: Int): String {
            return when (logLevel) {
                LogLevel.VERBOSE -> "VERBOSE"
                LogLevel.DEBUG -> "DEBUG"
                LogLevel.INFO -> "INFO"
                LogLevel.WARN -> "WARN"
                LogLevel.ERROR -> "ERROR"
                else -> "UNKNOWN"
            }
        }
    }

    // 带堆栈信息的格式化器 - 改进版,支持Android Studio导航
    private val stackTraceFlattener = object : ClassicFlattener() {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

        override fun flatten(time: Long, logLevel: Int, tag: String?, message: String): String {
            val timeStr = dateFormat.format(Date(time))
            val levelStr = getLevelName(logLevel)
            val threadName = Thread.currentThread().name
            val stackInfo = getAndroidStudioNavigableStackTrace()

            return "[$timeStr] [$levelStr] [${threadName}] ${tag ?: config.tag}: $message$stackInfo"
        }

        private fun getLevelName(logLevel: Int): String {
            return when (logLevel) {
                LogLevel.VERBOSE -> "V"
                LogLevel.DEBUG -> "D"
                LogLevel.INFO -> "I"
                LogLevel.WARN -> "W"
                LogLevel.ERROR -> "E"
                else -> "U"
            }
        }

        private fun getAndroidStudioNavigableStackTrace(): String {
            if (config.stackDepth <= 0) return ""

            val stackTrace = Thread.currentThread().stackTrace
            val sb = StringBuilder()
            var depthCount = 0

            for (element in stackTrace) {
                if (depthCount >= config.stackDepth) break

                val className = element.className
                val methodName = element.methodName

                val isSystemClass = isSystemClassName(className)
                if (isSystemClass) {
                    continue
                }

                if (className.startsWith("com.elvishew.xlog")) {
                    continue
                }

                val isCurrentLogClass = isCurrentLogClass(className)
                if (isCurrentLogClass) {
                    continue
                }

                val isInternalFormatter = isInternalFormatterClass(className)
                if (isInternalFormatter) {
                    continue
                }

                val fileName = element.fileName
                val lineNumber = element.lineNumber

                val displayFileName = if (fileName != null && fileName.endsWith(".kt")) {
                    fileName
                } else {
                    className.substringAfterLast(".")
                }

                if (lineNumber > 0) {
                    sb.append("\n    at $className.$methodName($displayFileName:$lineNumber)")
                    depthCount++
                }
            }

            return sb.toString()
        }

        private fun isSystemClassName(className: String): Boolean {
            return className.startsWith("java.lang.Thread") ||
                    className.startsWith("dalvik.system") ||
                    className.startsWith("java.lang.reflect") ||
                    className.startsWith("sun.reflect") ||
                    className.startsWith("android.os.") ||
                    className.contains(".VMStack")
        }

        private fun isCurrentLogClass(className: String): Boolean {
            return className.startsWith(currentPackageName) &&
                    (className.contains("KLog") || className == currentClassName)
        }

        private fun isInternalFormatterClass(className: String): Boolean {
            if (!className.contains("\$")) return false
            return className.contains("\$stackTraceFlattener") ||
                    className.contains("\$customFlattener") ||
                    className.contains("\$detailedFlattener")
        }
    }

    /**
     * 初始化日志系统 - 使用应用私有目录
     * @param context 应用上下文
     * @param config 日志配置
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Context = AppGlobals.getApplication(), config: LogConfig = LogConfig()) {
        KLog.config = config

        // 使用应用私有外部存储目录 (无需权限)
        // 路径通常为: /storage/emulated/0/Android/data/com.kk.heartrate/files/Robot/Log
        logDirectory = if (config.logDirectory != null) {
            // 如果指定了自定义目录,使用自定义目录
            File(config.logDirectory, "Log")
        } else {
            // 使用应用私有目录 (推荐)
            File(context.getExternalFilesDir(null), "Robot/Log")
        }

        // 确保目录存在
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }

        val logConfig = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .tag(config.tag)
            .build()

        val selectedFlattener = when {
            config.enableStackTrace && config.stackDepth > 0 -> stackTraceFlattener
            config.useDetailedFormat -> detailedFlattener
            else -> customFlattener
        }

        val filePrinterBuilder = FilePrinter.Builder(logDirectory.absolutePath)
            .fileNameGenerator(DailyFileNameGenerator())
            .flattener(selectedFlattener)
            .backupStrategy(NeverBackupStrategy())

        val retentionDays = config.retentionDaysDebug
        filePrinterBuilder.cleanStrategy(FileLastModifiedCleanStrategy(retentionDays * 24 * 60 * 60 * 1000))

        XLog.init(
            logConfig,
            AndroidPrinter(true),
            filePrinterBuilder.build()
        )

        isInitialized = true
        i("LogUtils initialized")
        d("Log directory: ${logDirectory.absolutePath}")
        d("Log mode: Daily file, retention: ${retentionDays} days")
        d("Stack trace enabled: ${config.enableStackTrace}, depth: ${config.stackDepth}")
        d("Current package: $currentPackageName, Current class: $currentClassName")
    }

    /**
     * 简单的初始化方法 - 使用默认应用私有目录
     * @param context 应用上下文
     * @param tag 日志标签
     */
    @JvmStatic
    @JvmOverloads
    fun initSimple(context: Context = AppGlobals.getApplication(), tag: String = "TLog") {
        init(context, LogConfig(tag = tag))
    }

    /**
     * 兼容旧版本的初始化方法 - 已废弃
     * @deprecated 使用 init(context, config) 或 initSimple(context, tag) 代替
     */
    @Deprecated("Use init(context, config) or initSimple(context, tag) instead")
    @JvmStatic
    fun initLog() {
        init()
    }

    // 检查是否已初始化
    private fun checkInitialized() {
        if (!isInitialized) {
            init() // 使用默认配置自动初始化
        }
    }

    // 获取调用者的类名+方法名
    private fun getCallerClassName(): String {
        val stackTrace = Thread.currentThread().stackTrace

        for (element in stackTrace) {
            val className = element.className
            val methodName = element.methodName

            if (isSystemClassName(className)) {
                continue
            }

            if (className.startsWith("com.elvishew.xlog")) {
                continue
            }

            if (isCurrentLogClass(className)) {
                continue
            }

            val simpleClassName = className.substringAfterLast(".")
            return simpleClassName
        }

        return config.tag
    }

    // 基础日志方法 - 带堆栈信息
    @JvmStatic
    fun v(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.VERBOSE, callerTag, msg)
    }

    @JvmStatic
    fun d(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.DEBUG, callerTag, msg)
    }

    @JvmStatic
    fun i(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.INFO, callerTag, msg)
    }

    @JvmStatic
    fun w(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.WARN, callerTag, msg)
    }

    @JvmStatic
    fun e(msg: String, e: Throwable? = null) {
        checkInitialized()
        val callerTag = getCallerClassName()
        if (e != null) {
            logWithStackTrace(LogLevel.ERROR, callerTag, "$msg\n${e.stackTraceToString()}")
        } else {
            logWithStackTrace(LogLevel.ERROR, callerTag, msg)
        }
    }

    @JvmStatic
    fun e(msg: String, e: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.ERROR, callerTag, "$msg\n$e")
    }

    @JvmStatic
    fun e(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        logWithStackTrace(LogLevel.ERROR, callerTag, msg)
    }

    // 带自定义TAG的方法 - 带堆栈信息
    @JvmStatic
    fun v(tag: String, msg: String) {
        checkInitialized()
        logWithStackTrace(LogLevel.VERBOSE, tag, msg)
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        checkInitialized()
        logWithStackTrace(LogLevel.DEBUG, tag, msg)
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        checkInitialized()
        logWithStackTrace(LogLevel.INFO, tag, msg)
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        checkInitialized()
        logWithStackTrace(LogLevel.WARN, tag, msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String, e: Throwable? = null) {
        checkInitialized()
        if (e != null) {
            logWithStackTrace(LogLevel.ERROR, tag, "$msg\n${e.stackTraceToString()}")
        } else {
            logWithStackTrace(LogLevel.ERROR, tag, msg)
        }
    }

    // 带堆栈信息的日志方法
    private fun logWithStackTrace(logLevel: Int, tag: String, message: String) {
        val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
            val stackTrace = getFilteredStackTrace(config.stackDepth)
            if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
        } else {
            ""
        }

        val fullMessage = "$message$stackInfo"

        when (logLevel) {
            LogLevel.VERBOSE -> XLog.tag(tag).v(fullMessage)
            LogLevel.DEBUG -> XLog.tag(tag).d(fullMessage)
            LogLevel.INFO -> XLog.tag(tag).i(fullMessage)
            LogLevel.WARN -> XLog.tag(tag).w(fullMessage)
            LogLevel.ERROR -> XLog.tag(tag).e(fullMessage)
            else -> XLog.tag(tag).d(fullMessage)
        }
    }

    // 获取过滤后的堆栈信息
    private fun getFilteredStackTrace(maxDepth: Int = config.stackDepth): String {
        if (maxDepth <= 0) return ""

        val stackTrace = Thread.currentThread().stackTrace
        val sb = StringBuilder()
        var depthCount = 0

        for (element in stackTrace) {
            if (depthCount >= maxDepth) break

            val className = element.className
            val methodName = element.methodName

            val isSystemClass = isSystemClassName(className)
            if (isSystemClass) {
                continue
            }

            if (className.startsWith("com.elvishew.xlog")) {
                continue
            }

            val isCurrentLogClass = isCurrentLogClass(className)
            if (isCurrentLogClass) {
                continue
            }

            val isInternalOrLambda = isInternalOrLambdaClass(className)
            if (isInternalOrLambda) {
                continue
            }

            val fileName = element.fileName
            val lineNumber = element.lineNumber

            val displayFileName = if (fileName != null) {
                when {
                    fileName.endsWith(".kt") -> fileName
                    fileName.endsWith(".java") -> fileName
                    fileName.isNotEmpty() -> fileName
                    else -> className.substringAfterLast(".")
                }
            } else {
                className.substringAfterLast(".")
            }

            if (lineNumber > 0) {
                sb.append("    at $className.$methodName($displayFileName:$lineNumber)\n")
                depthCount++
            }
        }

        return sb.toString().trim()
    }

    private fun isSystemClassName(className: String): Boolean {
        return className.startsWith("java.lang.Thread") ||
                className.startsWith("dalvik.") ||
                className.startsWith("java.lang.reflect") ||
                className.startsWith("sun.reflect") ||
                className.startsWith("android.os.") ||
                className.contains(".VMStack") ||
                className == "java.lang.Thread" ||
                className.startsWith("java.util.concurrent")
    }

    private fun isCurrentLogClass(className: String): Boolean {
        return className.startsWith(currentPackageName) &&
                (className.contains("KLog") || className == currentClassName)
    }

    private fun isInternalOrLambdaClass(className: String): Boolean {
        if (!className.contains("\$")) return false
        return className.contains("\$stackTraceFlattener") ||
                className.contains("\$customFlattener") ||
                className.contains("\$detailedFlattener") ||
                className.contains("lambda\$") ||
                className.contains("\$Lambda\$")
    }

    // JSON 日志方法
    @JvmStatic
    fun json(json: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        try {
            val formattedJson = formatJsonSafely(json)
            val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
                val stackTrace = getFilteredStackTrace(config.stackDepth)
                if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
            } else {
                ""
            }
            XLog.tag(callerTag).json("$formattedJson$stackInfo")
        } catch (e: Exception) {
            XLog.tag(callerTag).d("Invalid JSON: $json")
        }
    }

    @JvmStatic
    fun json(tag: String, json: String) {
        checkInitialized()
        try {
            val formattedJson = formatJsonSafely(json)
            val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
                val stackTrace = getFilteredStackTrace(config.stackDepth)
                if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
            } else {
                ""
            }
            XLog.tag(tag).json("$formattedJson$stackInfo")
        } catch (e: Exception) {
            XLog.tag(tag).d("Invalid JSON: $json")
        }
    }

    // 安全的JSON格式化
    private fun formatJsonSafely(json: String): String {
        return try {
            val trimmed = json.trim()
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val jsonObject = JsonParser.parseString(trimmed).asJsonObject
                GsonBuilder().setPrettyPrinting().create().toJson(jsonObject)
            } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                val jsonArray = JsonParser.parseString(trimmed).asJsonArray
                GsonBuilder().setPrettyPrinting().create().toJson(jsonArray)
            } else {
                json
            }
        } catch (e: Exception) {
            json
        }
    }

    // 带堆栈信息的高级日志方法
    @JvmStatic
    fun trace(msg: String) {
        checkInitialized()
        val callerTag = getCallerClassName()
        val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
            val stackTrace = getFilteredStackTrace(config.stackDepth)
            if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
        } else {
            ""
        }
        XLog.tag(callerTag).d("$msg$stackInfo")
    }

    @JvmStatic
    fun trace(tag: String, msg: String) {
        checkInitialized()
        val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
            val stackTrace = getFilteredStackTrace(config.stackDepth)
            if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
        } else {
            ""
        }
        XLog.tag(tag).d("$msg$stackInfo")
    }

    // 记录大对象(避免过长的字符串)
    @JvmStatic
    fun large(tag: String, key: String, value: Any, maxLength: Int = 1000) {
        checkInitialized()
        val actualTag = if (tag == config.tag) getCallerClassName() else tag
        val valueStr = value.toString()
        val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
            val stackTrace = getFilteredStackTrace(config.stackDepth)
            if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
        } else {
            ""
        }

        if (valueStr.length <= maxLength) {
            XLog.tag(actualTag).d("$key: $valueStr$stackInfo")
        } else {
            XLog.tag(actualTag).d("$key (truncated): ${valueStr.substring(0, maxLength)}...$stackInfo")
        }
    }

    // 批量日志记录
    @JvmStatic
    fun batch(tag: String, messages: List<String>) {
        checkInitialized()
        val actualTag = if (tag == config.tag) getCallerClassName() else tag
        messages.forEach { message ->
            val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
                val stackTrace = getFilteredStackTrace(config.stackDepth)
                if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
            } else {
                ""
            }
            XLog.tag(actualTag).i("$message$stackInfo")
        }
    }

    // 条件日志记录
    @JvmStatic
    fun dIf(condition: Boolean, tag: String, message: () -> String) {
        if (condition) {
            checkInitialized()
            val actualTag = if (tag == config.tag) getCallerClassName() else tag
            val stackInfo = if (config.enableStackTrace && config.stackDepth > 0) {
                val stackTrace = getFilteredStackTrace(config.stackDepth)
                if (stackTrace.isNotEmpty()) "        $stackTrace" else ""
            } else {
                ""
            }
            XLog.tag(actualTag).d("${message()}$stackInfo")
        }
    }

    // 文件管理方法 - 更新为使用应用私有目录
    @JvmStatic
    fun getLogDirectory(context: Context = AppGlobals.getApplication()): File {
        return if (::logDirectory.isInitialized) {
            logDirectory
        } else {
            // 如果未初始化,返回默认应用私有目录
            File(context.getExternalFilesDir(null), "Robot/Log")
        }
    }

    @JvmStatic
    fun getCurrentLogFile(context: Context = AppGlobals.getApplication()): File? {
        val logDir = getLogDirectory(context)
        if (!logDir.exists()) return null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        return File(logDir, "$currentDate.log")
    }

    @JvmStatic
    fun getAllLogFiles(context: Context = AppGlobals.getApplication()): List<File> {
        val logDir = getLogDirectory(context)
        return if (logDir.exists() && logDir.isDirectory) {
            logDir.listFiles { file -> file.isFile && file.name.endsWith(".log") }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    @JvmStatic
    fun cleanLogs(context: Context = AppGlobals.getApplication()) {
        val logDir = getLogDirectory(context)
        if (logDir.exists() && logDir.isDirectory) {
            logDir.listFiles()?.forEach { it.delete() }
            i("Logs cleaned successfully")
        }
    }

    @JvmStatic
    fun getCurrentLogFileSize(context: Context = AppGlobals.getApplication()): Long {
        return getCurrentLogFile(context)?.length() ?: 0L
    }

    @JvmStatic
    fun getLogStats(context: Context = AppGlobals.getApplication()): String {
        val logDir = getLogDirectory(context)
        val files = getAllLogFiles(context)
        val totalSize = files.sumOf { it.length() }

        return "Log files: ${files.size}, Total size: ${"%.2f".format(totalSize / 1024.0 / 1024.0)} MB, " +
                "Current file: ${getCurrentLogFileSize(context)} bytes"
    }
}
