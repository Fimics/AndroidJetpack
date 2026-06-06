package com.mic.guide.lib.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Flow 测试辅助：在 [runTest] 作用域里收集 [count] 个发射值断言。
 *
 * 用法：`val items = flow.collectValues(2)`，再 `assertEquals(...)`。
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Flow<T>.collectValues(count: Int): List<T> = take(count).toList()

/** 便捷包装：在测试协程里执行 [block]，自动推进虚拟时钟（StandardTestDispatcher 场景）。 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runFlowTest(block: suspend TestScope.() -> Unit) = runTest { block() }
