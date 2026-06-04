package com.mic.guide.arch.base

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * 所有 Activity 的基类，统一 ViewBinding、生命周期回调与通用工具。
 *
 * 初始化顺序：[createBinding] → [beforeInit] → [initView] → [initData] → [observe]。
 *
 * @param VB 当前页面的 ViewBinding 类型
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null

    /** 仅在 onCreate 之后、onDestroy 之前可用。 */
    protected val binding: VB
        get() = requireNotNull(_binding) { "binding 仅在 onCreate 之后可用" }

    /** 子类创建具体的 ViewBinding，例如 `ActivityHomeBinding.inflate(inflater)`。 */
    abstract fun createBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = createBinding(layoutInflater)
        setContentView(binding.root)
        beforeInit()
        initView()
        initData()
        observe()
    }

    /** 架构层钩子：在 [initView] 之前执行（如创建 ViewModel / Presenter / Controller）。 */
    protected open fun beforeInit() {}

    /** 初始化视图与监听。 */
    protected open fun initView() {}

    /** 加载初始数据。 */
    protected open fun initData() {}

    /** 订阅数据变化（LiveData / Flow）。 */
    protected open fun observe() {}

    protected fun toast(msg: CharSequence) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
