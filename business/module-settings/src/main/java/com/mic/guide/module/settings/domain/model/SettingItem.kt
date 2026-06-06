package com.mic.guide.module.settings.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * 设置项领域模型。标题/摘要用 `@StringRes` 资源 id（支持多语言，由 Adapter 用 context 解析）。
 *
 * @param key 稳定标识（深色模式 "dark_mode"、语言 "language"），用于区分需特殊处理的项
 * @param isSwitch true 时该行展示开关，否则展示「›」可点进入
 */
data class SettingItem(
    val id: Int,
    val key: String,
    @StringRes val titleRes: Int,
    @StringRes val summaryRes: Int,
    @DrawableRes val iconRes: Int,
    val isSwitch: Boolean = false,
)