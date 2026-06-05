package com.mic.guide.module.chat.domain.model

/**
 * 聊天消息领域模型（纯 Kotlin，不含 Android/网络细节）。
 *
 * @param fromMe true 表示本人发送（右侧气泡），false 为对方消息（左侧气泡）。
 */
data class Message(
    val id: Int,
    val author: String,
    val content: String,
    val fromMe: Boolean,
)