package com.mic.guide.module.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.mic.guide.api.settings.SettingsApi
import com.mic.guide.support.storage.PreferenceStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * [SettingsApi] 在本模块的实现（§6）：经 `support-storage` 的 [PreferenceStorage]（DataStore）持久化深色模式。
 *
 * **同步接口 × 异步存储的桥接**：`SettingsApi.isDarkMode()` 是同步返回，而 DataStore 是 Flow/suspend，
 * 因此用「内存缓存 [darkMode] + 启动期收集持久化值」桥接：
 * - 进程启动时（注册即构造）收集 [PreferenceStorage.boolFlow]，把磁盘值同步进缓存并应用到全局夜间模式；
 * - [isDarkMode] 读缓存（O(1)、不阻塞）；
 * - [setDarkMode] 立即改缓存 + 应用夜间模式 + 异步落盘。
 *
 * 由 [SettingsComponent] 注册进 `ApiRegistry`，任意模块可经 `SettingsApi` 读写而零依赖本模块。
 */
class SettingsApiImpl(app: Application) : SettingsApi {

    private val storage = PreferenceStorage(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var darkMode: Boolean = false

    init {
        // 启动期把持久化值同步进内存缓存，并应用到全局夜间模式（DataStore 首帧后回填）
        storage.boolFlow(KEY_DARK_MODE, default = false)
            .onEach { enabled ->
                darkMode = enabled
                applyNightMode(enabled)
            }
            .launchIn(scope)
    }

    override fun isDarkMode(): Boolean = darkMode

    override fun setDarkMode(enabled: Boolean) {
        darkMode = enabled
        applyNightMode(enabled)
        scope.launch { storage.putBool(KEY_DARK_MODE, enabled) }
    }

    private fun applyNightMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
        )
    }

    private companion object {
        const val KEY_DARK_MODE = "dark_mode"
    }
}