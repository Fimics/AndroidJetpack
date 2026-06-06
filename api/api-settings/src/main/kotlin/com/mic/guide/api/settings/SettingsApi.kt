package com.mic.guide.api.settings

/**
 * 设置能力接口（§6）：对外暴露全局开关（深色模式、语言），供任意模块读写而不依赖 `module-settings`。
 *
 * 实现在 `module-settings`：深色模式经 `support-storage`（DataStore）持久化；
 * 语言用 AppCompat 的「按应用语言」（`setApplicationLocales`）实现并自动持久化。
 */
interface SettingsApi {

    /** 是否开启深色模式。 */
    fun isDarkMode(): Boolean

    /** 设置深色模式开关。 */
    fun setDarkMode(enabled: Boolean)

    /** 当前语言 BCP-47 标签（如 `zh`/`en`）；跟随系统时为空串。 */
    fun getLanguage(): String

    /** 设置语言：传 `zh`/`en` 切换，传空串恢复「跟随系统」。会自动重建界面。 */
    fun setLanguage(tag: String)
}