package com.mic.guide.module.home.data.remote

/** jsonplaceholder /posts 的响应项（裸 DTO，无后端信封）。多余字段 userId 由 Gson 忽略。 */
data class PostDto(
    val id: Int,
    val title: String,
    val body: String,
)
