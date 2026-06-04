package com.mic.guide

import android.app.Application
import android.util.Log

class GuideApp : Application() {
    private val tag = "init";
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "app onCreate...")
    }
}