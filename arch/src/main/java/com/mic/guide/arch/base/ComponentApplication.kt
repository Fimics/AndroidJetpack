package com.mic.guide.arch.base

import android.app.Application

/**
 * 组件生命周期契约（组件化 / 模块可插拔，见 docs/01-arch.md §15）。
 *
 * 每个业务模块实现本接口，并在 `src/main/resources/META-INF/services/`
 * 下用 SPI 注册自己的实现类；壳工程（或组件模式下的 DebugApp）在
 * [BaseApplication.onInit] 里用 `ServiceLoader.load(ComponentApplication::class.java)`
 * 自动发现并初始化——**无需 import 任何模块的初始化类**，删模块即零 app 改动。
 */
interface ComponentApplication {

    /** 模块自治初始化：注册 api 实现 / 预热 / 声明路由等。 */
    fun onCreate(app: Application)

    /** 多组件初始化顺序，数值大者先执行。 */
    fun priority(): Int = 0
}
