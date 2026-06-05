package com.mic.guide.arch.mvvm

import com.mic.guide.arch.base.BaseViewModel

/**
 * MVVM ViewModel 基类，直接复用 [BaseViewModel] 的 Loading / 异常 / 协程封装。
 * 业务 ViewModel 继承它，推荐用 `StateFlow` 暴露 UI 状态，页面侧用 `collectIn` 收集。
 */
abstract class MvvmViewModel : BaseViewModel()
