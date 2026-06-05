package com.mic.guide.api.settings

/**
 * 设置能力接口（§6）：对外暴露全局开关（如深色模式），供任意模块读写而不依赖 `module-settings`。
 *
 * 实现在 `module-settings`，内部读写 `support-storage`（DataStore）。
 */
interface SettingsApi {

    /** 是否开启深色模式。 */
    fun isDarkMode(): Boolean

    /** 设置深色模式开关。 */
    fun setDarkMode(enabled: Boolean)
}