package com.mic.guide.module.chat.data.remote

/** jsonplaceholder /comments 的响应项（裸 DTO，无后端信封）。多余字段 postId 由 Gson 忽略。 */
data class CommentDto(
    val id: Int,
    val name: String,
    val body: String,
)
