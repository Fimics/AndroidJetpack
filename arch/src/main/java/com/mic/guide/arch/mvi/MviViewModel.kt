package com.mic.guide.arch.mvi

import androidx.lifecycle.viewModelScope
import com.mic.guide.arch.base.BaseViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVI ViewModel 基类，实现单向数据流：
 * `Intent --dispatch--> handleIntent --setState--> State`，副作用经 [sendEffect] 单次下发。
 *
 * @param I 意图类型
 * @param S 状态类型
 * @param E 副作用类型
 * @param initialState 初始 UI 状态
 */
abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect>(
    initialState: S,
) : BaseViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /** 当前最新状态。 */
    protected val currentState: S get() = _state.value

    /** 视图派发用户意图的唯一入口。 */
    fun dispatch(intent: I) = handleIntent(intent)

    /** 子类处理意图，并通过 [setState] / [sendEffect] 产出结果。 */
    protected abstract fun handleIntent(intent: I)

    /** 基于当前状态归约出新状态。 */
    protected fun setState(reducer: S.() -> S) {
        _state.value = currentState.reducer()
    }

    /** 下发一次性副作用。 */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
