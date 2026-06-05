package com.mic.guide.module.video.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** 视频缓存 DAO：Room 直接生成 [PagingSource]，作为 Paging 的「单一数据源」。 */
@Dao
interface VideoDao {

    /** 分页读取（Room 生成 PagingSource，按 id 升序即入库顺序）。 */
    @Query("SELECT * FROM video ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<VideoEntity>)

    @Query("DELETE FROM video")
    suspend fun clear()
}