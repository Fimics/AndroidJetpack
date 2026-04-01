package com.noetix.robotics

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.noetix.libcore.utils.KLog

class NoeTixApp:Application() {

    private val tag= "NoeTixApp"
    override fun onCreate() {
        super.onCreate()
        KLog.d(tag,"NoeTixApp ->onCreate()")
        LiveEventBus.config().enableLogger(false)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        KLog.d(tag,"onTrimMemory level -> $level")
    }
}