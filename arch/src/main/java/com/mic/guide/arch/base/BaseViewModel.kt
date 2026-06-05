package com.mic.guide.arch.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 所有 ViewModel 的基类，封装统一的 Loading 状态、异常处理与协程启动。
 *
 * 全面基于 Kotlin 协程 + Flow：
 * - [loading] 是 [StateFlow]（持有最新值，新订阅者会立即收到当前状态）；
 * - [error] 是 [SharedFlow]（replay=0 的一次性事件，UI 重建不会重放旧异常）。
 */
abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)

    /** Loading 状态流。 */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)

    /** 异常事件流（一次性，不重放）。 */
    val error: SharedFlow<Throwable> = _error.asSharedFlow()

    /**
     * 统一的协程启动入口：自动管理 Loading 显隐与异常分发。
     *
     * @param showLoading 是否在执行期间发布 Loading 状态
     * @param block 业务逻辑（挂起函数）
     */
    protected fun launchWithLoading(
        showLoading: Boolean = true,
        block: suspend () -> Unit,
    ): Job = viewModelScope.launch {
        if (showLoading) _loading.value = true
        try {
            block()
        } catch (e: CancellationException) {
            throw e // 协程取消需向上传播，不当作业务异常
        } catch (e: Throwable) {
            _error.tryEmit(e)
        } finally {
            if (showLoading) _loading.value = false
        }
    }
}
