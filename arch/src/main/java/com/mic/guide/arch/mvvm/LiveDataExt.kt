package com.mic.guide.arch.mvvm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * LiveData 观察的简化扩展：`liveData.observeWith(owner) { ... }`。
 */
inline fun <T> LiveData<T>.observeWith(owner: LifecycleOwner, crossinline block: (T) -> Unit) {
    observe(owner) { block(it) }
}
