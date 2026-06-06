package com.mic.guide.support.ai.model.dto

import com.google.gson.annotations.SerializedName
import com.mic.guide.support.ai.model.AiMessage

/** 请求体里的一条消息（OpenAI 兼容线格式）。 */
data class ChatMessageDto(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
) {
    companion object {
        fun from(m: AiMessage) = ChatMessageDto(m.role.wire, m.content)
    }
}

/** `/v1/chat/completions` 请求体。 */
data class ChatCompletionRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ChatMessageDto>,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("stream") val stream: Boolean,
)

/** 非流式响应：`choices[].message.content`。 */
data class ChatCompletionResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("choices") val choices: List<Choice>?,
    @SerializedName("usage") val usage: Usage?,
) {
    data class Choice(
        @SerializedName("index") val index: Int,
        @SerializedName("message") val message: ChatMessageDto?,
        @SerializedName("finish_reason") val finishReason: String?,
    )

    data class Usage(
        @SerializedName("prompt_tokens") val promptTokens: Int,
        @SerializedName("completion_tokens") val completionTokens: Int,
        @SerializedName("total_tokens") val totalTokens: Int,
    )

    fun firstContent(): String = choices?.firstOrNull()?.message?.content.orEmpty()
}

/** 流式响应的单个 SSE chunk：`choices[].delta.content` 是增量 token。 */
data class ChatCompletionChunk(
    @SerializedName("choices") val choices: List<Choice>?,
) {
    data class Choice(
        @SerializedName("delta") val delta: Delta?,
        @SerializedName("finish_reason") val finishReason: String?,
    )

    data class Delta(
        @SerializedName("content") val content: String?,
    )

    fun deltaContent(): String = choices?.firstOrNull()?.delta?.content.orEmpty()
}