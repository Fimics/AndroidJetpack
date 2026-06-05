package com.mic.guide.module.chat.data.repository

import com.mic.guide.arch.base.BaseRepository
import com.mic.guide.module.chat.data.remote.ChatApiService
import com.mic.guide.module.chat.domain.model.Message
import com.mic.guide.support.network.client.NetworkClient

/**
 * 聊天数据仓库：继承 [BaseRepository]，`safeCall` 在 IO 线程执行并把结果包成 [Result]。
 *
 * 已接真实网络（公共测试 API jsonplaceholder）：经 [NetworkClient] 拿到 [ChatApiService]，
 * 把 DTO 映射成领域模型 [Message]。上层（ViewModel/Fragment）无需感知网络细节。
 */
class ChatRepository : BaseRepository() {

    private val api: ChatApiService =
        NetworkClient.createService(ChatApiService::class.java)

    /**
     * 加载某会话的消息列表。
     *
     * 当前 conversationId 仅用于上层展示标题；接自家后端后作为路径参数透传给 [ChatApiService]。
     * 按 id 奇偶交替 `fromMe`，演示左右气泡。
     */
    suspend fun loadMessages(conversationId: String): Result<List<Message>> = safeCall {
        api.getMessages().map { dto ->
            Message(
                id = dto.id,
                author = dto.name,
                content = dto.body,
                fromMe = dto.id % 2 == 0,
            )
        }
    }
}