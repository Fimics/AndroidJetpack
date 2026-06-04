package com.mic.hilt.demo.http.client

import android.content.Context
import com.mic.hilt.KLog
import javax.inject.Inject

/**
 * 真实的操作在这里
 * 业主
 */
class VolleyProcessor @Inject constructor( context: Context?) : IHttpProcessor {
    override fun post(url: String?, params: Map<String?, Any?>?, callback: ICallback?) {
        KLog.d("hilt","volley post")
    }
}