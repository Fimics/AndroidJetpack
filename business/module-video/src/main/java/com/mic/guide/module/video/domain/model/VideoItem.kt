package com.mic.guide.module.video.domain.model

/** 视频领域模型（纯 Kotlin，不含 Android/网络细节）。 */
data class VideoItem(
    val id: Int,
    val title: String,
    val thumbnail: String,
)