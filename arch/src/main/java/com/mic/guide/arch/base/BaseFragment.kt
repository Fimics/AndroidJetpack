package com.mic.guide.arch.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * 所有 Fragment 的基类，统一 ViewBinding 生命周期管理与通用工具。
 *
 * 视图存在期间（onCreateView ~ onDestroyView）才可访问 [binding]，避免内存泄漏。
 *
 * @param VB 当前页面的 ViewBinding 类型
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    /** 仅在视图存在期间可用。 */
    protected val binding: VB
        get() = requireNotNull(_binding) { "binding 仅在视图存在期间可用" }

    /** 子类创建具体的 ViewBinding，例如 `FragmentHomeBinding.inflate(inflater, container, false)`。 */
    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        beforeInit()
        initView()
        initData()
        observe()
    }

    /** 架构层钩子：在 [initView] 之前执行（如观察 ViewModel 基础状态）。 */
    protected open fun beforeInit() {}

    protected open fun initView() {}

    protected open fun initData() {}

    protected open fun observe() {}

    protected fun toast(msg: CharSequence) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
