package com.mic.ble.client.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.kk.core.utils.KLog
import com.mic.ble.client.protocol.GattUUID

private const val TAG = "BleScanner"

/**
 * BLE 设备扫描工具
 *
 * 负责扫描周围蓝牙设备，过滤出支持 WiFi 配网服务的设备
 * 使用 Flow 提供异步、响应式的扫描结果流
 *
 * @param bluetoothAdapter Android BluetoothAdapter，用于获取 BLE 扫描器
 */
@SuppressLint("MissingPermission")  // 在 Android 12+ 需要 BLUETOOTH_SCAN 权限
class BleScanner(private val bluetoothAdapter: BluetoothAdapter?) {

    /**
     * 获取 BLE 扫描器实例
     * 使用 lazy 延迟初始化，只在第一次使用时获取
     */
    private val scanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    /**
     * 扫描支持配网服务的 BLE 设备
     *
     * 此方法返回一个 Flow，每当发现新的支持配网服务的设备时，就发送一个 ScanResult
     * 扫描通过设备广播的服务 UUID 过滤，只返回包含 PROVISIONING_SERVICE 的设备
     *
     * 扫描过程：
     * 1. 创建扫描过滤器，指定只扫描包含 PROVISIONING_SERVICE UUID 的设备
     * 2. 设置扫描模式为低延迟（快速扫描但功耗较高）
     * 3. 启动扫描，每找到一个匹配设备就通过 Flow 发送
     * 4. 当 Flow 被关闭时（awaitClose），自动停止扫描
     *
     * @return Flow<ScanResult> 扫描结果流，每次发现新设备时发送一个结果
     *
     * @see ScanFilter 用于过滤特定服务 UUID 的设备
     * @see ScanSettings 配置扫描参数（模式、回调类型等）
     */
    fun scanForProvisioningDevices(): Flow<ScanResult> = callbackFlow {
        KLog.d(TAG, "scanForProvisioningDevices() - 启动设备扫描，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
        // 创建扫描过滤器，只扫描包含 PROVISIONING_SERVICE 的设备
        val scanFilters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(GattUUID.PROVISIONING_SERVICE))
                .build()
        )

        // 扫描设置
        // SCAN_MODE_LOW_LATENCY：快速扫描模式，大约 100ms 间隔
        // CALLBACK_TYPE_ALL_MATCHES：每次发现匹配的设备都回调一次
        KLog.d(TAG, "scanForProvisioningDevices() - 配置扫描参数，模式=LOW_LATENCY")
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        // 创建扫描回调处理器
        val scanCallback = object : ScanCallback() {
            /**
             * 当发现一个扫描结果时调用
             *
             * @param callbackType 回调类型
             * @param result 扫描结果，包含设备信息和信号强度
             */
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                // 将扫描结果通过 Flow 发送出去
                // trySendBlocking：同步发送，不会阻塞当前线程
                KLog.d(TAG, "scanForProvisioningDevices() - 发现设备，deviceName=${result.device.name}, deviceAddress=${result.device.address}, rssi=${result.rssi}")
                trySendBlocking(result)
            }

            /**
             * 当扫描失败时调用
             *
             * @param errorCode 错误代码，可能的值：
             *   - SCAN_FAILED_ALREADY_STARTED：扫描已在进行
             *   - SCAN_FAILED_APPLICATION_REGISTRATION_FAILED：应用注册失败
             *   - SCAN_FAILED_INTERNAL_ERROR：系统内部错误
             *   - SCAN_FAILED_FEATURE_UNSUPPORTED：设备不支持此扫描模式
             */
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                // 扫描失败时关闭 Flow 并抛出异常
                KLog.e(TAG, "scanForProvisioningDevices() - 扫描失败，errorCode=$errorCode")
                close(Exception("Scan failed with error code: $errorCode"))
            }
        }

        // 开始 BLE 扫描
        // 使用前面定义的过滤器和设置，以及扫描回调
        KLog.d(TAG, "scanForProvisioningDevices() - 启动 BLE 扫描")
        scanner?.startScan(scanFilters, scanSettings, scanCallback)

        // awaitClose：当 Flow 被取消或关闭时，执行此块
        // 确保停止 BLE 扫描，释放资源
        awaitClose {
            KLog.d(TAG, "scanForProvisioningDevices() - 停止扫描，释放资源")
            scanner?.stopScan(scanCallback)
        }
    }
}
