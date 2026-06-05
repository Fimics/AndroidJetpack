package com.mic.guide.module.video.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.video.data.repository.VideoRepository
import com.mic.guide.module.video.domain.model.VideoItem
import kotlinx.coroutines.flow.Flow

/**
 * 视频 ViewModel：继承 [MvvmViewModel]，用 Paging 3 暴露分页流。
 *
 * `cachedIn(viewModelScope)` 让分页数据在配置变更（旋转）后仍存活、不重新请求。
 * loading/error 不再走基类 StateFlow，而由 UI 观察 `PagingDataAdapter.loadStateFlow`（§8 列表分页）。
 */
class VideoViewModel : MvvmViewModel() {

    private val repository = VideoRepository()

    val videos: Flow<PagingData<VideoItem>> =
        repository.videoPager().cachedIn(viewModelScope)
}