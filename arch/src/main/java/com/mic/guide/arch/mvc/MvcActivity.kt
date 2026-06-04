package com.mic.guide.arch.mvc

import androidx.viewbinding.ViewBinding
import com.mic.guide.arch.base.BaseActivity

/**
 * MVC 架构 Activity 基类：View 层持有一个 [MvcController] 处理业务。
 *
 * @param VB 页面 ViewBinding
 * @param C  控制器类型
 */
abstract class MvcActivity<VB : ViewBinding, C : MvcController> : BaseActivity<VB>() {

    protected lateinit var controller: C
        private set

    /** 子类创建控制器实例。 */
    abstract fun createController(): C

    override fun beforeInit() {
        controller = createController()
        controller.onCreate()
    }

    override fun onDestroy() {
        if (::controller.isInitialized) controller.onDestroy()
        super.onDestroy()
    }
}
