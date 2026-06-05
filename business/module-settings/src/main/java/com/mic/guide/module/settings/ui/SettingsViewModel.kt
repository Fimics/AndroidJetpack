package com.mic.guide.module.settings.ui

import com.mic.guide.arch.mvvm.MvvmViewModel
import com.mic.guide.module.settings.data.repository.SettingsRepository
import com.mic.guide.module.settings.domain.model.SettingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设置 ViewModel：继承 [MvvmViewModel]（= BaseViewModel），用 `StateFlow` 暴露设置项列表。
 *
 * 无参构造，可由 `by viewModels()` 默认工厂创建；接入 Hilt 后改为 `@HiltViewModel` + `@Inject`。
 */
class SettingsViewModel : MvvmViewModel() {

    private val repository = SettingsRepository()

    private val _items = MutableStateFlow<List<SettingItem>>(emptyList())
    val items: StateFlow<List<SettingItem>> = _items.asStateFlow()

    init {
        launchWithLoading {
            repository.loadSettings().onSuccess { _items.value = it }
        }
    }
}