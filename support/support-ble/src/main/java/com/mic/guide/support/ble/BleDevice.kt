package com.mic.guide.support.ble

/** 扫描到的 BLE 设备（纯数据）。 */
data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
)

/** 连接状态机。 */
enum class BleConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, SERVICES_DISCOVERED, FAILED
}

/** 某特征值收到的数据（通知或读取结果）。 */
data class BleData(
    val serviceUuid: String,
    val characteristicUuid: String,
    val value: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleData) return false
        return serviceUuid == other.serviceUuid &&
            characteristicUuid == other.characteristicUuid &&
            value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        var result = serviceUuid.hashCode()
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }
}
