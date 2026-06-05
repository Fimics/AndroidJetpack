package com.mic.guide.module.music.ui

import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.music.data.repository.MusicRepository
import com.mic.guide.module.music.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 音乐 ViewModel：继承 [MvvmViewModel]（= BaseViewModel），用 `StateFlow` 暴露歌单。
 *
 * 无参构造，可由 `by viewModels()` 默认工厂创建；接入 Hilt 后改为 `@HiltViewModel` + `@Inject`。
 */
class MusicViewModel : MvvmViewModel() {

    private val repository = MusicRepository()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchWithLoading {
            repository.loadTracks().onSuccess { _tracks.value = it }
        }
    }
}