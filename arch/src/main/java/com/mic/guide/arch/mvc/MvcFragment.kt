package com.mic.guide.arch.mvc

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseFragment

/**
 * MVC 架构 Fragment 基类：View 层持有一个 [MvcController] 处理业务，
 * 与 [MvcActivity] 对称，让 Fragment 也能走 MVC 模式。
 *
 * @param VB 页面 ViewBinding
 * @param C  控制器类型
 */
abstract class MvcFragment<VB : ViewBinding, C : MvcController> : BaseFragment<VB>() {

    protected lateinit var controller: C
        private set

    /** 子类创建控制器实例。 */
    abstract fun createController(): C

    override fun beforeInit() {
        controller = createController()
        controller.onCreate()
    }

    override fun onDestroyView() {
        if (::controller.isInitialized) controller.onDestroy()
        super.onDestroyView()
    }
}
