package com.mic.guide.support.router

/**
 * 全局路由 URI 常量与构造器（单一事实源）。
 *
 * 这里的 URI 必须与各业务模块 `nav_graph` 里 `<deepLink>` 声明一致，
 * 例如 module-chat 的 `aiguide://chat/detail/{conversationId}`。
 */
object Routes {

    const val SCHEME = "aiguide"

    /** 聊天详情：对应 module-chat 的隐式 deepLink。 */
    fun chatDetail(conversationId: String): String =
        "$SCHEME://chat/detail/$conversationId"
}
