package com.mic.guide.support.push

import android.content.Context
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 推送总门面（§9 扩展）：聚合具体 [PushProvider]（FCM/极光/…），对业务暴露统一的
 * token 状态流（[token]）与消息事件流（[messages]），并把到达消息落地为系统通知。
 *
 * 用法：
 * 1. `PushManager.init(context, FcmPushProvider())`；
 * 2. UI 收 `PushManager.messages` 做角标/跳转，收 `PushManager.token` 上报后端；
 * 3. Provider 收到下行消息时调 [onMessageReceived]。
 */
object PushManager {

    private var provider: PushProvider? = null
    private var notificationHelper: NotificationHelper? = null
    private var defaultIconRes: Int = 0

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _messages = MutableSharedFlow<PushMessage>(extraBufferCapacity = 16)
    val messages: SharedFlow<PushMessage> = _messages.asSharedFlow()

    /** 初始化：传入具体通道实现与默认通知小图标。 */
    fun init(context: Context, provider: PushProvider, notificationIconRes: Int) {
        this.provider = provider
        this.defaultIconRes = notificationIconRes
        this.notificationHelper = NotificationHelper(context)
        provider.register { token ->
            Logger.d("push token from ${provider.name}: $token", tag = "Push")
            _token.value = token
        }
    }

    /** Provider 收到下行消息后回调本方法：分发事件 + 展示通知。 */
    fun onMessageReceived(message: PushMessage, showNotification: Boolean = true) {
        _messages.tryEmit(message)
        if (showNotification && defaultIconRes != 0) {
            notificationHelper?.show(message.title, message.body, defaultIconRes)
        }
    }

    fun unregister() {
        provider?.unregister()
    }
}
