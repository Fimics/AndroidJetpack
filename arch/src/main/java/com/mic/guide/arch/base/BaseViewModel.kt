package com.mic.guide.arch.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 所有 ViewModel 的基类，封装统一的 Loading 状态、异常处理与协程启动。
 */
abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> = _error

    /**
     * 统一的协程启动入口：自动管理 Loading 显隐与异常分发。
     *
     * @param showLoading 是否在执行期间发布 Loading 状态
     * @param block 业务逻辑（挂起函数）
     */
    protected fun launchWithLoading(
        showLoading: Boolean = true,
        block: suspend () -> Unit,
    ): Job {
        val handler = CoroutineExceptionHandler { _, throwable ->
            if (showLoading) _loading.value = false
            _error.value = throwable
        }
        if (showLoading) _loading.value = true
        return viewModelScope.launch(handler) {
            try {
                block()
            } finally {
                if (showLoading) _loading.value = false
            }
        }
    }
}
