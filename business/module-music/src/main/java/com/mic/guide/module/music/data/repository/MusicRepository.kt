package com.mic.guide.module.music.data.repository

import com.mic.guide.arch.base.BaseRepository
import com.mic.guide.module.music.data.remote.MusicApiService
import com.mic.guide.module.music.domain.model.Track
import com.mic.guide.support.network.client.NetworkClient

/**
 * 音乐数据仓库：继承 [BaseRepository]，`safeCall` 在 IO 线程执行并把结果包成 [Result]。
 *
 * 已接真实网络（公共测试 API jsonplaceholder）：经 [NetworkClient] 拿到 [MusicApiService]，
 * 把 DTO 映射成领域模型 [Track]。上层（ViewModel/Fragment）无需感知网络细节。
 */
class MusicRepository : BaseRepository() {

    private val api: MusicApiService =
        NetworkClient.createService(MusicApiService::class.java)

    suspend fun loadTracks(): Result<List<Track>> = safeCall {
        api.getAlbums().map { dto ->
            Track(id = dto.id, title = dto.title, artist = "歌手 #${dto.userId}")
        }
    }
}