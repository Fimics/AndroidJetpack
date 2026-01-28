package com.mic.py

import android.app.Application
import com.mic.libpy.AppContextHolder

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.app = this
    }
}