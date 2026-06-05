package com.mic.guide.module.video.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mic.guide.arch.base.BaseApplication
import com.mic.guide.module.video.data.VideoRemoteMediator
import com.mic.guide.module.video.data.local.VideoDatabase
import com.mic.guide.module.video.data.remote.VideoApiService
import com.mic.guide.module.video.domain.model.VideoItem
import com.mic.guide.support.network.client.NetworkClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 视频数据仓库：Paging 3 + RemoteMediator + Room 的「分页离线缓存」（§9.2）。
 *
 * 单一数据源是 Room（`VideoDao.pagingSource`），网络由 [VideoRemoteMediator] 补页写库；
 * 上层拿到的是 `Flow<PagingData<VideoItem>>`：离线看缓存页、在线滚动到底自动拉新页。
 */
@OptIn(ExperimentalPagingApi::class)
class VideoRepository {

    private val api: VideoApiService =
        NetworkClient.createService(VideoApiService::class.java)

    private val db = VideoDatabase.get(BaseApplication.appContext)

    fun videoPager(): Flow<PagingData<VideoItem>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = VideoRemoteMediator(api, db),
        pagingSourceFactory = { db.videoDao().pagingSource() },
    ).flow.map { pagingData ->
        pagingData.map { entity ->
            VideoItem(id = entity.id, title = entity.title, thumbnail = entity.thumbnail)
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}