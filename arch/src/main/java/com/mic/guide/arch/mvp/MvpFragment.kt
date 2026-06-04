package com.mic.guide.arch.mvp

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseFragment

/**
 * MVP 架构 Fragment 基类：自身实现 View 契约 [V]，持有并自动绑定 [P]。
 *
 * @param VB 页面 ViewBinding
 * @param V  View 契约（当前 Fragment 需实现它）
 * @param P  Presenter 类型
 */
abstract class MvpFragment<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : BaseFragment<VB>() {

    protected lateinit var presenter: P
        private set

    abstract fun createPresenter(): P

    @Suppress("UNCHECKED_CAST")
    override fun beforeInit() {
        presenter = createPresenter()
        presenter.attach(this as V)
        // 绑定到视图生命周期，onDestroyView 时自动 detach
        viewLifecycleOwner.lifecycle.addObserver(presenter)
    }
}
