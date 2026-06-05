package com.mic.guide.module.music.data.remote

/** jsonplaceholder /albums 的响应项（裸 DTO，无后端信封）。 */
data class AlbumDto(
    val id: Int,
    val userId: Int,
    val title: String,
)