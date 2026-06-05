package com.mic.guide.module.home.ui

import androidx.lifecycle.viewModelScope
import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.home.data.repository.HomeRepository
import com.mic.guide.module.home.domain.model.FeedItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * 首页 ViewModel：继承 [MvvmViewModel]（= BaseViewModel）。
 *
 * **缓存优先**：[feed] 直接由 Repository 的缓存流 `stateIn` 而来——冷启动先发缓存、磁盘变化自动更新；
 * `init` 触发一次 [refresh] 做网络后台刷新（写缓存即驱动 [feed] 再发）。接入 Hilt 后改 `@HiltViewModel`，写法不变。
 */
class HomeViewModel : MvvmViewModel() {

    private val repository = HomeRepository()

    val feed: StateFlow<List<FeedItem>> = repository.feedStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh(page: Int = 1) {
        // 网络后台刷新：自动管理 loading；失败进基类 error(SharedFlow)，UI 仍显示缓存
        launchWithLoading { repository.refresh(page) }
    }
}