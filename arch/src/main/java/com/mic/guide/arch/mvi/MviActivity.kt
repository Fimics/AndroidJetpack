package com.mic.guide.arch.mvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * MVI 架构 Activity 基类：在 STARTED 生命周期内收集 State 与 Effect，
 * 分别交给 [renderState] 与 [handleEffect]。
 *
 * 子类用委托提供 ViewModel：`override val viewModel: XxxViewModel by viewModels()`。
 *
 * @param VB 页面 ViewBinding
 * @param I  意图类型
 * @param S  状态类型
 * @param E  副作用类型
 * @param VM ViewModel 类型
 */
abstract class MviActivity<VB : ViewBinding, I : MviIntent, S : MviState, E : MviEffect,
        VM : MviViewModel<I, S, E>> : BaseActivity<VB>() {

    protected abstract val viewModel: VM

    override fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collectLatest { renderState(it) } }
                launch { viewModel.effect.collectLatest { handleEffect(it) } }
            }
        }
    }

    /** 渲染最新 UI 状态（幂等）。 */
    protected abstract fun renderState(state: S)

    /** 处理一次性副作用（Toast / 导航等）。 */
    protected abstract fun handleEffect(effect: E)
}
