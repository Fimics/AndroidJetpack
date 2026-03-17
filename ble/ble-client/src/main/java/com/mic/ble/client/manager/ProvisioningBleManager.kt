package com.mic.ble.client.manager

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import com.kk.core.utils.KLog
import com.mic.ble.client.protocol.GattUUID
import java.nio.charset.StandardCharsets

private const val TAG = "ProvisioningBleManager"

/**
 * WiFi 配网 BLE 管理器
 *
 * 扩展 Nordic BleManager 以提供 WiFi 配网功能
 * 负责与服务器设备的 GATT 通信，包括：
 * 1. 连接到配网服务器
 * 2. 发现 GATT 服务和特征
 * 3. 写入 WiFi SSID 和密码
 * 4. 订阅并接收配网状态更新
 *
 * @param context Android 上下文
 */
class ProvisioningBleManager(context: Context) : BleManager(context) {

    // 缓存的 GATT 特征（在 isRequiredServiceSupported 中赋值）
    private var ssidCharacteristic: BluetoothGattCharacteristic? = null
    private var passwordCharacteristic: BluetoothGattCharacteristic? = null
    private var statusCharacteristic: BluetoothGattCharacteristic? = null

    /** 缓存的配网状态值 */
    private var statusValue: String = ""

    override fun getGattCallback() = object : BleManagerGattCallback() {

        override fun isRequiredServiceSupported(gatt: android.bluetooth.BluetoothGatt): Boolean {
            KLog.d(TAG, "isRequiredServiceSupported() - 检查配网服务")
            val service = gatt.getService(GattUUID.PROVISIONING_SERVICE) ?: run {
                KLog.w(TAG, "isRequiredServiceSupported() - 配网服务未找到")
                return false
            }

            ssidCharacteristic = service.getCharacteristic(GattUUID.SSID_CHARACTERISTIC)
            passwordCharacteristic = service.getCharacteristic(GattUUID.PASSWORD_CHARACTERISTIC)
            statusCharacteristic = service.getCharacteristic(GattUUID.STATUS_CHARACTERISTIC)

            if (ssidCharacteristic == null || passwordCharacteristic == null || statusCharacteristic == null) {
                KLog.w(TAG, "isRequiredServiceSupported() - 必需的特征未找到")
                return false
            }

            KLog.d(TAG, "isRequiredServiceSupported() - 配网服务验证通过")
            return true
        }

        override fun onServicesInvalidated() {
            KLog.d(TAG, "onServicesInvalidated() - 清除缓存的特征")
            ssidCharacteristic = null
            passwordCharacteristic = null
            statusCharacteristic = null
        }

        override fun initialize() {
            KLog.d(TAG, "initialize() - 初始化 BLE 连接")

            // 设置状态特征的通知回调
            setNotificationCallback(statusCharacteristic)
                .with { _, data ->
                    statusValue = data.getStringValue(0) ?: ""
                    KLog.d(TAG, "initialize() - 状态已接收，statusValue=$statusValue")
                }

            // 请求协商更大的 MTU
            requestMtu(512).enqueue()

            // 启用状态特征的通知
            enableNotifications(statusCharacteristic).enqueue()
        }
    }

    /**
     * 写入 WiFi SSID 到设备
     */
    fun writeSSID(ssid: String): WriteRequest {
        KLog.d(TAG, "writeSSID() - ssid=$ssid")
        val data = Data(ssid.toByteArray(StandardCharsets.UTF_8))
        return writeCharacteristic(
            ssidCharacteristic,
            data
        ).timeout(5_000)
    }

    /**
     * 写入 WiFi 密码到设备
     */
    fun writePassword(password: String): WriteRequest {
        KLog.d(TAG, "writePassword() - passwordLength=${password.length}")
        val data = Data(password.toByteArray(StandardCharsets.UTF_8))
        return writeCharacteristic(
            passwordCharacteristic,
            data
        ).timeout(5_000)
    }

    /**
     * 获取状态特征的数据流
     */
    fun getStatusFlow(): Flow<String> {
        KLog.d(TAG, "getStatusFlow()")
        val char = statusCharacteristic
            ?: throw IllegalStateException("状态特征未找到")

        return setNotificationCallback(char).asFlow().map { data ->
            data.getStringValue(0) ?: ""
        }
    }

    /**
     * 获取连接状态的数据流
     */
    fun getConnectionStateFlow() = stateAsFlow()
}
