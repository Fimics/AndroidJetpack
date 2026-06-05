package com.mic.guide.module.video.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** RemoteMediator 翻页键 DAO。 */
@Dao
interface VideoRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<VideoRemoteKey>)

    @Query("SELECT * FROM video_remote_key WHERE id = :id")
    suspend fun remoteKey(id: Int): VideoRemoteKey?

    @Query("DELETE FROM video_remote_key")
    suspend fun clear()
}