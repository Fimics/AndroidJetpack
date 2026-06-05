package com.mic.guide.module.music.domain.model

/** 歌曲领域模型（纯 Kotlin，不含 Android/网络细节）。 */
data class Track(
    val id: Int,
    val title: String,
    val artist: String,
)