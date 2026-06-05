package com.mic.guide.module.video.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mic.guide.module.video.data.local.VideoDatabase
import com.mic.guide.module.video.data.local.VideoEntity
import com.mic.guide.module.video.data.local.VideoRemoteKey
import com.mic.guide.module.video.data.remote.VideoApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * 视频分页的网络中介（Paging 3 RemoteMediator，§9.2）：把网络页写入 [VideoDatabase]，
 * Room 的 PagingSource 作为「单一数据源」对外发数据——因此**离线也能翻看已缓存的页**，
 * 在线时网络补齐后续页。`REFRESH` 清表重拉，`APPEND` 据末条的 `nextKey` 取下一页。
 */
@OptIn(ExperimentalPagingApi::class)
class VideoRemoteMediator(
    private val api: VideoApiService,
    private val db: VideoDatabase,
) : RemoteMediator<Int, VideoEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, VideoEntity>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                val key = db.remoteKeyDao().remoteKey(lastItem.id)
                // nextKey 为 null：若键存在说明确实到底，否则等待后续加载
                key?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = key != null)
            }
        }

        return try {
            val items = api.getPhotos(page = page, limit = state.config.pageSize).map { dto ->
                VideoEntity(id = dto.id, title = dto.title, thumbnail = dto.thumbnailUrl)
            }
            val endReached = items.isEmpty()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeyDao().clear()
                    db.videoDao().clear()
                }
                val prev = if (page == FIRST_PAGE) null else page - 1
                val next = if (endReached) null else page + 1
                db.remoteKeyDao().insertAll(items.map { VideoRemoteKey(it.id, prev, next) })
                db.videoDao().upsertAll(items)
            }
            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}