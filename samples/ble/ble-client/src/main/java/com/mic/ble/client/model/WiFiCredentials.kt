package com.mic.ble.client.model

/**
 * WiFi 凭证数据模型
 *
 * 用于存储和传输 WiFi 网络的 SSID 和密码信息
 * 通过 BLE 特征将此数据发送到目标设备
 *
 * @property ssid WiFi 网络名称（最大 32 字节）
 * @property password WiFi 网络密码（最大 64 字节）
 */
data class WiFiCredentials(
    val ssid: String = "",
    val password: String = ""
) {
    /**
     * 验证凭证是否有效
     * 确保 SSID 和密码都不为空
     *
     * @return true 如果两个字段都不为空，false 否则
     */
    fun isValid(): Boolean {
        return ssid.isNotBlank() && password.isNotBlank()
    }
}
