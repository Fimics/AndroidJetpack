package com.mic.guide.module.home.domain.model

/** 首页 feed 领域模型（纯 Kotlin，不含 Android/网络细节）。 */
data class FeedItem(
    val id: Int,
    val title: String,
    val subtitle: String,
)
