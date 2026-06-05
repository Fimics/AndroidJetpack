package com.mic.guide.support.network.config

/** 网络全局配置：BaseUrl、超时、是否 Debug（开 Body 日志）。 */
object NetworkConfig {

    /** 默认 BaseUrl（示范用公共测试 API；接入自家后端时改这里或按模块传入）。 */
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    const val TIMEOUT_SECONDS = 15L

    const val DEBUG = true
}
