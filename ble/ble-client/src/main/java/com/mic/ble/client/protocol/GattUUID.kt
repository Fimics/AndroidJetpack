package com.mic.ble.client.protocol

import java.util.UUID

/**
 * GATT 配网服务定义
 *
 * 定义了蓝牙 GATT 服务、特征及描述符的 UUID
 * 这些 UUID 必须与服务器端的 UUID 一致以建立通信
 */
object GattUUID {
    /**
     * 配网主服务 UUID
     * 服务器会广播此服务，客户端通过此 UUID 进行服务发现
     */
    val PROVISIONING_SERVICE: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AB")

    /**
     * SSID 特征 UUID
     * 属性：WRITE
     * 用途：客户端写入 WiFi 网络名称到此特征
     */
    val SSID_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")

    /**
     * 密码特征 UUID
     * 属性：WRITE
     * 用途：客户端写入 WiFi 密码到此特征
     */
    val PASSWORD_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AD")

    /**
     * 状态特征 UUID
     * 属性：READ, NOTIFY
     * 用途：服务器通过此特征向客户端发送配网状态
     * 客户端可订阅此特征以接收实时状态更新通知
     */
    val STATUS_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AE")

    /**
     * CCCD (Client Characteristic Configuration Descriptor) UUID
     * 这是标准的 SIG UUID，用于客户端订阅/取消订阅通知
     * 对于所有带 NOTIFY/INDICATE 属性的特征，自动添加此描述符
     */
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
