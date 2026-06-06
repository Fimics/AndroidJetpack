package com.mic.guide.lib.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit 规则：把 `Dispatchers.Main` 替换为 [TestDispatcher]，让 ViewModel 里 `viewModelScope`
 * 等基于 Main 的协程在单测中可控/同步执行；测试结束自动还原。
 *
 * 用法：`@get:Rule val mainRule = MainDispatcherRule()`。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
