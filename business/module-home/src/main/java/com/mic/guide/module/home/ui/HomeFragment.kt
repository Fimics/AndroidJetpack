package com.mic.guide.module.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.home.R
import com.mic.guide.module.home.databinding.FragmentHomeBinding
import com.mic.guide.support.router.NavigatorProvider

/**
 * 首页 Fragment（MVVM 端到端示范）：
 * - 逻辑写在 `initView()/observe()` 钩子，不覆写 `onViewCreated`（基类已绑定 ViewBinding + loading/error）；
 * - `feed` 是 StateFlow，用 `collectIn(viewLifecycleOwner)` 在 STARTED 收集；
 * - loading 由基类自动收集后回调到 `onLoading`；error 由基类默认 toast。
 */
class HomeFragment : MvvmFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()

    private val adapter = HomeAdapter { item ->
        // 模块内导航：不引 Safe Args 插件，用 bundle 传参（详情页从 arguments 读取）
        findNavController().navigate(
            R.id.action_home_to_detail,
            bundleOf("title" to item.title, "subtitle" to item.subtitle),
        )
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
        binding.btnRefresh.setOnClickListener { viewModel.refresh() }

        // 跨模块跳转：经 support-router 的 AppNavigator 门面（§5.7 方式1），
        // 内部走 deepLink + 失败降级；module-home 仍零依赖 module-chat。
        binding.btnChat.setOnClickListener {
            NavigatorProvider.navigator?.toChatDetail("from-home-42")
        }
    }

    override fun observe() {
        viewModel.feed.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }
}
