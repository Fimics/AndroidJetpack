package com.mic.ble.client.manager

import android.content.Context
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.asFlow
import no.nordicsemi.android.ble.ktx.stateAsFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.response.OnDataReceived
import com.kk.core.utils.KLog
import com.mic.ble.client.protocol.GattUUID
import com.mic.ble.client.model.ProvisioningStatus
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
 * BleManager 提供的优势：
 * - 自动处理 GATT 连接状态管理
 * - 内置请求队列，保证操作串行执行
 * - 自动 MTU 协商和服务发现延迟
 * - 完善的错误处理和重试机制
 *
 * @param context Android 上下文
 */
class ProvisioningBleManager(context: Context) : BleManager(context) {

    /**
     * 缓存的配网状态值
     * 保存从服务器收到的最新状态字符串
     */
    private var statusValue: String = ""

    /**
     * 获取并配置 GATT 回调处理器
     *
     * 此方法由 BleManager 自动调用，用于设置 GATT 连接过程中的各种回调
     * 包括：服务发现完成、初始化、设备就绪等
     */
    override fun getGattCallback() = object : BleManagerGattCallback() {

        /**
         * 检查设备是否支持所需的 GATT 服务和特征
         *
         * 当 BLE 连接建立并发现了服务后，此方法被调用
         * 需要验证设备是否包含配网服务及其所有必需的特征
         *
         * @param gatt BluetoothGatt 对象，包含发现的所有服务
         * @return true 如果服务器支持配网，false 表示设备不兼容
         */
        override fun isRequiredServiceSupported(gatt: android.bluetooth.BluetoothGatt): Boolean {
            // 尝试获取配网服务
            KLog.d(TAG, "isRequiredServiceSupported() - 检查配网服务，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
            val service = gatt.getService(GattUUID.PROVISIONING_SERVICE)
            if (service == null) {
                KLog.w(TAG, "isRequiredServiceSupported() - 配网服务未找到，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
                return false
            }

            // 检查所有必需的特征
            KLog.d(TAG, "isRequiredServiceSupported() - 检查必需的特征")
            val ssidChar = service.getCharacteristic(GattUUID.SSID_CHARACTERISTIC)
            val passwordChar = service.getCharacteristic(GattUUID.PASSWORD_CHARACTERISTIC)
            val statusChar = service.getCharacteristic(GattUUID.STATUS_CHARACTERISTIC)

            if (ssidChar == null || passwordChar == null || statusChar == null) {
                KLog.w(TAG, "isRequiredServiceSupported() - 必需的特征未找到，ssidChar=${ssidChar != null}, passwordChar=${passwordChar != null}, statusChar=${statusChar != null}")
                return false
            }

            KLog.d(TAG, "isRequiredServiceSupported() - 配网服务验证通过，所有特征已找到")
            return true
        }

        /**
         * 初始化 BLE 连接
         *
         * 当设备就绪、服务验证通过后，此方法被调用
         * 执行必要的初始化操作：
         * 1. 设置状态特征的通知回调
         * 2. 协商 MTU 大小（从 23 字节增加到 512 字节）
         * 3. 启用状态特征的通知，以接收服务器推送的状态更新
         *
         * 这些操作通过 BleManager 的请求队列串行执行
         */
        override fun initialize() {
            KLog.d(TAG, "initialize() - 初始化 BLE 连接")
            // 步骤 1：为状态特征设置通知回调
            // 当服务器发送通知时，此回调会被触发
            KLog.d(TAG, "initialize() - 设置状态特征通知回调，characteristicUuid=${GattUUID.STATUS_CHARACTERISTIC}")
            setNotificationCallback(getCharacteristic(GattUUID.STATUS_CHARACTERISTIC))
                .with { device, data ->
                    // 将接收到的字节数据转换为字符串
                    statusValue = data.getStringValue(0, StandardCharsets.UTF_8) ?: ""
                    KLog.d(TAG, "initialize() - 状态已接收，statusValue=$statusValue, dataLength=${data.size()}")
                }

            // 步骤 2：请求协商更大的 MTU
            // 默认 MTU 是 23 字节，协商 512 字节可以提高数据传输效率
            // 重试 1 次，每次重试间隔 100ms
            KLog.d(TAG, "initialize() - 请求协商 MTU=512，默认MTU=23")
            requestMtu(512)
                .retry(1, 100)
                .enqueue()

            // 步骤 3：启用状态特征的通知
            // 这样服务器可以主动向客户端推送状态更新
            // 而不是客户端需要不断轮询查询状态
            KLog.d(TAG, "initialize() - 启用状态特征的通知")
            enableNotifications(getCharacteristic(GattUUID.STATUS_CHARACTERISTIC))
                .retry(1, 100)
                .enqueue()
        }

        /**
         * 辅助方法：获取指定 UUID 的特征
         *
         * @param uuid 特征的 UUID
         * @return 对应的 BluetoothGattCharacteristic，如果不存在则返回 null
         */
        private fun getCharacteristic(uuid: java.util.UUID) =
            gatt?.getService(GattUUID.PROVISIONING_SERVICE)?.getCharacteristic(uuid)
    }

    /**
     * 写入 WiFi SSID 到设备
     *
     * 向服务器的 SSID 特征写入网络名称
     * 此操作会进入 BleManager 的请求队列，等待前面的操作完成后执行
     *
     * @param ssid WiFi 网络名称，例如 "MyNetwork"
     * @return WriteRequest 对象，可以设置回调或进一步配置
     *
     * 使用示例：
     * ```
     * manager.writeSSID("MyNetwork")
     *     .done { Log.d(TAG, "SSID 已发送") }
     *     .fail { device, reason -> Log.e(TAG, "SSID 发送失败：$reason") }
     *     .enqueue()
     * ```
     */
    fun writeSSID(ssid: String): WriteRequest {
        // 将 SSID 字符串转换为 UTF-8 字节
        KLog.d(TAG, "writeSSID() - 准备写入 SSID，ssid=$ssid, length=${ssid.length}")
        val ssidData = Data(ssid.toByteArray(StandardCharsets.UTF_8))
        return writeCharacteristic(
            GattUUID.PROVISIONING_SERVICE,
            GattUUID.SSID_CHARACTERISTIC,
            ssidData
        )
            .retry(1, 100)  // 如果写入失败，重试 1 次
            .timeout(5_000)  // 5 秒超时
    }

    /**
     * 写入 WiFi 密码到设备
     *
     * 向服务器的密码特征写入网络密码
     * 此操作会进入 BleManager 的请求队列，等待前面的操作完成后执行
     *
     * @param password WiFi 网络密码，例如 "password123"
     * @return WriteRequest 对象，可以设置回调或进一步配置
     *
     * 使用示例：
     * ```
     * manager.writePassword("password123")
     *     .done { KLog.d(TAG, "密码已发送") }
     *     .fail { device, reason -> KLog.e(TAG, "密码发送失败：$reason") }
     *     .enqueue()
     * ```
     */
    fun writePassword(password: String): WriteRequest {
        // 将密码字符串转换为 UTF-8 字节
        KLog.d(TAG, "writePassword() - 准备写入密码，passwordLength=${password.length}")
        val passwordData = Data(password.toByteArray(StandardCharsets.UTF_8))
        return writeCharacteristic(
            GattUUID.PROVISIONING_SERVICE,
            GattUUID.PASSWORD_CHARACTERISTIC,
            passwordData
        )
            .retry(1, 100)  // 如果写入失败，重试 1 次
            .timeout(5_000)  // 5 秒超时
    }

    /**
     * 获取状态特征的数据流
     *
     * 返回一个 Flow，当服务器发送状态更新时，Flow 会发送新的数据
     * 这是一个热流，订阅者会接收到服务器的实时通知
     *
     * @return Flow<String> 包含服务器发送的状态字符串
     * @throws IllegalStateException 如果状态特征不存在
     *
     * 使用示例：
     * ```
     * viewModelScope.launch {
     *     manager.getStatusFlow().collect { status ->
     *         when(status) {
     *             "SUCCESS" -> updateUI(ProvisioningStatus.SUCCESS)
     *             "FAILED" -> updateUI(ProvisioningStatus.FAILED)
     *             else -> Log.d(TAG, "状态：$status")
     *         }
     *     }
     * }
     * ```
     */
    fun getStatusFlow(): Flow<String> {
        KLog.d(TAG, "getStatusFlow() - 获取状态流，characteristicUuid=${GattUUID.STATUS_CHARACTERISTIC}")
        val statusChar = gatt?.getService(GattUUID.PROVISIONING_SERVICE)
            ?.getCharacteristic(GattUUID.STATUS_CHARACTERISTIC)

        return statusChar?.let {
            // 将特征转换为 Flow
            // asFlow 是 Nordic ble-ktx 提供的扩展函数
            KLog.d(TAG, "getStatusFlow() - 状态特征已找到，转换为Flow")
            it.asFlow(this)
        } ?: run {
            KLog.e(TAG, "getStatusFlow() - 状态特征未找到，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
            throw IllegalStateException("状态特征未找到")
        }
    }

    /**
     * 获取连接状态的数据流
     *
     * 返回一个 Flow，表示 BLE 连接的状态变化
     * 状态值包括：
     * - BluetoothAdapter.STATE_DISCONNECTED (0)
     * - BluetoothAdapter.STATE_CONNECTING (1)
     * - BluetoothAdapter.STATE_CONNECTED (2)
     * - BluetoothAdapter.STATE_DISCONNECTING (3)
     *
     * @return Flow<Int> 包含连接状态码
     *
     * 使用示例：
     * ```
     * viewModelScope.launch {
     *     manager.getConnectionStateFlow().collect { state ->
     *         when(state) {
     *             BluetoothAdapter.STATE_CONNECTED -> KLog.d(TAG, "已连接")
     *             BluetoothAdapter.STATE_DISCONNECTED -> KLog.d(TAG, "已断开")
     *         }
     *     }
     * }
     * ```
     */
    fun getConnectionStateFlow(): Flow<Int> {
        KLog.d(TAG, "getConnectionStateFlow() - 获取连接状态流")
        return stateAsFlow()
    }

    /**
     * 写入特征的辅助方法
     *
     * 根据服务 UUID 和特征 UUID 找到对应的特征，并执行写入操作
     *
     * @param serviceUuid 服务的 UUID
     * @param characteristicUuid 特征的 UUID
     * @param data 要写入的数据
     * @return WriteRequest 对象
     * @throws IllegalArgumentException 如果特征不存在
     */
    private fun writeCharacteristic(
        serviceUuid: java.util.UUID,
        characteristicUuid: java.util.UUID,
        data: Data
    ): WriteRequest {
        KLog.d(TAG, "writeCharacteristic() - 写入特征，serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid, dataSize=${data.size()}")
        val characteristic = gatt?.getService(serviceUuid)
            ?.getCharacteristic(characteristicUuid)
            ?: run {
                KLog.e(TAG, "writeCharacteristic() - 特征未找到，serviceUuid=$serviceUuid, characteristicUuid=$characteristicUuid")
                throw IllegalArgumentException("特征未找到")
            }

        return write(characteristic, data)
    }
}
