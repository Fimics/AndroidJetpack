package com.mic.guide.arch.api

import java.util.concurrent.ConcurrentHashMap

/**
 * 跨模块能力接口注册表（配合 [com.mic.guide.arch.base.ComponentApplication] / SPI，见 §6 / §15.5）。
 *
 * 工作方式（零编译耦合）：
 * 1. 能力接口（如 `ChatApi`）定义在纯 Kotlin 的 `api-*` 模块；
 * 2. 业务模块在自己的 `XComponent.onCreate()` 里 [register] 实现——这一步由壳工程的
 *    `ServiceLoader` 自动触发（删模块 → 不再注册 → app 零改动）；
 * 3. 消费方**只依赖 `api-*` 接口**，用 [get] / [require] 按接口类型取实现。
 *
 * 因此「拔掉某业务模块」= 它的 `Component` 不再被 ServiceLoader 发现 → 对应能力 [get] 返回 null，
 * 调用方据此降级，不会编译失败、也不会崩溃。线程安全（[ConcurrentHashMap]）。
 */
object ApiRegistry {

    @PublishedApi
    internal val services = ConcurrentHashMap<Class<*>, Any>()

    /** 注册某能力接口的实现（通常在 `XComponent.onCreate()` 内调用）。 */
    fun <T : Any> register(clazz: Class<T>, impl: T) {
        services[clazz] = impl
    }

    /** 按接口类型取实现；未注册（模块未集成/被拔掉）时返回 null，供调用方降级。 */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: Class<T>): T? = services[clazz] as? T

    /** 同 [get]，但未注册时抛异常——用于「该能力必须存在」的场景。 */
    fun <T : Any> require(clazz: Class<T>): T = get(clazz)
        ?: error("ApiRegistry: 未找到 ${clazz.name} 的实现，对应业务模块是否已集成并注册？")

    /** 移除注册（一般仅测试或模块热卸载时用）。 */
    fun <T : Any> unregister(clazz: Class<T>) {
        services.remove(clazz)
    }
}

/** reified 便捷取用：`ApiRegistry.get<ChatApi>()`。 */
inline fun <reified T : Any> ApiRegistry.get(): T? = get(T::class.java)

/** reified 便捷取用：`ApiRegistry.require<ChatApi>()`。 */
inline fun <reified T : Any> ApiRegistry.require(): T = require(T::class.java)