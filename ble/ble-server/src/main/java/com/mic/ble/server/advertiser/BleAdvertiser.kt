package com.mic.ble.server.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.kk.core.utils.KLog
import com.mic.ble.server.protocol.GattUUID

private const val TAG = "BleAdvertiser"

/**
 * BLE 广播工具
 *
 * 负责启动和管理 BLE 广告广播
 * 使得附近的设备（如手机）可以发现并连接到此设备
 *
 * 广播过程：
 * 1. 创建广告数据，包含设备名称和配网服务 UUID
 * 2. 配置广告设置（功率、模式等）
 * 3. 启动广告广播
 * 4. 监听广告状态，失败时通知
 *
 * @param bluetoothAdapter Android BluetoothAdapter，用于获取广告器
 */
@SuppressLint("MissingPermission")  // 在 Android 12+ 需要 BLUETOOTH_ADVERTISE 权限
class BleAdvertiser(private val bluetoothAdapter: BluetoothAdapter?) {

    /**
     * BLE 广告器实例
     * 负责实际的广告广播操作
     * 使用 lazy 延迟初始化
     */
    private val advertiser: BluetoothLeAdvertiser? by lazy {
        bluetoothAdapter?.bluetoothLeAdvertiser
    }

    /**
     * 启动 BLE 广告广播
     *
     * 此方法返回一个 Flow，表示广告广播的启动过程
     *
     * 广告数据设置：
     * - 包含设备名称（客户端可见）
     * - 包含配网服务 UUID（用于服务发现过滤）
     * - 扫描响应数据也包含服务 UUID（确保被发现）
     *
     * 广告参数：
     * - ADVERTISE_MODE_LOW_LATENCY：快速广告模式，100ms 间隔，功耗较高
     * - TX_POWER_HIGH：最高发射功率，使范围最大化
     * - Connectable：广告允许客户端连接
     * - Timeout：0 表示永不超时
     *
     * @return Flow<AdvertiseState> 广告启动状态流
     *         - AdvertiseState.Started：广告启动成功
     *         - 异常：广告启动失败
     *
     * 使用示例：
     * ```
     * viewModelScope.launch {
     *     advertiser.startAdvertising().collect { state ->
     *         when(state) {
     *             AdvertiseState.Started -> Log.d(TAG, "广告启动成功")
     *         }
     *     }
     * }
     * ```
     */
    fun startAdvertising(): Flow<AdvertiseState> = callbackFlow {
        KLog.d(TAG, "startAdvertising() - 启动 BLE 广告广播，serviceUuid=${GattUUID.PROVISIONING_SERVICE}")
        // 步骤 1：配置广告设置
        KLog.d(TAG, "startAdvertising() - 配置广告参数，模式=LOW_LATENCY, 功率=HIGH, 可连接=true")
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)  // 快速广告
            .setTxPowerLevel(AdvertiseSettings.TX_POWER_HIGH)                // 最大功率
            .setConnectable(true)                                             // 允许连接
            .setTimeout(0)                                                    // 无超时
            .build()

        // 步骤 2：创建主广告数据
        // 此数据被发送到扫描设备，包含设备名称和服务 UUID
        KLog.d(TAG, "startAdvertising() - 创建广告数据，包含设备名和服务UUID")
        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)                                      // 包含设备名
            .addServiceUuid(ParcelUuid(GattUUID.PROVISIONING_SERVICE))       // 包含配网服务 UUID
            .build()

        // 步骤 3：创建扫描响应数据
        // 当扫描设备请求更多信息时，此数据被返回
        // 也包含服务 UUID，确保被发现
        KLog.d(TAG, "startAdvertising() - 创建扫描响应数据")
        val scanResponseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(GattUUID.PROVISIONING_SERVICE))
            .build()

        // 步骤 4：创建广告回调处理器
        val advertiseCallback = object : AdvertiseCallback() {
            /**
             * 广告启动成功时调用
             *
             * @param settingsInEffect 实际生效的广告设置
             *                         (可能与请求的设置不同)
             */
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                KLog.i(TAG, "startAdvertising() - 广告启动成功，模式=${settingsInEffect.advertiseMode}, 功率=${settingsInEffect.txPowerLevel}")
                // 通过 Flow 发送成功状态
                trySendBlocking(AdvertiseState.Started)
            }

            /**
             * 广告启动失败时调用
             *
             * @param errorCode 错误代码，可能的值：
             *   - ADVERTISE_FAILED_DATA_TOO_LARGE：广告数据过大（31 字节限制）
             *   - ADVERTISE_FAILED_TOO_MANY_ADVERTISERS：过多活动广告
             *   - ADVERTISE_FAILED_ALREADY_STARTED：此回调已有广告运行
             *   - ADVERTISE_FAILED_INTERNAL_ERROR：系统内部错误
             */
            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                // 将错误码转换为可读的错误信息
                val errorMsg = when (errorCode) {
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "广告数据过大"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "过多活动广告"
                    ADVERTISE_FAILED_ALREADY_STARTED -> "广告已在运行"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "系统内部错误"
                    else -> "未知错误"
                }
                KLog.e(TAG, "startAdvertising() - 广告启动失败，errorCode=$errorCode, errorMsg=$errorMsg")
                // 关闭 Flow 并抛出异常，向上游报告失败
                close(Exception("广告启动失败：$errorMsg"))
            }
        }

        // 步骤 5：启动实际的 BLE 广告
        KLog.d(TAG, "startAdvertising() - 启动 BLE 广告")
        advertiser?.startAdvertising(settings, advertiseData, scanResponseData, advertiseCallback)

        // awaitClose：当 Flow 被取消或关闭时执行
        // 确保停止 BLE 广告，释放资源
        awaitClose {
            KLog.d(TAG, "startAdvertising() - 停止广告，释放资源")
            advertiser?.stopAdvertising(advertiseCallback)
            KLog.d(TAG, "startAdvertising() - 广告已停止")
        }
    }

    /**
     * 广告状态枚举
     */
    sealed class AdvertiseState {
        /**
         * 表示 BLE 广告已成功启动
         * 此时周围的设备应该能发现此设备
         */
        object Started : AdvertiseState()
    }
}
