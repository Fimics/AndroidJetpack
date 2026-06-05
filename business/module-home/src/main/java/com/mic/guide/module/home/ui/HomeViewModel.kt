package com.mic.guide.module.home.ui

import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.home.data.repository.HomeRepository
import com.mic.guide.module.home.domain.model.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 首页 ViewModel：继承 [MvvmViewModel]（= BaseViewModel），用 `StateFlow` 暴露 UI 状态。
 *
 * 无参构造，可由 `by viewModels()` 默认工厂创建；当前手动 new 出 [HomeRepository]
 * （接入 Hilt 后改为 `@HiltViewModel` + `@Inject` 注入，页面写法不变）。
 */
class HomeViewModel : MvvmViewModel() {

    private val repository = HomeRepository()

    private val _feed = MutableStateFlow<List<FeedItem>>(emptyList())
    val feed: StateFlow<List<FeedItem>> = _feed.asStateFlow()

    init {
        refresh()
    }

    fun refresh(page: Int = 1) {
        // 自动管理 loading；失败统一进基类 error(SharedFlow)，无需在此 try/catch
        launchWithLoading {
            repository.loadFeed(page).onSuccess { _feed.value = it }
        }
    }
}
