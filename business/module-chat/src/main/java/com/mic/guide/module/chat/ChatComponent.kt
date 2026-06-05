package com.mic.guide.module.chat

import android.app.Application
import android.util.Log
import com.mic.guide.api.chat.ChatApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.ComponentApplication

/**
 * module-chat 的组件入口（§15.5）。通过 SPI 注册，由壳工程 ServiceLoader 自动发现并初始化。
 *
 * 在此把本模块的 [ChatApi] 实现注册进 [ApiRegistry]：拔掉本模块 → 该 Component 不再被发现 →
 * `ChatApi` 不再注册 → 消费方 `ApiRegistry.get(ChatApi::class.java)` 拿到 null 并降级（§6）。
 */
class ChatComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Log.d("Component", "module-chat onCreate")
        ApiRegistry.register(ChatApi::class.java, ChatApiImpl())
    }

    override fun priority(): Int = 50
}
