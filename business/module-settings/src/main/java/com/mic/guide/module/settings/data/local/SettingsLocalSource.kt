package com.mic.guide.module.settings.data.local

import com.mic.guide.module.settings.domain.model.SettingItem

/**
 * 设置项本地数据源（示范用静态表；接 DataStore 后改为读取持久化偏好）。
 *
 * 设置页与 home/music/video 不同：数据天然在本地、不走网络，因此本模块**不依赖 support-network**，
 * 但仍走相同的 Repository → ViewModel → Fragment 分层（§4 说明：本地一次性数据同样经 `safeCall`）。
 */
object SettingsLocalSource {

    fun load(): List<SettingItem> = listOf(
        SettingItem(1, "账号与安全", "登录设备、密码、注销"),
        SettingItem(2, "消息通知", "推送、声音、振动"),
        SettingItem(3, "深色模式", "跟随系统 / 开 / 关"),
        SettingItem(4, "存储与缓存", "清理缓存、下载目录"),
        SettingItem(5, "关于", "版本号、开源许可、检查更新"),
    )
}