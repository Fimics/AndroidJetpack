package com.mic.guide.module.video.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 视频特征数据库（§9.2）：本模块自管 schema/版本，与 `support-database` 的通用库分开
 * ——印证 `AppDatabase` 注释里「规模化后各业务可建独立数据库」。仅供 RemoteMediator 的离线缓存用。
 */
@Database(
    entities = [VideoEntity::class, VideoRemoteKey::class],
    version = 2,   // v2：缩略图源由 via.placeholder 改 picsum，destructive 重建丢弃旧缓存行
    exportSchema = false,
)
abstract class VideoDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun remoteKeyDao(): VideoRemoteKeyDao

    companion object {
        @Volatile
        private var instance: VideoDatabase? = null

        fun get(context: Context): VideoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                VideoDatabase::class.java,
                "aiguide_video.db",
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                .also { instance = it }
        }
    }
}