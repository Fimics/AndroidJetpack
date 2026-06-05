package com.mic.guide.module.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.chat.databinding.FragmentChatDetailBinding

/**
 * 聊天详情页（MVVM 端到端示范）：作为隐式 deepLink 的目标（`aiguide://chat/detail/{conversationId}`）。
 *
 * 其他模块（如 module-home）只靠 URI 字符串跳到这里，**零依赖本 Fragment 类**，
 * 等价于 ARouter 的 path 跳转（见 docs/02-navigation-vs-arouter.md §3.1 做法 B）。
 *
 * 数据流（§10）：`arguments.conversationId` → `ChatViewModel.load()` →
 * `ChatRepository.safeCall { ChatApiService.getMessages() }` → `messages: StateFlow` → 列表渲染。
 */
class ChatDetailFragment : MvvmFragment<FragmentChatDetailBinding, ChatViewModel>() {

    override val viewModel: ChatViewModel by viewModels()

    private val adapter = ChatAdapter()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentChatDetailBinding.inflate(inflater, container, false)

    override fun initView() {
        val conversationId = arguments?.getString("conversationId").orEmpty()
        binding.tvTitle.text = "聊天会话（module-chat）"
        binding.tvId.text = "conversationId = $conversationId\n（由 home 经 deepLink 跨模块传入）"

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter

        // 会话 id 来自跨模块传参，构造期未知，故在此触发首次加载
        viewModel.load(conversationId)
    }

    override fun observe() {
        viewModel.messages.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }
}