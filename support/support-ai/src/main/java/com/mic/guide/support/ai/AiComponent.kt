package com.mic.guide.support.ai

import android.app.Application
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.ComponentApplication
import com.mic.guide.lib.log.Logger

/**
 * support-ai 组件入口：「带组件的支撑模块」自注册（§6.6 同款，参考 support-media）。
 *
 * 经 SPI 被壳工程 `ServiceLoader` 发现，把单例 [AiChatClient] 注册进 [ApiRegistry]，
 * 任意模块经 `ApiRegistry.get(AiChatClient::class.java)` 复用 AI 对话能力；未集成本模块即 null 降级。
 */
class AiComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Logger.d("support-ai onCreate", tag = "Component")
        ApiRegistry.register(AiChatClient::class.java, AiChatClient())
    }

    override fun priority(): Int = 80
}
