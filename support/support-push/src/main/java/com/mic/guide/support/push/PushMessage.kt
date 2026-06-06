package com.mic.guide.support.push

/** 一条推送消息（与具体推送厂商无关的统一模型）。 */
data class PushMessage(
    val title: String?,
    val body: String?,
    val data: Map<String, String> = emptyMap(),
    val messageId: String? = null,
)

/**
 * 推送通道适配契约：FCM / 极光 / 个推 / 华为 等各自实现本接口，
 * [PushManager] 只面对本接口——换厂商不改业务（§11 通信解耦同理）。
 */
interface PushProvider {

    val name: String

    /** 初始化并注册，回调返回 deviceToken（拿不到则传 null）。 */
    fun register(onToken: (String?) -> Unit)

    /** 注销（如退登时停止推送）。 */
    fun unregister()
}
