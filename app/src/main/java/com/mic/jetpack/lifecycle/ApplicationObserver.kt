package com.mic.jetpack.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.mic.libcore.utils.KLog

class ApplicationObserver:LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//        KLog.d("lifecycle","event->${event.name}")
    }
}