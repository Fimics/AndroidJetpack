package com.mic.guide.support.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mic.guide.support.database.dao.CacheDao
import com.mic.guide.support.database.entity.CacheEntity

/**
 * 全局 Room 数据库（§9）：库名/版本取自 [DatabaseConfig]。
 *
 * 多模块下，业务自有 `@Entity`/`@Dao` 有两种落地法：
 * ① 简单期：把业务实体登记到本 `entities` 数组、在此暴露其 Dao（集中一处，本工程当前做法的基底）；
 * ② 规模化：各业务模块各自建独立数据库（互不影响 schema/版本）。
 *
 * 当前内置一张通用 `cache` 表（[CacheEntity]）作为基础设施缓存，新增业务实体时把它加进 `entities` 并加一个 abstract dao 即可。
 */
@Database(
    entities = [CacheEntity::class],
    version = DatabaseConfig.VERSION,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cacheDao(): CacheDao
}