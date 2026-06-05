package com.mic.guide.module.video.data.remote

/** jsonplaceholder /photos 的响应项（裸 DTO，无后端信封）。字段名与 JSON key 一致，无需 @SerializedName。 */
data class PhotoDto(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
)