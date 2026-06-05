package com.mic.guide.module.home.data.repository

import com.mic.guide.arch.base.BaseRepository
import com.mic.guide.module.home.data.remote.HomeApiService
import com.mic.guide.module.home.domain.model.FeedItem
import com.mic.guide.support.network.client.NetworkClient

/**
 * 首页数据仓库：继承 [BaseRepository]，`safeCall` 在 IO 线程执行并把结果包成 [Result]。
 *
 * 已接真实网络（公共测试 API jsonplaceholder）：经 [NetworkClient] 拿到 [HomeApiService]，
 * 把 DTO 映射成领域模型 [FeedItem]。上层（ViewModel/Fragment）无需感知网络细节。
 */
class HomeRepository : BaseRepository() {

    private val api: HomeApiService =
        NetworkClient.createService(HomeApiService::class.java)

    suspend fun loadFeed(page: Int = 1): Result<List<FeedItem>> = safeCall {
        api.getPosts(page = page).map { dto ->
            FeedItem(id = dto.id, title = dto.title, subtitle = dto.body)
        }
    }
}
