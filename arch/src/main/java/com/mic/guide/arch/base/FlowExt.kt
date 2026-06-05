package com.mic.guide.arch.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 在 [owner] 的生命周期内安全收集 Flow：进入 [activeState] 时开始收集，
 * 低于该状态时自动取消，避免后台无谓消费与内存泄漏。
 *
 * - Activity：传 `this`。
 * - Fragment：务必传 `viewLifecycleOwner`，而非 Fragment 本身。
 *
 * ```
 * viewModel.uiState.collectIn(viewLifecycleOwner) { render(it) }
 * ```
 */
inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (T) -> Unit,
) {
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(activeState) {
            collect { block(it) }
        }
    }
}
