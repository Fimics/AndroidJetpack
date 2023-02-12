package com.mic.jetpack.http.client

import android.app.Application
import com.mic.KLog
import javax.inject.Inject

class XUtilsProcessor @Inject constructor(app: Application?) : IHttpProcessor {
    override fun post(url: String?, params: Map<String?, Any?>?, callback: ICallback?) {
        KLog.d("hilt","xutils post")
    }
}