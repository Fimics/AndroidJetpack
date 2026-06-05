package com.mic.guide.module.video.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 视频缓存实体（本模块特征数据库的行；RemoteMediator 写入、Room PagingSource 读出）。 */
@Entity(tableName = "video")
data class VideoEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val thumbnail: String,
)
