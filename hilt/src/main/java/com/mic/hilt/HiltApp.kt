package com.mic.hilt

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApp : Application() {
    private val tag = "init";
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "app onCreate...")
    }
}