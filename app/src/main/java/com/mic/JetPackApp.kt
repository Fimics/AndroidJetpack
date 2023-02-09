package com.mic

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.mic.jetpack.lifecycle.ApplicationObserver

//@HiltAndroidApp
class JetPackApp : Application() {
    private val tag = "init";
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "app onCreate...")
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationObserver())
    }
}