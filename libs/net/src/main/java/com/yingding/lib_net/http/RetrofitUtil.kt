package com.yingding.lib_net.http

import android.annotation.SuppressLint
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.log_print.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * 获取retrofit对象并配置
 */
class RetrofitUtil private constructor() {

    private var httpClient: OkHttpClient? = null//一个项目中这个最好唯一

    private val retrofitCache = HashMap<String, Retrofit>()

    var exceptionInterface: NetExceptionInterface? = null//网络异常

    //添加请求头
    var headerMap: HashMap<String, String>? = null

    private object SingleTonHolder {
        val INSTANCE = RetrofitUtil()
    }

    private fun build(baseUrl: String): Retrofit {
        // 添加公共参数拦截器
        if (httpClient == null) {
            httpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(), TrustAllManager())
                .hostnameVerifier(TrustAllHostnameVerifier())
                .addInterceptor(HttpInterceptor()).also {
//                    if(BuildConfig.DEBUG)
                        it.addInterceptor(getLogInterceptor())
                }.build()

        }

        return Retrofit.Builder()
            .client(httpClient!!)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    private fun getLogInterceptor(): Interceptor {
        return LoggingInterceptor.Builder()
            .executor(Executors.newCachedThreadPool())
            .loggable(true)
            .enableAndroidStudio_v3_LogsHack(true)
            .tag("http").build()
    }

    private fun createSSLSocketFactory(): SSLSocketFactory? {
        var sSLSocketFactory: SSLSocketFactory? = null
        try {
            val sc: SSLContext = SSLContext.getInstance("TLS")
            sc.init(
                null, arrayOf(TrustAllManager()),
                SecureRandom()
            )
            sSLSocketFactory = sc.socketFactory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sSLSocketFactory
    }

    fun <T> getInterface(baseUrl: String, clazz: Class<T>): T {
        var retrofit = retrofitCache[baseUrl]
        if (retrofit == null) {
            retrofit = build(baseUrl)
            retrofitCache[baseUrl] = retrofit
        }
        return retrofit.create(clazz)
    }

    fun clearCache() {
        retrofitCache.clear()
    }

    companion object {
        val instance: RetrofitUtil
            get() = SingleTonHolder.INSTANCE
    }
}

//信任所有证书
@SuppressLint("CustomX509TrustManager")
class TrustAllManager : X509TrustManager {
    @SuppressLint("TrustAllX509TrustManager")
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate>? {
        return arrayOf()
    }

}

class TrustAllHostnameVerifier : HostnameVerifier {
    @SuppressLint("BadHostnameVerifier")
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }

}


