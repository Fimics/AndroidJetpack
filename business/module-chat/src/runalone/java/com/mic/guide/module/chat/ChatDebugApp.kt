package com.mic.guide.module.chat

import com.mic.guide.arch.base.BaseApplication
import com.mic.guide.arch.base.ComponentApplication
import java.util.ServiceLoader

/** 组件模式下的独立 Application：单独初始化本模块所需组件。 */
class ChatDebugApp : BaseApplication() {
    override fun onInit() {
        ServiceLoader.load(ComponentApplication::class.java)
            .sortedByDescending { it.priority() }
            .forEach { it.onCreate(this) }
    }
}
