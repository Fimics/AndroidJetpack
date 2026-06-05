package com.mic.guide.module.chat.ui

import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.chat.data.repository.ChatRepository
import com.mic.guide.module.chat.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 聊天详情 ViewModel：继承 [MvvmViewModel]（= BaseViewModel），用 `StateFlow` 暴露消息列表。
 *
 * 无参构造，可由 `by viewModels()` 默认工厂创建；当前手动 new 出 [ChatRepository]
 * （接入 Hilt 后改为 `@HiltViewModel` + `@Inject` 注入，页面写法不变）。
 *
 * 与 [HomeViewModel] 不同：会话 id 来自跨模块 deepLink 传参，构造期未知，
 * 因此不在 `init` 加载，由 Fragment 在 `initView()` 调 [load] 触发。
 */
class ChatViewModel : MvvmViewModel() {

    private val repository = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun load(conversationId: String) {
        // 自动管理 loading；失败统一进基类 error(SharedFlow)，无需在此 try/catch
        launchWithLoading {
            repository.loadMessages(conversationId).onSuccess { _messages.value = it }
        }
    }
}