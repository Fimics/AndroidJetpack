package com.mic.guide.lib.log

import android.util.Log

/**
 * 全工程统一日志门面：薄封装 [Log]，集中控制总开关与默认 TAG。
 *
 * 业务/支撑模块统一调 `Logger.d(...)`，便于后续替换实现（如接 xlog、写文件、上报）而不改调用点。
 * 正式包把 [enabled] 置 false 即全局静音。
 */
object Logger {

    /** 全局开关：Release 可置 false 关闭所有日志。 */
    @JvmStatic
    var enabled: Boolean = true

    private const val DEFAULT_TAG = "AiGuide"

    fun d(msg: String, tag: String = DEFAULT_TAG) {
        if (enabled) Log.d(tag, msg)
    }

    fun i(msg: String, tag: String = DEFAULT_TAG) {
        if (enabled) Log.i(tag, msg)
    }

    fun w(msg: String, tag: String = DEFAULT_TAG) {
        if (enabled) Log.w(tag, msg)
    }

    fun e(msg: String, throwable: Throwable? = null, tag: String = DEFAULT_TAG) {
        if (enabled) Log.e(tag, msg, throwable)
    }
}