package com.mic.guide.module.chat

import android.app.Application
import android.util.Log
import com.mic.guide.arch.base.ComponentApplication

/**
 * module-chat 的组件入口（§15.5）。通过 SPI 注册，由壳工程 ServiceLoader 自动发现并初始化。
 */
class ChatComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Log.d("Component", "module-chat onCreate")
        // TODO: 注册本模块的 api-* 实现 / 路由 / 预热
    }

    override fun priority(): Int = 50
}
