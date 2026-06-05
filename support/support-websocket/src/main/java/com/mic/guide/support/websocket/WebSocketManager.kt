package com.mic.guide.support.websocket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * WebSocket 长连接门面（§9）：封装 OkHttp WebSocket 的建连/发送/关闭，
 * 业务（如聊天实时消息）只面对回调，不直接持有 OkHttp 细节。
 *
 * 单连接持有；多路连接可由调用方持有多个实例。
 */
class WebSocketManager(
    private val client: OkHttpClient = OkHttpClient(),
) {

    private var webSocket: WebSocket? = null

    /** 建立连接；通过 [listener] 接收开/收/错/关事件。 */
    fun connect(url: String, listener: WebSocketListener) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    /** 发送文本帧；未连接时返回 false。 */
    fun send(text: String): Boolean = webSocket?.send(text) ?: false

    /** 正常关闭连接（1000 = NORMAL_CLOSURE）。 */
    fun close(code: Int = 1000, reason: String? = null) {
        webSocket?.close(code, reason)
        webSocket = null
    }
}