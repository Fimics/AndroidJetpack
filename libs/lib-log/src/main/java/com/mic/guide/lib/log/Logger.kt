package com.mic.guide.lib.log

import android.util.Log

/**
 * 全工程统一日志门面。**已与迁移自 libcore 的 [KLog] 合并**：
 * - [useFileLog] = true（默认）时委托 [KLog]（xlog 控制台 + 按天落盘 + 调用栈定位）；
 * - = false 时退化为系统 [Log]（轻量，无文件）。
 *
 * 业务/支撑模块统一调 `Logger.d(...)`；[enabled] 置 false 全局静音。
 * 文件日志使用前建议在 Application 调一次 `KLog.initSimple(this)`（不调也会用默认配置惰性初始化）。
 */
object Logger {

    /** 全局开关：Release 可置 false 关闭所有日志。 */
    @JvmStatic
    var enabled: Boolean = true

    /** 是否走 KLog（文件日志）。关掉则仅用系统 Log。 */
    @JvmStatic
    var useFileLog: Boolean = true

    private const val DEFAULT_TAG = "AiGuide"

    fun d(msg: String, tag: String = DEFAULT_TAG) {
        if (!enabled) return
        if (useFileLog) KLog.d(tag, msg) else Log.d(tag, msg)
    }

    fun i(msg: String, tag: String = DEFAULT_TAG) {
        if (!enabled) return
        if (useFileLog) KLog.i(tag, msg) else Log.i(tag, msg)
    }

    fun w(msg: String, tag: String = DEFAULT_TAG) {
        if (!enabled) return
        if (useFileLog) KLog.w(tag, msg) else Log.w(tag, msg)
    }

    fun e(msg: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (!enabled) return
        if (useFileLog) KLog.e(tag, msg, throwable) else Log.e(tag, msg, throwable)
    }
}
