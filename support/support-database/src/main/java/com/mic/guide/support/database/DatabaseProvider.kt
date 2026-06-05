package com.mic.guide.support.database

import android.content.Context
import androidx.room.Room

/**
 * [AppDatabase] 单例工厂（§9）：进程内唯一实例，线程安全双检锁构造。
 *
 * 业务模块的 `data/local` 经此拿到 Dao：`DatabaseProvider.get(context).cacheDao()`，
 * 不直接接触 `Room.databaseBuilder`。库名来自 [DatabaseConfig.DB_NAME]。
 */
object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DatabaseConfig.DB_NAME,
        )
            .fallbackToDestructiveMigration(dropAllTables = true)   // 示范：版本升级直接重建；正式接入应写 Migration
            .build()
            .also { instance = it }
    }

    /** 便捷取用通用缓存 Dao。 */
    fun cacheDao(context: Context) = get(context).cacheDao()
}