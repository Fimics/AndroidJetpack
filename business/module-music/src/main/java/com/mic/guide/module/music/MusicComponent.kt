package com.mic.guide.module.music

import android.app.Application
import android.util.Log
import com.mic.guide.api.music.MusicApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.ComponentApplication

/**
 * module-music 的组件入口（§15.5）。通过 SPI 注册，由壳工程 ServiceLoader 自动发现并初始化。
 *
 * 在此把本模块的 [MusicApi] 实现注册进 [ApiRegistry]，供任意模块按接口取用（§6）。
 */
class MusicComponent : ComponentApplication {

    override fun onCreate(app: Application) {
        Log.d("Component", "module-music onCreate")
        ApiRegistry.register(MusicApi::class.java, MusicApiImpl())
    }

    override fun priority(): Int = 40
}
