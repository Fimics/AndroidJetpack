package com.mic.guide.arch.mvp

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseActivity

/**
 * MVP 架构 Activity 基类：自身实现 View 契约 [V]，持有并自动绑定 [P]。
 *
 * @param VB 页面 ViewBinding
 * @param V  View 契约（当前 Activity 需实现它）
 * @param P  Presenter 类型
 */
abstract class MvpActivity<VB : ViewBinding, V : MvpView, P : MvpPresenter<V>> : BaseActivity<VB>() {

    protected lateinit var presenter: P
        private set

    /** 子类创建 Presenter 实例。 */
    abstract fun createPresenter(): P

    @Suppress("UNCHECKED_CAST")
    override fun beforeInit() {
        presenter = createPresenter()
        presenter.attach(this as V)
        // 随生命周期自动 detach
        lifecycle.addObserver(presenter)
    }
}
