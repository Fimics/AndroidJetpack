package com.mic.guide.support.network.client

import com.mic.guide.support.network.config.NetworkConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit/OkHttp 工厂：只负责「怎么发请求」，不含任何业务接口路径（§4.3）。
 * 业务模块用 `NetworkClient.createService(XxxApiService::class.java, baseUrl)` 拿到接口实例。
 */
object NetworkClient {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (NetworkConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .build()
    }

    fun <T> createService(
        service: Class<T>,
        baseUrl: String = NetworkConfig.BASE_URL,
    ): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(service)
}
