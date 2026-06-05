package com.mic.guide.module.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mic.guide.arch.base.BaseFragment
import com.mic.guide.module.chat.databinding.FragmentChatDetailBinding

/**
 * 聊天详情页：作为隐式 deepLink 的目标（`aiguide://chat/detail/{conversationId}`）。
 *
 * 其他模块（如 module-home）只靠 URI 字符串跳到这里，**零依赖本 Fragment 类**，
 * 等价于 ARouter 的 path 跳转（见 docs/02-navigation-vs-arouter.md §3.1 做法 B）。
 */
class ChatDetailFragment : BaseFragment<FragmentChatDetailBinding>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentChatDetailBinding.inflate(inflater, container, false)

    override fun initView() {
        val conversationId = arguments?.getString("conversationId").orEmpty()
        binding.tvTitle.text = "聊天会话（module-chat）"
        binding.tvId.text = "conversationId = $conversationId\n（由 home 经 deepLink 跨模块传入）"
    }
}
