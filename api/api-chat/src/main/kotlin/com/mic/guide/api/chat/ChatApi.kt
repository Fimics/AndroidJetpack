package com.mic.guide.api.chat

/**
 * 聊天能力接口（§6）：供其他业务模块调用，而**不依赖 `module-chat` 实现**。
 *
 * 实现类在 `module-chat` 内提供并注册到能力容器（接 Hilt 后用 `@Binds`；当前可用 SPI/注册表），
 * 调用方只依赖本接口。打开会话页本质是导航，内部可委托 `support-router` 的 deepLink。
 */
interface ChatApi {

    /** 打开指定会话的聊天详情页。 */
    fun openConversation(conversationId: String)

    /** 查询某会话未读数（非 UI 能力，适合走 api 而非路由）。 */
    fun unreadCount(conversationId: String): Int
}