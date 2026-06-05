package com.mic.guide.lib.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * 协程调度器门面：业务/Repository 注入本接口而非直接用 [Dispatchers]，便于单测替换为测试调度器。
 *
 * 默认实现 [DefaultAppDispatchers] 直连标准调度器；测试时传入基于 `StandardTestDispatcher` 的实现。
 */
interface AppDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

object DefaultAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
}
