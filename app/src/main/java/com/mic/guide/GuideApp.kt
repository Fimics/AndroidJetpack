package com.mic.guide

import android.util.Log
import com.mic.guide.arch.base.BaseApplication
import com.mic.guide.arch.base.ComponentApplication
import java.util.ServiceLoader

/**
 * 壳工程 Application：组件化自动装配入口（见 docs/01-arch.md §15.5）。
 *
 * 用 ServiceLoader 发现各业务模块通过 SPI 注册的 [ComponentApplication]，
 * 按 priority 倒序初始化。增删一个业务模块只需改 settings/app 依赖，本类零改动。
 */
class GuideApp : BaseApplication() {

    private val tag = "init"

    override fun onInit() {
        Log.d(tag, "app onInit, loading components...")
        ServiceLoader.load(ComponentApplication::class.java)
            .sortedByDescending { it.priority() }
            .forEach { component ->
                Log.d(tag, "init component: ${component.javaClass.name}")
                component.onCreate(this)
            }
    }
}
