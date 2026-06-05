package com.mic.guide.module.music

import com.mic.guide.api.music.MusicApi
import com.mic.guide.api.music.SongInfo
import com.mic.guide.api.player.PlayerApi
import com.mic.guide.arch.api.ApiRegistry

/**
 * [MusicApi] 在本模块的实现（§6）。由 [MusicComponent] 注册进 `ApiRegistry`，
 * 供其他模块（如 module-home）经接口调用「播放/查询当前曲目」，调用方零依赖本类。
 *
 * 真正的播放**复用 `support-media` 的 [PlayerApi]**（Media3/ExoPlayer）：本模块仅依赖 `api-player` 接口、
 * 经 `ApiRegistry` 取实现，零依赖 `support-media`（§6.6）。曲目元信息仍由本模块自管（内存示范）。
 */
class MusicApiImpl : MusicApi {

    @Volatile
    private var current: SongInfo? = null

    override fun play(songId: String) {
        current = SongInfo(id = songId, title = "曲目 $songId", artist = "歌手 $songId")
        // 复用底层播放能力；support-media 未集成时为 null，仅元信息更新不崩
        ApiRegistry.get(PlayerApi::class.java)?.play("https://example.com/audio/$songId.mp3")
    }

    override fun pause() {
        ApiRegistry.get(PlayerApi::class.java)?.pause()
    }

    override fun currentSong(): SongInfo? = current
}