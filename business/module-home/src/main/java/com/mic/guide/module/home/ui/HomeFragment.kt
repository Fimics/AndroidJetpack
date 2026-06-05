package com.mic.guide.module.home.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.api.chat.ChatApi
import com.mic.guide.api.music.MusicApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.home.R
import com.mic.guide.module.home.databinding.FragmentHomeBinding

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

        // 跨模块能力调用（§5.7 方式2 / §6）：只依赖 api-chat 接口，经 ApiRegistry 取实现。
        // module-chat 被拔掉时 get() 返回 null → 降级提示，不崩、不编译失败。
        binding.btnChat.setOnClickListener {
            val chat = ApiRegistry.get(ChatApi::class.java)
            if (chat != null) chat.openConversation("from-home-42") else toast("聊天模块未集成")
        }

        // 跨模块「非 UI 能力」（§5.11）：经 MusicApi 播放并读当前曲目，零依赖 module-music。
        binding.btnMusic.setOnClickListener {
            val music = ApiRegistry.get(MusicApi::class.java)
            if (music != null) {
                music.play("1001")
                toast("正在播放：${music.currentSong()?.title}")
            } else {
                toast("音乐模块未集成")
            }
        }
    }

    override fun observe() {
        viewModel.feed.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }
}
