package com.mic.guide.arch.base

import android.app.Application
import android.content.Context

/**
 * Application 基类，提供全局 Context 与统一初始化入口。
 * 壳工程的 Application 继承它并在 [onInit] 中完成路由 / DI / 日志等初始化。
 */
abstract class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        onInit()
    }

    /** 子类在此完成各模块初始化。 */
    protected abstract fun onInit()

    companion object {
        @JvmStatic
        lateinit var instance: BaseApplication
            private set

        /** 全局 ApplicationContext，便于工具类使用。 */
        val appContext: Context get() = instance.applicationContext
    }
}
