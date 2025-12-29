package com.mic.hilt.http.client

import android.os.Handler
import com.mic.hilt.KLog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class OkHttpProcessor @Inject constructor() : IHttpProcessor {
    private val mOkHttpClient: OkHttpClient= OkHttpClient()
    private val myHandler: Handler= Handler()



    override fun post(url: String?, params: Map<String?, Any?>?, callback: ICallback?) {
        KLog.d("hilt","okhttp post")
        val requestBody:RequestBody = appendBody(params)
        val request: Request =Request.Builder()
            .url(url!!)
            .post(requestBody)
            .build()
        mOkHttpClient.newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body!!.string()
                if (response.isSuccessful) {
                    myHandler.post { callback!!.onSuccess(result) }
                } else {
                    myHandler.post { callback!!.onFailure(result) }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                myHandler.post { callback!!.onFailure("onFailure") }
            }
        })
    }

    private fun appendBody(params: Map<String?, Any?>?): RequestBody {
        val body:FormBody.Builder = FormBody.Builder()
        if (params.isNullOrEmpty()) {
            return body.build()
        }
        for ((key, value) in params) {
            key?.let { body.add(it, value.toString()) }
        }
        return body.build()
    }
}