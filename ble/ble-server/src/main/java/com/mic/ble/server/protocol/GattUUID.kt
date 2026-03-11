package com.mic.ble.server.protocol

import java.util.UUID

/**
 * GATT 配网服务定义（服务器端）
 *
 * 定义了蓝牙 GATT 服务、特征及描述符的 UUID
 * 这些 UUID 必须与客户端的 UUID 完全一致
 *
 * 此对象由服务器在初始化 GATT 服务时使用
 */
object GattUUID {
    /**
     * 配网主服务 UUID
     * 服务器会在此 UUID 下创建特征
     * 客户端通过扫描此 UUID 来发现设备
     */
    val PROVISIONING_SERVICE: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AB")

    /**
     * SSID 特征 UUID
     * 属性：WRITE
     * 客户端写入 WiFi 网络名称
     * 服务器在 onCharacteristicWriteRequest 中接收
     */
    val SSID_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AC")

    /**
     * 密码特征 UUID
     * 属性：WRITE
     * 客户端写入 WiFi 密码
     * 服务器在 onCharacteristicWriteRequest 中接收
     */
    val PASSWORD_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AD")

    /**
     * 状态特征 UUID
     * 属性：READ, NOTIFY
     * 服务器通过此特征向客户端发送配网状态
     * 当值变化时，通过 notifyCharacteristicChanged 向客户端发送通知
     */
    val STATUS_CHARACTERISTIC: UUID = UUID.fromString("12345678-1234-1234-1234-1234567890AE")

    /**
     * CCCD (Client Characteristic Configuration Descriptor) UUID
     * 标准 SIG 定义的描述符 UUID
     * 用于 NOTIFY/INDICATE 特征
     * 允许客户端订阅/取消订阅通知
     */
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}
