package com.mic.guide.module.settings.data.repository

import com.mic.guide.arch.base.BaseRepository
import com.mic.guide.module.settings.data.local.SettingsLocalSource
import com.mic.guide.module.settings.domain.model.SettingItem

/**
 * 设置数据仓库：继承 [BaseRepository]，`safeCall` 在 IO 线程执行并把结果包成 [Result]。
 *
 * 数据来自本地 [SettingsLocalSource]（无网络），演示同一套分层对**本地一次性数据**同样适用：
 * 上层（ViewModel/Fragment）拿到的仍是 `Result<List<SettingItem>>`，与 home/music/video 写法一致。
 */
class SettingsRepository : BaseRepository() {

    suspend fun loadSettings(): Result<List<SettingItem>> = safeCall {
        SettingsLocalSource.load()
    }
}