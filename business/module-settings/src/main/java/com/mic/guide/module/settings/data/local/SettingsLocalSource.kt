package com.mic.guide.module.settings.data.local

import com.mic.guide.module.settings.R
import com.mic.guide.module.settings.domain.model.SettingItem

/**
 * 设置项本地数据源（静态表，文案用字符串资源以支持多语言）。
 *
 * - [load]：一级页面（设置首页）的入口项。
 * - [detailItems]：每个一级项对应的二级页面功能项（每个功能即一个 item）。
 */
object SettingsLocalSource {

    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_LANGUAGE = "language"
    const val KEY_PROFILE = "profile"

    /** 一级页面入口项。 */
    fun load(): List<SettingItem> = listOf(
        SettingItem(1, "account", R.string.settings_title_account, R.string.settings_summary_account, R.drawable.ic_account),
        SettingItem(2, "notify", R.string.settings_title_notify, R.string.settings_summary_notify, R.drawable.ic_notification),
        SettingItem(3, KEY_DARK_MODE, R.string.settings_title_dark, R.string.settings_summary_dark, R.drawable.ic_dark_mode, isSwitch = true),
        SettingItem(4, KEY_LANGUAGE, R.string.settings_title_language, R.string.settings_summary_language, R.drawable.ic_language),
        SettingItem(5, "storage", R.string.settings_title_storage, R.string.settings_summary_storage, R.drawable.ic_storage),
        SettingItem(6, "about", R.string.settings_title_about, R.string.settings_summary_about, R.drawable.ic_about),
    )

    /** 二级页面功能项：每个一级项点进去看到的「功能列表」。 */
    fun detailItems(sectionKey: String): List<SettingItem> = when (sectionKey) {
        KEY_PROFILE -> listOf(
            row(101, R.string.pf_avatar, R.drawable.ic_account),
            row(102, R.string.pf_nickname, R.drawable.ic_account),
            row(103, R.string.pf_account, R.drawable.ic_account),
            row(104, R.string.pf_qrcode, R.drawable.ic_account),
        )
        "account" -> listOf(
            row(201, R.string.ac_devices, R.drawable.ic_account),
            row(202, R.string.ac_password, R.drawable.ic_account),
            row(203, R.string.ac_delete, R.drawable.ic_account),
        )
        "notify" -> listOf(
            switch(301, R.string.nt_push, R.drawable.ic_notification),
            switch(302, R.string.nt_sound, R.drawable.ic_notification),
            switch(303, R.string.nt_vibrate, R.drawable.ic_notification),
        )
        "storage" -> listOf(
            row(401, R.string.st_clear, R.drawable.ic_storage),
            row(402, R.string.st_download, R.drawable.ic_storage),
            switch(403, R.string.st_auto, R.drawable.ic_storage),
        )
        "about" -> listOf(
            row(501, R.string.ab_version, R.drawable.ic_about),
            row(502, R.string.ab_license, R.drawable.ic_about),
            row(503, R.string.ab_update, R.drawable.ic_about),
        )
        else -> emptyList()
    }

    /** 普通功能行（无摘要，展示「›」）。 */
    private fun row(id: Int, titleRes: Int, iconRes: Int) =
        SettingItem(id, "leaf_$id", titleRes, summaryRes = 0, iconRes = iconRes)

    /** 开关功能行。 */
    private fun switch(id: Int, titleRes: Int, iconRes: Int) =
        SettingItem(id, "leaf_$id", titleRes, summaryRes = 0, iconRes = iconRes, isSwitch = true)
}