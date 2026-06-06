package com.mic.guide.api.video

/** 视频条目（纯数据，跨模块传递）。 */
data class VideoInfo(
    val id: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String?,
)

/**
 * 视频能力接口（§6，与 `module-video` 对称）。
 *
 * 其他模块（如首页推荐位想直接起播一个视频）只依赖本接口，不依赖 `module-video`。
 * 实现类在 `module-video` 注册进 `ApiRegistry`；播放本身委托 `api-player`（§6.6）。
 */
interface VideoApi {

    /** 按 URL 播放一个视频（内部委托 PlayerApi）。 */
    fun play(video: VideoInfo)

    /** 最近一次播放的视频，无则 null。 */
    fun lastPlayed(): VideoInfo?
}
