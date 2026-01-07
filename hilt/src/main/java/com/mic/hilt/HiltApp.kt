package com.mic.hilt

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

//1.第一步必须要做的事：在app的build.gradle中添加hilt的依赖
@HiltAndroidApp
class HiltApp : Application() {
    private val tag = "init";
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "app onCreate...")
    }
}