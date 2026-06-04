package com.mic.hilt.demo.http.client

import android.app.Application
import com.mic.hilt.KLog
import javax.inject.Inject

class XUtilsProcessor @Inject constructor(app: Application?) : IHttpProcessor {
    override fun post(url: String?, params: Map<String?, Any?>?, callback: ICallback?) {
        KLog.d("hilt","xutils post")
    }
}