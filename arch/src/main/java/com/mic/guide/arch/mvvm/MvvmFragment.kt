package com.mic.guide.arch.mvvm

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseFragment
import com.mic.guide.arch.base.BaseViewModel

/**
 * MVVM 架构 Fragment 基类：持有 [BaseViewModel] 并自动观察其 Loading / 异常。
 *
 * 子类用委托提供 ViewModel（需 `androidx.fragment:fragment-ktx`）：
 * ```
 * override val viewModel: HomeViewModel by viewModels()
 * ```
 *
 * @param VB 页面 ViewBinding
 * @param VM ViewModel 类型（需继承 [BaseViewModel]）
 */
abstract class MvvmFragment<VB : ViewBinding, VM : BaseViewModel> : BaseFragment<VB>() {

    protected abstract val viewModel: VM

    override fun beforeInit() {
        viewModel.loading.observe(viewLifecycleOwner) { onLoading(it) }
        viewModel.error.observe(viewLifecycleOwner) { onError(it) }
    }

    protected open fun onLoading(loading: Boolean) {}

    protected open fun onError(e: Throwable) {
        toast(e.message ?: "未知错误")
    }
}
