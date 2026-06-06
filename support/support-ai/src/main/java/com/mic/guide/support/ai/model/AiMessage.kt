package com.mic.guide.support.ai.model

/** 对话角色（OpenAI 兼容）。 */
enum class AiRole(val wire: String) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
}

/**
 * 一条对话消息（领域模型，供 UI / 业务持有）。
 *
 * 与传给网关的 DTO（[com.mic.guide.support.ai.model.dto.ChatMessageDto]）区分：
 * 领域模型用枚举角色、可携带本地态（如是否流式中），DTO 只为序列化。
 */
data class AiMessage(
    val role: AiRole,
    val content: String,
) {
    companion object {
        fun system(content: String) = AiMessage(AiRole.SYSTEM, content)
        fun user(content: String) = AiMessage(AiRole.USER, content)
        fun assistant(content: String) = AiMessage(AiRole.ASSISTANT, content)
    }
}