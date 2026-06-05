package com.mic.guide.support.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mic.guide.support.database.entity.CacheEntity
import kotlinx.coroutines.flow.Flow

/** 通用缓存表 DAO：suspend 一次性读写 + Flow 观察（§4 状态与数据流约定）。 */
@Dao
interface CacheDao {

    /** 插入/覆盖一条缓存（相同 key 覆盖）。 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: CacheEntity)

    /** 按 key 取缓存；不存在返回 null。 */
    @Query("SELECT * FROM cache WHERE `key` = :key")
    suspend fun get(key: String): CacheEntity?

    /** 观察某前缀下的全部缓存（如 `home:%`），流式返回。 */
    @Query("SELECT * FROM cache WHERE `key` LIKE :keyPrefix || '%' ORDER BY updatedAt DESC")
    fun observe(keyPrefix: String): Flow<List<CacheEntity>>

    /** 删除一条缓存。 */
    @Query("DELETE FROM cache WHERE `key` = :key")
    suspend fun delete(key: String)

    /** 清空缓存表。 */
    @Query("DELETE FROM cache")
    suspend fun clear()
}