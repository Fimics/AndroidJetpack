package com.mic.rx

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val httpBuilder = OkHttpClient.Builder()
    private val okHttpClient = httpBuilder
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(getLoggingInterceptor(false))
        .build()


    @JvmStatic
    fun getRetrofitClient(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))  //添加json解析工具
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  //添加rxjava处理工具
            .build()
    }

    private fun getLoggingInterceptor(isFileClient: Boolean): Interceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        return if (isFileClient) {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
            } else {
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            }
    }
}