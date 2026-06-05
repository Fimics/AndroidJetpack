package com.mic.guide.support.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 通用缓存表（support-database 作为基础设施提供）：业务把结构化/较大的缓存（如 feed JSON）
 * 按 key 存这里；轻量偏好仍走 `support-storage`（DataStore）。
 *
 * @param key 业务自定义键（建议带模块前缀，如 `home:feed:1`）
 * @param value 序列化后的内容（JSON 文本等）
 * @param updatedAt 写入时间戳（毫秒），便于做过期判断
 */
@Entity(tableName = "cache")
data class CacheEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
)