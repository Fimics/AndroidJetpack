package com.mic.guide.arch.mvvm

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseActivity
import com.mic.guide.arch.base.BaseViewModel

/**
 * MVVM 架构 Activity 基类：持有 [BaseViewModel] 并自动观察其 Loading / 异常。
 *
 * 子类用委托提供 ViewModel：
 * ```
 * override val viewModel: HomeViewModel by viewModels()
 * ```
 *
 * @param VB 页面 ViewBinding
 * @param VM ViewModel 类型（需继承 [BaseViewModel]）
 */
abstract class MvvmActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>() {

    /** 子类提供，推荐 `by viewModels()`。 */
    protected abstract val viewModel: VM

    override fun beforeInit() {
        viewModel.loading.observe(this) { onLoading(it) }
        viewModel.error.observe(this) { onError(it) }
    }

    /** 默认空实现，子类按需展示 Loading。 */
    protected open fun onLoading(loading: Boolean) {}

    /** 默认 Toast 错误信息，子类可覆盖。 */
    protected open fun onError(e: Throwable) {
        toast(e.message ?: "未知错误")
    }
}
