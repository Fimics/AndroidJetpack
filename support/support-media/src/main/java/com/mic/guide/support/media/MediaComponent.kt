package com.mic.guide.support.media

import android.app.Application
import android.util.Log
import com.mic.guide.api.player.PlayerApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.ComponentApplication

/**
 * support-media 的组件入口：**支撑模块也可实现 [ComponentApplication] 自注册能力**（§6 / §15.5）。
 *
 * 经 SPI 被壳工程 `ServiceLoader` 自动发现，注册 [PlayerApi] 的 Media3 实现进 [ApiRegistry]，
 * 供 music/video 等任意模块按接口复用——它们零依赖 `support-media`，删掉本模块即 `get()` 返回 null 降级。
 * priority 偏高，确保业务首次播放前能力已就绪。
 */
class MediaComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Log.d("Component", "support-media onCreate")
        ApiRegistry.register(PlayerApi::class.java, Media3PlayerApi(app))
    }

    override fun priority(): Int = 90
}