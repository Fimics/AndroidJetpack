package com.mic.guide.module.music.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mic.guide.arch.base.collectIn
import com.mic.guide.arch.mvvm.MvvmFragment
import com.mic.guide.module.music.databinding.FragmentMusicBinding

/**
 * 音乐首页（MVVM 端到端示范，与 [com.mic.guide.module.home.ui.HomeFragment] 同构）：
 * 逻辑写在 `initView()/observe()` 钩子；`tracks` 是 StateFlow，`collectIn(viewLifecycleOwner)` 收集。
 */
class MusicFragment : MvvmFragment<FragmentMusicBinding, MusicViewModel>() {

    override val viewModel: MusicViewModel by viewModels()

    private val adapter = MusicAdapter()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMusicBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvMusic.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMusic.adapter = adapter
        binding.btnRefresh.setOnClickListener { viewModel.refresh() }
    }

    override fun observe() {
        viewModel.tracks.collectIn(viewLifecycleOwner) { adapter.submit(it) }
    }

    override fun onLoading(loading: Boolean) {
        binding.progress.isVisible = loading
    }
}