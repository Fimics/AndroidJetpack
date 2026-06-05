package com.mic.guide.api.music

/** 当前播放歌曲的对外快照（纯数据，跨模块传递）。 */
data class SongInfo(
    val id: String,
    val title: String,
    val artist: String,
)

/**
 * 音乐能力接口（§6）：典型的「非 UI 能力」——播放、查询当前曲目，
 * 适合走 api 接口而非路由（§5.11）。实现在 `module-music`，调用方只依赖本接口。
 */
interface MusicApi {

    fun play(songId: String)

    fun pause()

    /** 当前正在播放的歌曲；未播放时返回 null。 */
    fun currentSong(): SongInfo?
}