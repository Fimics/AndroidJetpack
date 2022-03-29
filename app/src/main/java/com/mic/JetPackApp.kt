package com.mic

import android.app.Application
import android.util.Log

class JetPackApp : Application() {
    private val tag = "init";
    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "app onCreate...")
    }
}