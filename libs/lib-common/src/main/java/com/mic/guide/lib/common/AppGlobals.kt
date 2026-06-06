package com.mic.guide.lib.common

import android.app.Application
import android.content.pm.ApplicationInfo

/**
 * 全局 Application 持有者（迁移自 libcore `AppGlobals`，转 Kotlin）。
 *
 * 通过反射读取当前进程的 `Application`，让任意工具类无需显式传 Context 即可拿到全局上下文。
 * 业务侧也可在启动时主动 [attach] 注入，避免反射。
 */
object AppGlobals {

    @Volatile
    private var application: Application? = null

    private var debuggable: Boolean? = null

    /** 主动注入（推荐在 Application.onCreate 调用）。 */
    fun attach(app: Application) {
        application = app
    }

    @JvmStatic
    fun getApplication(): Application {
        application?.let { return it }
        return try {
            @Suppress("PrivateApi", "DiscouragedPrivateApi")
            val app = Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as Application
            application = app
            app
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException("AppGlobals: 无法获取 Application，请在启动时调用 AppGlobals.attach(app)")
        }
    }

    @JvmStatic
    fun isDebuggable(): Boolean {
        debuggable?.let { return it }
        val app = application ?: runCatching { getApplication() }.getOrNull()
        val result = app != null &&
            (app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        debuggable = result
        return result
    }
}
