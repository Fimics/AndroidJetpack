package com.mic.guide.arch.mvp

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * MVP Presenter 基类。以弱引用持有 View，避免内存泄漏；
 * 同时作为 [DefaultLifecycleObserver] 在宿主销毁时自动解绑。
 *
 * @param V View 契约类型
 */
abstract class MvpPresenter<V : MvpView> : DefaultLifecycleObserver {

    private var viewRef: WeakReference<V>? = null

    /** 当前绑定的 View，可能为 null（已解绑）。 */
    protected val view: V? get() = viewRef?.get()

    protected val isViewAttached: Boolean get() = view != null

    fun attach(view: V) {
        viewRef = WeakReference(view)
    }

    fun detach() {
        viewRef?.clear()
        viewRef = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        detach()
    }
}
