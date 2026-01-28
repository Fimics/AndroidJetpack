package com.mic.log.runtime

import android.util.Log

object AutoLog {
    private const val TAG = "AutoLog"

    /**
     * 全局开关：建议 debug=true / release=false
     * 也可以改成从 BuildConfig / remote config 读
     */
    @JvmField var enabled: Boolean = true

    /**
     * 堆栈开关：非常重，建议只在 debug 或采样开启
     */
    @JvmField var enableStack: Boolean = false

    @JvmStatic
    fun enter(owner: String, name: String, desc: String) {
        if (!enabled) return
        if (enableStack) {
            Log.d(TAG, ">> $owner#$name $desc\n${stack()}")
        } else {
            Log.d(TAG, ">> $owner#$name $desc")
        }
    }

    @JvmStatic
    fun exit(owner: String, name: String, costNs: Long) {
        if (!enabled) return
        Log.d(TAG, "<< $owner#$name cost=${costNs / 1_000_000.0}ms")
    }

    @JvmStatic
    fun error(owner: String, name: String, t: Throwable) {
        if (!enabled) return
        Log.e(TAG, "!! $owner#$name", t)
    }

    @JvmStatic
    fun stack(): String = Throwable().stackTraceToString()
}