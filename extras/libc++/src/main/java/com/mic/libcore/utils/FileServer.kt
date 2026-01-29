package com.mic.libcore.utils

import android.util.Log
import com.mic.server.ServerService
import kotlin.concurrent.thread

class FileServer {
    private val tag = "init"
    private val DIR = "json"

    fun start(){
        runServer()
        initDataSource(DIR)
        Log.d(tag, "ContentProvider onCreate...")
    }

    private fun runServer() {
        ServerService.start(AppGlobals.getApplication())
    }

    private fun initDataSource(dir: String) {
        AppGlobals.getApplication()?.let {
            thread {
                FileTools.copyDir(dir, it, object : FileTools.DataSourceCallBack {
                    override fun onCompleted() {
                    }
                })
            }
        }
    }
}