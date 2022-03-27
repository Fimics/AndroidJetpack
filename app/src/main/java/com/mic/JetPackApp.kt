package com.mic

import android.app.Application
import android.util.Log

class JetPackApp:Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("application","app onCreate...")
    }

}