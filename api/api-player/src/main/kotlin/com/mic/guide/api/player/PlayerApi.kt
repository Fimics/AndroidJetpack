package com.mic.guide.api.player

/** 播放状态（纯数据，供 UI 观察/展示）。 */
enum class PlayState { IDLE, PLAYING, PAUSED }

/**
 * 通用播放器能力接口（§6）：音/视频播放的底层控制，供 `module-music` / `module-video` 复用。
 *
 * 实现可下沉到 `support-media`（Media3/ExoPlayer），通过本接口对业务暴露，
 * 业务层不直接依赖具体播放器实现。
 */
interface PlayerApi {

    fun play(url: String)

    fun pause()

    fun stop()

    fun state(): PlayState
}