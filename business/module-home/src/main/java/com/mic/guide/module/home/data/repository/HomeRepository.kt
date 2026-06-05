package com.mic.guide.module.home.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mic.guide.arch.base.BaseApplication
import com.mic.guide.arch.base.BaseRepository
import com.mic.guide.module.home.data.remote.HomeApiService
import com.mic.guide.module.home.domain.model.FeedItem
import com.mic.guide.support.database.DatabaseProvider
import com.mic.guide.support.database.entity.CacheEntity
import com.mic.guide.support.network.client.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 首页数据仓库：继承 [BaseRepository]，`safeCall` 在 IO 线程执行并把结果包成 [Result]。
 *
 * **缓存优先 + 网络后台刷新（§9.1）**：UI 的数据来自 [feedStream]——它观察 `support-database` 的
 * 通用缓存表（`CacheDao.observe`），命中即发、磁盘变化自动再发；[refresh] 仅负责取网络并**写缓存**，
 * 写入后 Room 的 `Flow` 自动把新数据推给 UI。因此「显示」与「刷新」解耦：离线直接走缓存、在线后台更新。
 */
class HomeRepository : BaseRepository() {

    private val api: HomeApiService =
        NetworkClient.createService(HomeApiService::class.java)

    private val cacheDao = DatabaseProvider.cacheDao(BaseApplication.appContext)
    private val gson = Gson()

    /** 缓存流：观察该页缓存行，发出解析后的 feed；空缓存发空列表（冷启动先显示上次内容）。 */
    fun feedStream(page: Int = 1): Flow<List<FeedItem>> =
        cacheDao.observe(cacheKey(page)).map { entities ->
            entities.flatMap { parse(it.value) }
        }

    /** 后台刷新：取网络成功即写缓存（→ [feedStream] 自动再发）；失败上抛，UI 保留缓存内容不被清空。 */
    suspend fun refresh(page: Int = 1): Result<Unit> = safeCall {
        val items = api.getPosts(page = page).map { dto ->
            FeedItem(id = dto.id, title = dto.title, subtitle = dto.body)
        }
        cacheDao.put(
            CacheEntity(
                key = cacheKey(page),
                value = gson.toJson(items),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun parse(json: String): List<FeedItem> {
        val type = object : TypeToken<List<FeedItem>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun cacheKey(page: Int): String = "home:feed:$page"
}