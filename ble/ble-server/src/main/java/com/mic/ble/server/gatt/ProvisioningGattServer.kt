package com.mic.ble.server.gatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import com.kk.core.utils.KLog
import com.mic.ble.server.protocol.GattUUID
import java.nio.charset.StandardCharsets

private const val TAG = "ProvisioningGattServer"

/**
 * WiFi 配网 GATT 服务器实现
 *
 * 这是设备端的 GATT 服务器，负责：
 * 1. 定义并添加 WiFi 配网服务和特征
 * 2. 接收客户端写入的 SSID 和密码
 * 3. 向客户端推送配网状态更新
 * 4. 管理客户端连接状态
 *
 * GATT 服务器工作原理：
 * - 创建 BluetoothGattServer 实例
 * - 定义服务和特征
 * - 注册回调处理客户端请求
 * - 在回调中处理读、写、连接等事件
 *
 * @param context Android 上下文，用于获取系统服务
 */
@SuppressLint("MissingPermission")  // 在 Android 12+ 需要 BLUETOOTH_ADVERTISE 等权限
class ProvisioningGattServer(context: Context) {

    /**
     * 蓝牙管理器
     */
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    /**
     * 蓝牙适配器
     */
    private val bluetoothAdapter = bluetoothManager?.adapter

    /**
     * GATT 服务器实例
     * 负责处理客户端连接和 GATT 操作
     */
    private var gattServer: BluetoothGattServer? = null

    /**
     * 已连接的客户端设备集合
     * StateFlow 用于响应式监听连接状态变化
     */
    private val _connectionStatus = MutableStateFlow<Set<BluetoothDevice>>(emptySet())
    val connectionStatus: StateFlow<Set<BluetoothDevice>> = _connectionStatus.asStateFlow()

    /**
     * 配网过程的状态字符串
     * 可能的值：IDLE, RECEIVING_CREDENTIALS, CONNECTING_TO_WIFI, SUCCESS, FAILED
     */
    private val _provisioningStatus = MutableStateFlow<String>("IDLE")
    val provisioningStatus: StateFlow<String> = _provisioningStatus.asStateFlow()

    /**
     * 从客户端接收的 WiFi SSID
     */
    private var ssidValue: String = ""

    /**
     * 从客户端接收的 WiFi 密码
     */
    private var passwordValue: String = ""

    /**
     * 启动 GATT 服务器
     *
     * 此方法执行以下步骤：
     * 1. 创建 GATT 服务器实例
     * 2. 定义配网服务和三个特征
     * 3. 为状态特征添加 CCCD 描述符（用于通知）
     * 4. 添加服务到 GATT 服务器
     * 5. 返回一个 Flow 表示启动状态
     *
     * @param context Android 上下文
     * @return Flow<Boolean> 表示服务器启动结果的流
     *         - true：服务器启动成功
     *         - 异常：服务器启动失败
     */
    fun startServer(context: Context): Flow<Boolean> = callbackFlow {
        KLog.d(TAG, "startServer() - 启动 GATT 服务器")
        // ==================== 步骤 1：创建 GATT 服务器回调处理器 ====================

        val gattCallback = object : BluetoothGattServerCallback() {

            /**
             * 当服务被添加到 GATT 服务器时调用
             *
             * @param status 操作状态（0 表示成功）
             * @param service 被添加的服务
             */
            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                KLog.d(TAG, "onServiceAdded() - 服务已添加，serviceUuid=${service.uuid}, status=$status")
            }

            /**
             * 当客户端请求读取特征值时调用
             *
             * 此处理器：
             * - 处理状态特征的读请求，返回当前配网状态
             * - 拒绝其他特征的读请求
             *
             * @param device 请求读取的客户端设备
             * @param requestId 请求 ID，用于响应
             * @param offset 读取偏移量
             * @param characteristic 要读取的特征
             */
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic
            ) {
                KLog.d(TAG, "onCharacteristicReadRequest() - 读请求，characteristicUuid=${characteristic.uuid}, deviceAddress=${device.address}, offset=$offset")
                when (characteristic.uuid) {
                    GattUUID.STATUS_CHARACTERISTIC -> {
                        // 状态特征：返回当前状态
                        KLog.d(TAG, "onCharacteristicReadRequest() - 状态特征读请求，返回状态=${_provisioningStatus.value}")
                        gattServer?.sendResponse(
                            device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                            _provisioningStatus.value.toByteArray(StandardCharsets.UTF_8)
                        )
                    }
                    else -> {
                        // 其他特征：拒绝读取（不可读）
                        KLog.w(TAG, "onCharacteristicReadRequest() - 拒绝读取，特征不支持读操作，characteristicUuid=${characteristic.uuid}")
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_READ_NOT_PERMITTED, offset, null)
                    }
                }
            }

            /**
             * 当客户端写入特征值时调用
             *
             * 此处理器：
             * - 处理 SSID 特征的写请求，保存网络名称
             * - 处理密码特征的写请求，保存网络密码
             * - 更新配网状态
             *
             * @param device 执行写入的客户端设备
             * @param requestId 请求 ID，用于响应
             * @param characteristic 被写入的特征
             * @param preparedWrite 是否是 Reliable Write 的一部分
             * @param responseNeeded 客户端是否需要响应
             * @param offset 写入偏移量
             * @param value 写入的数据
             */
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                KLog.d(TAG, "onCharacteristicWriteRequest() - 写请求，characteristicUuid=${characteristic.uuid}, deviceAddress=${device.address}, dataLength=${value.size}, offset=$offset")
                when (characteristic.uuid) {
                    GattUUID.SSID_CHARACTERISTIC -> {
                        // 接收 SSID
                        ssidValue = String(value, StandardCharsets.UTF_8)
                        KLog.d(TAG, "onCharacteristicWriteRequest() - SSID 已接收，ssid=$ssidValue")
                        _provisioningStatus.value = "RECEIVING_CREDENTIALS"
                    }
                    GattUUID.PASSWORD_CHARACTERISTIC -> {
                        // 接收密码
                        passwordValue = String(value, StandardCharsets.UTF_8)
                        KLog.d(TAG, "onCharacteristicWriteRequest() - 密码已接收，passwordLength=${passwordValue.length}")
                        _provisioningStatus.value = "CONNECTING_TO_WIFI"
                    }
                    else -> {
                        KLog.w(TAG, "onCharacteristicWriteRequest() - 未知的特征写请求，characteristicUuid=${characteristic.uuid}")
                    }
                }

                // 如果客户端需要响应，发送成功响应
                if (responseNeeded) {
                    KLog.d(TAG, "onCharacteristicWriteRequest() - 发送响应，status=GATT_SUCCESS")
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                }
            }

            /**
             * 当客户端读取描述符时调用
             *
             * 主要用于处理 CCCD 读取
             *
             * @param device 请求读取的客户端
             * @param requestId 请求 ID
             * @param offset 读取偏移量
             * @param descriptor 要读取的描述符
             */
            override fun onDescriptorReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor
            ) {
                KLog.d(TAG, "onDescriptorReadRequest() - 描述符读请求，descriptorUuid=${descriptor.uuid}, deviceAddress=${device.address}, offset=$offset")
                // 返回 CCCD 默认值（0x0000 = 通知禁用）
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, byteArrayOf(0, 0))
            }

            /**
             * 当客户端写入描述符时调用
             *
             * 主要用于处理 CCCD 写入（客户端启用/禁用通知）
             *
             * @param device 执行写入的客户端
             * @param requestId 请求 ID
             * @param descriptor 被写入的描述符
             * @param preparedWrite 是否是 Reliable Write 的一部分
             * @param responseNeeded 客户端是否需要响应
             * @param offset 写入偏移量
             * @param value 写入的数据（对于 CCCD，0x0001 = 启用通知）
             */
            override fun onDescriptorWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                KLog.d(TAG, "onDescriptorWriteRequest() - 描述符写请求，descriptorUuid=${descriptor.uuid}, deviceAddress=${device.address}, dataLength=${value.size}, offset=$offset")
                if (responseNeeded) {
                    KLog.d(TAG, "onDescriptorWriteRequest() - 发送响应，status=GATT_SUCCESS")
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                }
            }

            /**
             * 当客户端连接状态变化时调用
             *
             * 用于追踪已连接的客户端列表
             *
             * @param device 状态变化的客户端设备
             * @param status 操作状态
             * @param newState 新的连接状态：
             *   - STATE_CONNECTED (2)：已连接
             *   - STATE_DISCONNECTED (0)：已断开
             */
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                KLog.d(TAG, "onConnectionStateChange() - 连接状态变化，newState=$newState，status=$status，deviceAddress=${device.address}")
                val connectedDevices = _connectionStatus.value.toMutableSet()

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // 客户端已连接
                    connectedDevices.add(device)
                    KLog.i(TAG, "onConnectionStateChange() - 设备已连接，deviceAddress=${device.address}")
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // 客户端已断开
                    connectedDevices.remove(device)
                    KLog.d(TAG, "onConnectionStateChange() - 设备已断开，deviceAddress=${device.address}")
                }

                _connectionStatus.value = connectedDevices
            }
        }

        // ==================== 步骤 2：创建 GATT 服务器 ====================

        KLog.d(TAG, "startServer() - 创建 GATT 服务器实例")
        gattServer = bluetoothManager?.openGattServer(context, gattCallback)
        if (gattServer == null) {
            // GATT 服务器创建失败
            KLog.e(TAG, "startServer() - GATT 服务器创建失败")
            close(Exception("无法打开 GATT 服务器"))
            return@callbackFlow
        }
        KLog.d(TAG, "startServer() - GATT 服务器创建成功")

        // ==================== 步骤 3：创建配网服务 ====================

        KLog.d(TAG, "startServer() - 创建配网服务，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
        val provisioningService = BluetoothGattService(
            GattUUID.PROVISIONING_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY  // 主服务
        )

        // ==================== 步骤 4：创建 SSID 特征 ====================

        val ssidCharacteristic = BluetoothGattCharacteristic(
            GattUUID.SSID_CHARACTERISTIC,
            // 属性：支持 WRITE 和 WRITE_NO_RESPONSE
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            // 权限：允许写入
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // ==================== 步骤 5：创建密码特征 ====================

        val passwordCharacteristic = BluetoothGattCharacteristic(
            GattUUID.PASSWORD_CHARACTERISTIC,
            // 属性：支持 WRITE 和 WRITE_NO_RESPONSE
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            // 权限：允许写入
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // ==================== 步骤 6：创建状态特征 ====================

        val statusCharacteristic = BluetoothGattCharacteristic(
            GattUUID.STATUS_CHARACTERISTIC,
            // 属性：支持 READ 和 NOTIFY
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            // 权限：允许读取
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // ==================== 步骤 7：为状态特征添加 CCCD ====================

        // CCCD (Client Characteristic Configuration Descriptor)
        // 允许客户端启用/禁用通知
        val cccd = BluetoothGattDescriptor(
            GattUUID.CCCD,
            // 权限：允许读和写
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        statusCharacteristic.addDescriptor(cccd)

        // ==================== 步骤 8：将特征添加到服务 ====================

        provisioningService.addCharacteristic(ssidCharacteristic)
        provisioningService.addCharacteristic(passwordCharacteristic)
        provisioningService.addCharacteristic(statusCharacteristic)

        // ==================== 步骤 9：将服务添加到 GATT 服务器 ====================

        KLog.d(TAG, "startServer() - 添加服务到 GATT 服务器")
        gattServer?.addService(provisioningService)
        KLog.i(TAG, "startServer() - 配网服务已添加，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")

        // 表示服务器启动成功
        KLog.d(TAG, "startServer() - GATT 服务器启动成功")
        trySendBlocking(true)

        // ==================== 清理 ====================

        awaitClose {
            KLog.d(TAG, "startServer() - GATT 服务器正在关闭，释放资源")
            gattServer?.close()
            KLog.d(TAG, "startServer() - GATT 服务器已关闭")
        }
    }

    /**
     * 获取从客户端接收的 WiFi SSID
     *
     * @return 网络名称字符串
     */
    fun getSSID(): String = ssidValue

    /**
     * 获取从客户端接收的 WiFi 密码
     *
     * @return 网络密码字符串
     */
    fun getPassword(): String = passwordValue

    /**
     * 更新配网状态，并向所有已连接的客户端推送通知
     *
     * 此方法：
     * 1. 更新内部状态值
     * 2. 更新状态特征的值
     * 3. 向所有已连接的客户端发送通知
     *
     * @param status 新的配网状态字符串
     *              建议值：SUCCESS, FAILED, IDLE 等
     */
    fun updateStatus(status: String) {
        KLog.d(TAG, "updateStatus() - 更新配网状态，status=$status, connectedDeviceCount=${_connectionStatus.value.size}")
        // 更新内部状态
        _provisioningStatus.value = status

        // 获取状态特征
        val statusChar = gattServer?.getService(GattUUID.PROVISIONING_SERVICE)
            ?.getCharacteristic(GattUUID.STATUS_CHARACTERISTIC)

        // 更新特征值
        statusChar?.value = status.toByteArray(StandardCharsets.UTF_8)

        // 向所有已连接的客户端发送通知
        KLog.d(TAG, "updateStatus() - 向${_connectionStatus.value.size}个客户端发送通知")
        _connectionStatus.value.forEach { device ->
            KLog.d(TAG, "updateStatus() - 推送通知给客户端，deviceAddress=${device.address}")
            gattServer?.notifyCharacteristicChanged(device, statusChar, false)
        }
    }

    /**
     * 关闭 GATT 服务器，释放资源
     */
    fun close() {
        gattServer?.close()
        gattServer = null
    }
}
