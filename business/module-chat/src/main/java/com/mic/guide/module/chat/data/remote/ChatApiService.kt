package com.mic.guide.module.chat.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** 聊天 HTTP 接口（本模块的 data/remote；BaseUrl 由 NetworkClient 注入）。 */
interface ChatApiService {

    /**
     * 拉取某会话的消息列表。
     *
     * 示范用公共测试 API：以 `comments` 作为消息源（jsonplaceholder 无真实会话接口），
     * 接自家后端时把路径换成 `chat/{conversationId}/messages` 即可，调用方无需改动。
     */
    @GET("comments")
    suspend fun getMessages(
        @Query("_limit") limit: Int = 15,
    ): List<CommentDto>
}