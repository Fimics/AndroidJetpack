package com.mic.guide.lib.test

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.ExternalResource

/**
 * JUnit 规则：托管一个 [MockWebServer] 的启动/关闭，给 Repository/ApiService 提供假后端。
 *
 * 用法：
 * ```
 * @get:Rule val server = MockWebServerRule()
 * @Test fun xxx() {
 *     server.enqueueJson("""{"code":0}""")
 *     val api = NetworkClient.createService(XxxApi::class.java, server.baseUrl)
 *     ...
 * }
 * ```
 */
class MockWebServerRule : ExternalResource() {

    lateinit var server: MockWebServer
        private set

    /** 直接传给 Retrofit baseUrl 的地址（以 `/` 结尾）。 */
    val baseUrl: String get() = server.url("/").toString()

    override fun before() {
        server = MockWebServer().apply { start() }
    }

    override fun after() {
        server.shutdown()
    }

    /** 入队一个 JSON 成功响应。 */
    fun enqueueJson(body: String, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body),
        )
    }

    /** 入队一个错误响应。 */
    fun enqueueError(code: Int = 500) {
        server.enqueue(MockResponse().setResponseCode(code))
    }
}
