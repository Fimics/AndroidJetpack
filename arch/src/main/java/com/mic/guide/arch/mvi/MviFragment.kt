package com.mic.guide.arch.mvi

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseFragment
import com.mic.guide.arch.base.collectIn

/**
 * MVI 架构 Fragment 基类：在视图 STARTED 生命周期内收集 State 与 Effect，
 * 分别交给 [renderState] 与 [handleEffect]，与 [MviActivity] 对称。
 *
 * 子类用委托提供 ViewModel：`override val viewModel: XxxViewModel by viewModels()`。
 *
 * @param VB 页面 ViewBinding
 * @param I  意图类型
 * @param S  状态类型
 * @param E  副作用类型
 * @param VM ViewModel 类型
 */
abstract class MviFragment<VB : ViewBinding, I : MviIntent, S : MviState, E : MviEffect,
        VM : MviViewModel<I, S, E>> : BaseFragment<VB>() {

    protected abstract val viewModel: VM

    override fun observe() {
        viewModel.state.collectIn(viewLifecycleOwner) { renderState(it) }
        viewModel.effect.collectIn(viewLifecycleOwner) { handleEffect(it) }
    }

    /** 渲染最新 UI 状态（幂等）。 */
    protected abstract fun renderState(state: S)

    /** 处理一次性副作用（Toast / 导航等）。 */
    protected abstract fun handleEffect(effect: E)
}
