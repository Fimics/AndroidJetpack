package com.mic

import android.app.Application
import android.content.Context
import android.util.Log
import com.mic.server.ServerService
import com.mic.utils.FileTools

class JetPackApp : Application() {

    override fun onCreate() {
        super.onCreate()
        runServer()
        initDataSource("json")
        Log.d("application", "app onCreate...")
    }

    private fun runServer() {
        ServerService.start(this)
    }

    private fun initDataSource(dir: String) {
        FileTools.copyDir("json",this,object :FileTools.DataSourceCallBack{
            override fun onCompleted() {
            }
        })
    }
}