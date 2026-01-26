package com.mic.libcore.utils

import android.content.Context
import android.os.Looper
import android.widget.Toast
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 线程未捕获异常处理器
 * 功能：
 * 1. 捕获未处理异常
 * 2. 可选的上报逻辑
 * 3. 防止程序直接崩溃（可选）
 */

@Suppress("unused")
class ThreadUncaughtHandler private constructor(context: Context) : Thread.UncaughtExceptionHandler {


    private val appContext: Context = context.applicationContext
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    private var crashListener: CrashListener? = null
    private var showToast: Boolean = true
    private var toastMessage: String = "程序出现异常，即将退出"

    companion object {
        private const val TAG = "ThreadUncaughtHandler"
        @Volatile
        private var instance: ThreadUncaughtHandler? = null

        /**
         * 初始化并安装异常处理器（推荐在Application中调用）
         */
        fun install(context: Context) {
            if (instance == null) {
                synchronized(ThreadUncaughtHandler::class.java) {
                    if (instance == null) {
                        KLog.d(TAG,"install")
                        instance = ThreadUncaughtHandler(context)
                    }
                }
            }
        }

        /**
         * 获取实例（在安装后使用）
         */
        fun getInstance(): ThreadUncaughtHandler {
            return instance ?: throw IllegalStateException("ThreadUncaughtHandler 未初始化，请先调用 install() 方法")
        }

        /**
         * 设置崩溃监听器（可选）
         */
        fun setCrashListener(listener: CrashListener) {
            instance?.setListener(listener)
        }

        /**
         * 设置是否显示Toast提示（默认显示）
         */
        fun setShowToast(show: Boolean) {
            instance?.setToastEnabled(show)
        }

        /**
         * 设置Toast提示消息（默认："程序出现异常，即将退出"）
         */
        fun setToastMessage(message: String) {
            instance?.setToastMessage(message)
        }
    }

    init {
        // 设置当前处理器为默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 设置崩溃监听器
     */
    private fun setListener(listener: CrashListener) {
        this.crashListener = listener
    }

    /**
     * 设置是否显示Toast
     */
    private fun setToastEnabled(enabled: Boolean) {
        this.showToast = enabled
    }

    /**
     * 设置Toast消息
     */
    private fun setToastMessage(message: String) {
        this.toastMessage = message
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // 处理异常
        handleException(thread, exception)

        // 如果系统提供了默认的异常处理器，则交给系统去结束程序
        defaultHandler?.uncaughtException(thread, exception) ?: run {
            // 否则直接退出程序
            android.os.Process.killProcess(android.os.Process.myPid())
//            System.exit(1)
        }
    }

    /**
     * 自定义异常处理
     */
    private fun handleException(thread: Thread, exception: Throwable) {
        if (exception == null) return

        // 在UI线程显示Toast（可选）
        if (showToast) {
            showToastOnUiThread()
        }

        // 生成崩溃日志信息
        val crashInfo = generateCrashInfo(thread, exception)
        
        // 打印到Logcat
        printToLogcat(crashInfo)
        
        // 回调监听器
        notifyCrashListener(thread, exception, crashInfo)
    }

    /**
     * 在UI线程显示Toast
     */
    private fun showToastOnUiThread() {
        Thread {
            Looper.prepare()
            Toast.makeText(appContext, toastMessage, Toast.LENGTH_LONG).show()
            Looper.loop()
        }.start()

        // 等待Toast显示
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 生成崩溃信息
     */
    private fun generateCrashInfo(thread: Thread, exception: Throwable): CrashInfo {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // 获取堆栈信息
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        
        // 获取设备基本信息
        val deviceInfo = getDeviceInfo()

        return CrashInfo(
            timestamp = time,
            threadName = thread.name,
            exceptionType = exception.javaClass.name,
            exceptionMessage = exception.message ?: "No message",
            stackTrace = stackTrace.toString(),
            deviceInfo = deviceInfo
        )
    }

    /**
     * 获取设备基本信息
     */
    private fun getDeviceInfo(): Map<String, String> {
        return try {
            val packageManager = appContext.packageManager
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            
            mapOf(
                "App Version" to "${packageInfo.versionName} (${packageInfo.versionCode})",
                "Android Version" to android.os.Build.VERSION.RELEASE,
                "SDK Version" to android.os.Build.VERSION.SDK_INT.toString(),
                "Device" to android.os.Build.MODEL,
                "Manufacturer" to android.os.Build.MANUFACTURER,
                "Brand" to android.os.Build.BRAND
            )
        } catch (e: Exception) {
            mapOf("Error" to "Failed to get device info: ${e.message}")
        }
    }

    /**
     * 打印到Logcat
     */
    private fun printToLogcat(crashInfo: CrashInfo) {
        val log = buildString {
            appendLine("=== 程序崩溃信息 ===")
            appendLine("时间: ${crashInfo.timestamp}")
            appendLine("线程: ${crashInfo.threadName}")
            appendLine("异常类型: ${crashInfo.exceptionType}")
            appendLine("异常信息: ${crashInfo.exceptionMessage}")
            appendLine("\n设备信息:")
            crashInfo.deviceInfo.forEach { (key, value) ->
                appendLine("  $key: $value")
            }
            appendLine("\n堆栈轨迹:")
            appendLine(crashInfo.stackTrace)
            appendLine("=== 崩溃信息结束 ===")
        }
        
        // 输出到Logcat
        KLog.d(TAG,log)
    }

    /**
     * 通知崩溃监听器
     */
    private fun notifyCrashListener(thread: Thread, exception: Throwable, crashInfo: CrashInfo) {
        crashListener?.onCrash(thread, exception, crashInfo)
    }

    /**
     * 崩溃信息数据类
     */
    data class CrashInfo(
        val timestamp: String,
        val threadName: String,
        val exceptionType: String,
        val exceptionMessage: String,
        val stackTrace: String,
        val deviceInfo: Map<String, String>
    )

    /**
     * 崩溃监听接口
     */
    fun interface CrashListener {
        fun onCrash(thread: Thread, exception: Throwable, crashInfo: CrashInfo)
    }
}