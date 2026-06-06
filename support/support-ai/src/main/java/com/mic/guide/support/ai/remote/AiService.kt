package com.mic.guide.support.ai.remote

import com.mic.guide.support.ai.model.dto.ChatCompletionRequest
import com.mic.guide.support.ai.model.dto.ChatCompletionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * 大模型网关 Retrofit 接口（仅非流式路径走这里；流式 SSE 由 [com.mic.guide.support.ai.AiChatClient] 用裸 OkHttp 处理）。
 */
interface AiService {

    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body body: ChatCompletionRequest,
    ): ChatCompletionResponse
}