package com.mic.ble.client

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.support.v18.scanner.ScanResult
import com.kk.core.utils.KLog
import com.mic.ble.client.manager.ProvisioningBleManager
import com.mic.ble.client.model.WiFiCredentials
import com.mic.ble.client.model.ProvisioningStatus
import com.mic.ble.client.scanner.BleScanner

private const val TAG = "BleClientViewModel"

/**
 * 扫描到的设备信息
 */
data class ScannedDevice(
    val device: BluetoothDevice,
    val name: String,
    val rssi: Int
)

/**
 * BLE 配网客户端 ViewModel
 */
@SuppressLint("MissingPermission")
class BleClientViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter = bluetoothManager?.adapter

    private val bleScanner = BleScanner()

    private var provisioningManager: ProvisioningBleManager? = null

    private var scanJob: Job? = null

    // ==================== UI 状态 ====================

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _provisioningStatus = MutableStateFlow(ProvisioningStatus.IDLE)
    val provisioningStatus: StateFlow<ProvisioningStatus> = _provisioningStatus.asStateFlow()

    private val _connectionStatus = MutableStateFlow("未连接")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // ==================== 公开方法 ====================

    fun startScan() {
        if (_isScanning.value) return

        _isScanning.value = true
        _scannedDevices.value = emptyList()
        KLog.d(TAG, "开始扫描 BLE 设备...")

        scanJob = viewModelScope.launch {
            try {
                bleScanner.scanForDevices().collect { scanResult ->
                    val device = scanResult.device
                    // 优先使用 scanRecord 中的名称（更可靠），其次用 device.name
                    val deviceName = scanResult.scanRecord?.deviceName
                        ?: device.name
                        ?: "未知设备"

                    KLog.d(TAG, "扫描到配网设备：$deviceName (${device.address}), rssi=${scanResult.rssi}")

                    val currentDevices = _scannedDevices.value.toMutableList()
                    val existingIndex = currentDevices.indexOfFirst { it.device.address == device.address }
                    val scannedDevice = ScannedDevice(device, deviceName, scanResult.rssi)

                    if (existingIndex >= 0) {
                        // 更新已有设备的 RSSI
                        currentDevices[existingIndex] = scannedDevice
                    } else {
                        currentDevices.add(scannedDevice)
                        KLog.d(TAG, "新设备，当前共 ${currentDevices.size} 个")
                    }
                    _scannedDevices.value = currentDevices
                }
            } catch (e: Exception) {
                KLog.e(TAG, "扫描错误：${e.message}", e)
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun stopScan() {
        KLog.d(TAG, "停止扫描")
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    fun connectToDevice(device: BluetoothDevice) {
        KLog.d(TAG, "connectToDevice() - deviceAddress=${device.address}")
        _connectionStatus.value = "连接中..."

        provisioningManager = ProvisioningBleManager(context).apply {
            connect(device)
                .retry(3, 200)
                .timeout(15_000)
                .useAutoConnect(false)
                .enqueue()
        }

        viewModelScope.launch {
            try {
                provisioningManager?.getConnectionStateFlow()?.collect { state ->
                    val statusText = when (state) {
                        no.nordicsemi.android.ble.ktx.state.ConnectionState.Ready -> "已连接"
                        is no.nordicsemi.android.ble.ktx.state.ConnectionState.Connecting -> "连接中..."
                        is no.nordicsemi.android.ble.ktx.state.ConnectionState.Disconnecting -> "断开连接中..."
                        is no.nordicsemi.android.ble.ktx.state.ConnectionState.Disconnected -> "未连接"
                        else -> "未知"
                    }
                    _connectionStatus.value = statusText
                    KLog.d(TAG, "连接状态变化 - state=$state, statusText=$statusText")
                }
            } catch (e: Exception) {
                KLog.e(TAG, "连接状态获取错误：${e.message}", e)
                _connectionStatus.value = "未连接"
            }
        }
    }

    fun provision(credentials: WiFiCredentials) {
        KLog.d(TAG, "provision() - ssid=${credentials.ssid}")

        if (!credentials.isValid()) {
            KLog.w(TAG, "无效的凭证")
            return
        }

        _provisioningStatus.value = ProvisioningStatus.RECEIVING_CREDENTIALS

        viewModelScope.launch {
            try {
                provisioningManager?.let { manager ->
                    KLog.d(TAG, "发送 SSID")
                    manager.writeSSID(credentials.ssid).suspend()
                    KLog.d(TAG, "SSID 已发送")

                    KLog.d(TAG, "发送密码")
                    manager.writePassword(credentials.password).suspend()
                    KLog.d(TAG, "密码已发送")

                    _provisioningStatus.value = ProvisioningStatus.CONNECTING_TO_WIFI

                    manager.getStatusFlow().collect { status ->
                        when (status) {
                            "SUCCESS" -> {
                                KLog.i(TAG, "配网成功！")
                                _provisioningStatus.value = ProvisioningStatus.SUCCESS
                            }
                            "FAILED" -> {
                                KLog.w(TAG, "配网失败！")
                                _provisioningStatus.value = ProvisioningStatus.FAILED
                            }
                            else -> KLog.d(TAG, "配网状态：$status")
                        }
                    }
                }
            } catch (e: Exception) {
                KLog.e(TAG, "配网错误：${e.message}", e)
                _provisioningStatus.value = ProvisioningStatus.FAILED
            }
        }
    }

    fun disconnect() {
        provisioningManager?.disconnect()?.enqueue()
        _connectionStatus.value = "未连接"
        _provisioningStatus.value = ProvisioningStatus.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        KLog.d(TAG, "ViewModel 已清理")
        stopScan()
        disconnect()
    }
}
