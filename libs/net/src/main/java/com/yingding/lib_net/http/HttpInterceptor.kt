package com.yingding.lib_net.http

import android.text.TextUtils
import com.orhanobut.logger.Logger
import com.yingding.lib_net.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * http拦截器
 */
class HttpInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        //统一添加token


        val authorisedBuilder = originalRequest.newBuilder()
        RetrofitUtil.instance.headerMap?.forEach {
            authorisedBuilder.addHeader(it.key, it.value)
        }

        val response = chain.proceed(authorisedBuilder.build())

        if (BuildConfig.DEBUG) {
            val responseBody = response.peekBody((1024 * 1024).toLong())
            //输出打印日志
            val str = StringBuilder()
            str.append("url: ${response.request().url()}\n")
            if (!TextUtils.isEmpty(response.request().headers().toString()))
                str.append("header: ${response.request().headers()}")
            var bodyParm: String? = null
            if (originalRequest.body() != null) {
                val buffer = Buffer()
                originalRequest.body()!!.writeTo(buffer)
                bodyParm = buffer.readUtf8()
            }
            if (!response.request().url().toString().contains("upload")
                && !TextUtils.isEmpty(bodyParm)
                && response.request().method() == "POST"
            )
                str.append("requestBody: ${bodyParm}\n")
            str.append("code: ${response.code()}\n")
            str.append("result: ${responseBody.string()}")
            Logger.e(str.toString())
        }
        return response
    }
}
