package com.mic.guide.module.video.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * RemoteMediator 的翻页键（每条视频记录其所在页的前/后页码）。
 * APPEND 时据末条记录的 [nextKey] 决定下一页，null 表示到底。
 */
@Entity(tableName = "video_remote_key")
data class VideoRemoteKey(
    @PrimaryKey val id: Int,
    val prevKey: Int?,
    val nextKey: Int?,
)