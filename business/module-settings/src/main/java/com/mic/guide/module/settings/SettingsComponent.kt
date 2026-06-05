package com.mic.guide.module.settings

import android.app.Application
import android.util.Log
import com.mic.guide.api.settings.SettingsApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.ComponentApplication

/**
 * module-settings 的组件入口（§15.5）。通过 SPI 注册，由壳工程 ServiceLoader 自动发现并初始化。
 *
 * 在此注册 [SettingsApi] 实现（经 `support-storage` 持久化深色模式，§6）；
 * priority 较高以便启动期尽早回填并应用夜间模式。
 */
class SettingsComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Log.d("Component", "module-settings onCreate")
        ApiRegistry.register(SettingsApi::class.java, SettingsApiImpl(app))
    }

    override fun priority(): Int = 10
}
