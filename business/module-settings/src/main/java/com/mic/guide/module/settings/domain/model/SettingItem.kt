package com.mic.guide.module.settings.domain.model

/** 设置项领域模型（纯 Kotlin，不含 Android 细节）。 */
data class SettingItem(
    val id: Int,
    val title: String,
    val summary: String,
)