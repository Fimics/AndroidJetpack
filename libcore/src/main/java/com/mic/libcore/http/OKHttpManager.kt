package com.mic.libcore.http

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import java.security.NoSuchAlgorithmException
import java.security.KeyManagementException
import java.util.concurrent.TimeUnit

class OKHttpManager {
    companion object {
        const val tag = "OKHttpManager"
        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(createSslSocketFactory(), createTrustManager())
            .hostnameVerifier { _, _ -> true } // 忽略主机名验证
//            .addNetworkInterceptor(object :Interceptor{
//                override fun intercept(chain: Interceptor.Chain): Response {
//                    val request = chain.request()
//                    val url = request.url
//                    KLog.d(tag, "请求地址 ->${url}")
//                    return chain.proceed(request)
//                }
//            })
            .build()

        @JvmStatic
        fun get(): OkHttpClient {
            return okHttpClient
        }

        // 创建 SSL Socket Factory 并跳过证书验证
        private fun createSslSocketFactory(): javax.net.ssl.SSLSocketFactory {
            try {
                val trustAllCertificates = arrayOf<TrustManager>(createTrustManager())
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCertificates, java.security.SecureRandom())
                return sslContext.socketFactory
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("No such algorithm exception", e)
            } catch (e: KeyManagementException) {
                throw RuntimeException("Key management exception", e)
            }
        }

        // 创建一个 X509TrustManager 忽略证书验证
        private fun createTrustManager(): X509TrustManager {
            return object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

                // 返回空数组而不是null
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf() // 返回空数组
                }
            }
        }
    }
}
