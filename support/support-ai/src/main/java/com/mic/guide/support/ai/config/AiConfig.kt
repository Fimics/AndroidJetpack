package com.mic.guide.support.ai.config

/**
 * AI 能力全局配置（§9 扩展）：OpenAI 兼容协议（`/v1/chat/completions`），
 * 兼容 OpenAI / DeepSeek / 通义千问 / Moonshot 等一切 OpenAI 风格网关——只换 [baseUrl] + [apiKey] + [model]。
 *
 * 运行期可改（如用户在设置页填自己的 Key）：`AiConfig.apiKey = "sk-..."`。
 * 真实工程应把 Key 放安全存储（如 EncryptedSharedPreferences / 后端代签），不要硬编码进包。
 */
object AiConfig {

    /** 网关 BaseUrl，需以 `/` 结尾（Retrofit 要求）。默认 DeepSeek 公有云。 */
    @Volatile
    var baseUrl: String = "https://api.deepseek.com/"

    /** 鉴权 Token（`Authorization: Bearer <key>`）。 */
    @Volatile
    var apiKey: String = ""

    /** 默认模型名。 */
    @Volatile
    var model: String = "deepseek-chat"

    /** 采样温度（0~2，越大越发散）。 */
    @Volatile
    var temperature: Double = 0.7

    /** 单次连接/读写超时（秒）；流式响应读超时单独放大。 */
    const val TIMEOUT_SECONDS: Long = 30L

    /** 流式读超时（秒）：长回答需要更久的 readTimeout。 */
    const val STREAM_READ_TIMEOUT_SECONDS: Long = 120L

    /** Debug 下打开 OkHttp Body 日志（注意流式不会整体打印）。 */
    var debug: Boolean = true

    /** 是否已配置可用 Key。 */
    fun isConfigured(): Boolean = apiKey.isNotBlank() && baseUrl.isNotBlank()
}