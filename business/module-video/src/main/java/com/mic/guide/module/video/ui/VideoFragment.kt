package com.mic.guide.module.video.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.api.player.PlayerApi
import com.mic.guide.arch.api.ApiRegistry
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.video.databinding.FragmentVideoBinding

/**
 * 视频首页（Paging 3 分页 + 列表项复用 PlayerApi 播放）：
 * `videos` 是 `Flow<PagingData>`，滚动到底自动加载下一页；进度/刷新由 `loadStateFlow` 驱动。
 */
class VideoFragment : MvvmFragment<FragmentVideoBinding, VideoViewModel>() {

    override val viewModel: VideoViewModel by viewModels()

    // 点击列表项 → 复用 support-media 的 PlayerApi 播放（经 api-player 接口，零依赖 support-media，§6.6）
    private val adapter = VideoAdapter { item ->
        val player = ApiRegistry.get(PlayerApi::class.java)
        if (player != null) {
            player.play(SAMPLE_VIDEO_URL)
            toast("播放：${item.title}")
        } else {
            toast("播放能力未集成")
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentVideoBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvVideo.layoutManager = LinearLayoutManager(requireContext())
        // 列表 + 页脚（加载更多/重试）：withLoadStateFooter 拼成 ConcatAdapter
        binding.rvVideo.adapter = adapter.withLoadStateFooter(
            footer = VideoLoadStateAdapter { adapter.retry() },
        )
        binding.btnRefresh.setOnClickListener { adapter.refresh() }

        // 首页刷新中显示进度条（Paging 用 LoadState 表达加载态，替代基类 loading）
        adapter.loadStateFlow.collectIn(viewLifecycleOwner) { states ->
            binding.progress.isVisible = states.refresh is LoadState.Loading
        }
    }

    override fun observe() {
        viewModel.videos.collectIn(viewLifecycleOwner) { adapter.submitData(it) }
    }

    private companion object {
        // 示范媒体源（jsonplaceholder 无真实视频流）；接自家后端后用 item 的播放地址
        const val SAMPLE_VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    }
}