package com.mic.guide.support.database

/**
 * 数据库全局配置（§9）：集中库名/版本，被 [AppDatabase] 的 `@Database` 引用（[VERSION] 须为编译期常量）。
 *
 * 已落地 Room：[AppDatabase]（`@Database`）+ [com.mic.guide.support.database.entity.CacheEntity]（`@Entity`）
 * + [com.mic.guide.support.database.dao.CacheDao]（`@Dao`），经 [DatabaseProvider] 取单例。
 * 业务模块的 `data/local` 用 `DatabaseProvider.cacheDao(context)` 拿 Dao，不直接接触 Room builder。
 */
object DatabaseConfig {

    const val DB_NAME = "aiguide.db"

    const val VERSION = 1
}