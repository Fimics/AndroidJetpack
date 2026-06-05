package com.mic.guide.module.chat

import com.mic.guide.api.chat.ChatApi
import com.mic.guide.support.router.NavigatorProvider

/**
 * [ChatApi] 在本模块的实现（§6）。由 [ChatComponent] 注册进 `ApiRegistry`，
 * 供其他模块（如 module-home）经接口调用，调用方零依赖本类。
 *
 * 打开会话本质是导航：委托 `support-router` 的全局门面走 deepLink（与直接跳转同源）。
 */
class ChatApiImpl : ChatApi {

    override fun openConversation(conversationId: String) {
        NavigatorProvider.navigator?.toChatDetail(conversationId)
    }

    override fun unreadCount(conversationId: String): Int {
        // 示范桩：真实实现应查本模块 Repository / 本地库
        return 0
    }
}