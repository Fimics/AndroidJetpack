package com.mic.guide.support.media

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mic.guide.api.player.PlayState
import com.mic.guide.api.player.PlayerApi

/**
 * [PlayerApi] 的 Media3/ExoPlayer 实现（§6 / §9）：把播放器细节封在 `support-media`，
 * 业务（music/video）只面对 `api-player` 接口、零依赖本类与 Media3。
 *
 * ExoPlayer 必须在主线程创建/操作；[player] 懒构造，首次调用（来自 UI 主线程的播放动作）时建。
 */
class Media3PlayerApi(context: Context) : PlayerApi {

    private val appContext = context.applicationContext

    private val player: ExoPlayer by lazy { ExoPlayer.Builder(appContext).build() }

    override fun play(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun stop() {
        player.stop()
    }

    override fun state(): PlayState = when {
        player.isPlaying -> PlayState.PLAYING
        player.playbackState == Player.STATE_READY && !player.playWhenReady -> PlayState.PAUSED
        else -> PlayState.IDLE
    }
}