package com.mic.ble.client.model

/**
 * WiFi 配网状态枚举
 *
 * 表示设备在配网过程中的各个阶段状态
 * 由服务器设备通过 GATT 通知发送给客户端
 */
enum class ProvisioningStatus {
    /**
     * 空闲状态
     * 表示设备未在配网，等待凭证
     */
    IDLE,

    /**
     * 正在接收凭证
     * 表示设备已收到 SSID 和密码，正在处理
     */
    RECEIVING_CREDENTIALS,

    /**
     * 正在连接到 WiFi
     * 表示设备正在使用收到的凭证尝试连接到 WiFi 网络
     */
    CONNECTING_TO_WIFI,

    /**
     * 成功状态
     * 表示设备已成功连接到指定的 WiFi 网络
     */
    SUCCESS,

    /**
     * 失败状态
     * 表示配网过程失败（可能是密码错误或网络不可用）
     */
    FAILED;

    /**
     * 判断当前状态是否表示正在进行配网
     *
     * @return true 如果处于接收凭证或连接 WiFi 状态，false 否则
     */
    fun isProvisioning(): Boolean = this in setOf(RECEIVING_CREDENTIALS, CONNECTING_TO_WIFI)
}
