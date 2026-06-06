package com.mic.guide.support.ai

import com.google.gson.Gson
import com.mic.guide.lib.log.Logger
import com.mic.guide.support.ai.config.AiConfig
import com.mic.guide.support.ai.model.AiMessage
import com.mic.guide.support.ai.model.dto.ChatCompletionChunk
import com.mic.guide.support.ai.model.dto.ChatCompletionRequest
import com.mic.guide.support.ai.model.dto.ChatMessageDto
import com.mic.guide.support.ai.remote.AiService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * AI 对话客户端门面（AiGuide 核心能力，§9 扩展）。
 *
 * 提供两条主流用法：
 * - [chat]：一次性 `suspend` 取完整回答（非流式，Retrofit）。
 * - [chatStream]：把网关 SSE 逐 token 包成 `Flow<String>`，UI 边收边渲染（打字机效果，裸 OkHttp）。
 *
 * 配置全读 [AiConfig]（运行期可改）。本类无状态，可单例持有；由 [AiComponent] 注册进 ApiRegistry，
 * 任意模块经 `ApiRegistry.get(AiChatClient::class.java)` 复用。
 */
class AiChatClient(
    private val gson: Gson = Gson(),
) {

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(AiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AiConfig.STREAM_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (AiConfig.debug) HttpLoggingInterceptor.Level.HEADERS
                    else HttpLoggingInterceptor.Level.NONE
                },
            )
            .build()
    }

    private val service: AiService by lazy {
        Retrofit.Builder()
            .baseUrl(AiConfig.baseUrl)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AiService::class.java)
    }

    /** 一次性对话：返回完整回答文本；失败抛 [AiException]。 */
    suspend fun chat(messages: List<AiMessage>): String {
        if (!AiConfig.isConfigured()) throw AiException.notConfigured()
        try {
            val resp = service.chatCompletion(
                authorization = "Bearer ${AiConfig.apiKey}",
                body = ChatCompletionRequest(
                    model = AiConfig.model,
                    messages = messages.map(ChatMessageDto::from),
                    temperature = AiConfig.temperature,
                    stream = false,
                ),
            )
            return resp.firstContent()
        } catch (e: HttpException) {
            throw AiException(e.code(), e.response()?.errorBody()?.string() ?: e.message(), e)
        } catch (e: IOException) {
            throw AiException(AiException.CODE_NETWORK, e.message ?: "网络错误", e)
        }
    }

    /**
     * 流式对话：把 SSE（`data: {chunk}\n\n` / 终止 `data: [DONE]`）逐 token 发到下游。
     *
     * 用 [callbackFlow] 桥接 OkHttp 异步回调；下游取消时 [awaitClose] 取消请求，避免连接泄漏。
     */
    fun chatStream(messages: List<AiMessage>): Flow<String> = callbackFlow {
        if (!AiConfig.isConfigured()) {
            close(AiException.notConfigured())
            return@callbackFlow
        }

        val payload = gson.toJson(
            ChatCompletionRequest(
                model = AiConfig.model,
                messages = messages.map(ChatMessageDto::from),
                temperature = AiConfig.temperature,
                stream = true,
            ),
        )
        val request = Request.Builder()
            .url(AiConfig.baseUrl.trimEnd('/') + "/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${AiConfig.apiKey}")
            .addHeader("Accept", "text/event-stream")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        val call: Call = okHttp.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(AiException(AiException.CODE_NETWORK, e.message ?: "网络错误", e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        close(AiException(resp.code, resp.body?.string() ?: "HTTP ${resp.code}"))
                        return
                    }
                    val source = resp.body?.source()
                    if (source == null) {
                        close(AiException(AiException.CODE_NETWORK, "空响应体"))
                        return
                    }
                    try {
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data.isEmpty()) continue
                            if (data == "[DONE]") break
                            val delta = runCatching {
                                gson.fromJson(data, ChatCompletionChunk::class.java).deltaContent()
                            }.getOrDefault("")
                            if (delta.isNotEmpty()) trySend(delta)
                        }
                        close()
                    } catch (e: IOException) {
                        close(AiException(AiException.CODE_NETWORK, e.message ?: "读取流失败", e))
                    }
                }
            }
        })

        awaitClose {
            Logger.d("chatStream closed, cancel call", tag = "AI")
            call.cancel()
        }
    }
}