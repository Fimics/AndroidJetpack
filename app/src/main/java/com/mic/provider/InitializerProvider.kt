package com.mic.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.mic.server.ServerService
import com.mic.utils.FileTools

class InitializerProvider : ContentProvider() {

    private val tag = "init"
    private val DIR = "json"

    override fun onCreate(): Boolean {
        runServer()
        initDataSource(DIR)
        Log.d("tag", "ContentProvider onCreate...")
        return true
    }

    private fun runServer() {
        ServerService.start(this.context)
    }

    private fun initDataSource(dir: String) {
        this.context?.let {
            FileTools.copyDir(dir, it, object : FileTools.DataSourceCallBack {
                override fun onCompleted() {
                }
            })
        }
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("Not yet implemented")
    }
}