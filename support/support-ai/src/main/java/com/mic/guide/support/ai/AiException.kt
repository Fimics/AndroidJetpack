package com.mic.guide.support.ai

/** AI 调用异常：携带 HTTP 状态码与网关返回的错误体，便于上层区分鉴权失败/限流/服务错误。 */
class AiException(
    val statusCode: Int,
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    companion object {
        const val CODE_NOT_CONFIGURED = -1
        const val CODE_NETWORK = -2

        fun notConfigured() =
            AiException(CODE_NOT_CONFIGURED, "AI 未配置：请先设置 AiConfig.apiKey")
    }
}