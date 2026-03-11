package com.mic.ble.client

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kk.core.utils.KLog
import com.mic.ble.client.manager.ProvisioningBleManager
import com.mic.ble.client.model.WiFiCredentials
import com.mic.ble.client.model.ProvisioningStatus
import com.mic.ble.client.scanner.BleScanner

private const val TAG = "BleClientViewModel"

/**
 * BLE 配网客户端 ViewModel
 *
 * 这是 MVVM 架构中的视图模型层，负责管理 UI 的所有状态和业务逻辑
 * 包括：BLE 设备扫描、连接、WiFi 凭证发送、配网状态监听等
 *
 * ViewModel 的优势：
 * - 生命周期感知：比 Activity/Fragment 活得更久，屏幕旋转时不会重置状态
 * - 线程安全：所有状态通过 StateFlow 进行，支持并发访问
 * - 可测试：业务逻辑与 UI 分离，易于单元测试
 * - 响应式：基于 Flow/StateFlow，自动通知 UI 层状态变化
 *
 * @param application 应用上下文，用于获取系统服务
 */
class BleClientViewModel(application: Application) : AndroidViewModel(application) {

    // ==================== 系统服务初始化 ====================

    private val context: Context = application.applicationContext

    /**
     * BluetoothManager：系统蓝牙管理服务
     * 用于获取 BluetoothAdapter
     */
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    /**
     * BluetoothAdapter：与蓝牙硬件通信的接口
     * 用于执行扫描、连接等操作
     */
    private val bluetoothAdapter = bluetoothManager?.adapter

    /**
     * BLE 设备扫描工具
     * 负责扫描周围的 BLE 设备，过滤出支持配网的设备
     */
    private val bleScanner = BleScanner(bluetoothAdapter)

    /**
     * 配网 BLE 管理器
     * 负责与设备建立连接、发送凭证、接收状态
     * 使用 var 因为需要在连接到不同设备时替换
     */
    private var provisioningManager: ProvisioningBleManager? = null

    // ==================== UI 状态 (StateFlow) ====================
    // StateFlow 是响应式状态容器，UI 层可以订阅这些流以自动更新

    /**
     * 扫描到的 BLE 设备列表
     * 当扫描发现新设备时，自动更新此列表
     */
    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    /**
     * 是否正在扫描的标志
     * true 表示正在扫描中，false 表示扫描已停止
     */
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    /**
     * 配网过程的状态
     * 取值：IDLE, RECEIVING_CREDENTIALS, CONNECTING_TO_WIFI, SUCCESS, FAILED
     */
    private val _provisioningStatus = MutableStateFlow(ProvisioningStatus.IDLE)
    val provisioningStatus: StateFlow<ProvisioningStatus> = _provisioningStatus.asStateFlow()

    /**
     * BLE 连接状态
     * 显示人类可读的连接状态：未连接、正在连接、已连接等
     */
    private val _connectionStatus = MutableStateFlow("未连接")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // ==================== 公开方法 ====================

    /**
     * 开始扫描 BLE 设备
     *
     * 启动后台扫描任务，搜索周围的支持配网的 BLE 设备
     * 每发现一个新设备，就添加到 scannedDevices 列表中
     * 自动去重：不会添加重复的设备（通过 MAC 地址判断）
     *
     * 用法：
     * ```
     * viewModel.startScan()
     * // 订阅扫描结果
     * viewModel.scannedDevices.collect { devices ->
     *     updateDeviceList(devices)
     * }
     * ```
     */
    fun startScan() {
        // 防止重复扫描
        if (_isScanning.value) return

        _isScanning.value = true
        _scannedDevices.value = emptyList()  // 清空之前的扫描结果
        KLog.d(TAG, "开始扫描 BLE 设备...")

        // 在 ViewModel 的生命周期作用域内启动协程任务
        viewModelScope.launch {
            try {
                // 扫描设备的 Flow 会不断发送新发现的设备
                bleScanner.scanForProvisioningDevices().collect { scanResult ->
                    val device = scanResult.device
                    KLog.d(TAG, "扫描到设备：${device.name} (${device.address})")

                    // 将新设备添加到列表中（如果还不存在）
                    val currentDevices = _scannedDevices.value.toMutableList()
                    if (!currentDevices.any { it.address == device.address }) {
                        currentDevices.add(device)
                        _scannedDevices.value = currentDevices  // 触发 UI 更新
                        KLog.d(TAG, "设备列表更新，当前共 ${currentDevices.size} 个设备")
                    }
                }
            } catch (e: Exception) {
                KLog.e(TAG, "扫描错误：${e.message}", e)
                _isScanning.value = false
            }
        }
    }

    /**
     * 停止 BLE 扫描
     *
     * 立即停止扫描操作，节省功耗
     */
    fun stopScan() {
        _isScanning.value = false
        KLog.d(TAG, "扫描已停止")
    }

    /**
     * 连接到指定的 BLE 设备
     *
     * 选择扫描列表中的某个设备并尝试建立 BLE 连接
     * 连接建立后，会自动发现设备上的 GATT 服务并初始化
     *
     * 连接过程：
     * 1. 创建新的 ProvisioningBleManager 实例
     * 2. 调用 connect() 方法发起连接
     * 3. 设置重试和超时参数
     * 4. 监听连接状态变化，更新 UI
     *
     * @param device 要连接的 BluetoothDevice 对象
     *
     * 用法：
     * ```
     * val selectedDevice = scannedDevices.value.first()
     * viewModel.connectToDevice(selectedDevice)
     * ```
     */
    fun connectToDevice(device: BluetoothDevice) {
        KLog.d(TAG, "connectToDevice() 参数 - deviceName=${device.name}, deviceAddress=${device.address}, deviceType=${device.type}")
        _connectionStatus.value = "连接中..."

        // 创建一个新的 BLE 管理器用于这个连接
        provisioningManager = ProvisioningBleManager(context).apply {
            connect(device)
                .retry(3, 200)              // 连接失败时最多重试 3 次，每次间隔 200ms
                .timeout(15_000)            // 15 秒超时
                .useAutoConnect(false)      // 直接连接而不是后台自动重连
                .enqueue()                  // 加入请求队列执行
        }
        KLog.d(TAG, "已发起连接请求，设备：${device.name}")

        // 在后台监听连接状态变化
        viewModelScope.launch {
            try {
                provisioningManager?.getConnectionStateFlow()?.collect { state ->
                    // 将底层状态码转换为用户友好的文本
                    val statusText = when (state) {
                        BluetoothAdapter.STATE_CONNECTED -> "已连接"
                        BluetoothAdapter.STATE_CONNECTING -> "连接中..."
                        BluetoothAdapter.STATE_DISCONNECTED -> "未连接"
                        BluetoothAdapter.STATE_DISCONNECTING -> "断开连接中..."
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

    /**
     * 向设备发送 WiFi 凭证并启动配网
     *
     * 配网流程：
     * 1. 验证凭证的有效性（SSID 和密码都不为空）
     * 2. 写入 SSID 到设备
     * 3. 写入密码到设备
     * 4. 监听设备的配网状态响应
     * 5. 根据设备的状态更新 UI（成功/失败）
     *
     * 此过程是异步的，使用 Kotlin 协程的 suspend 函数
     * 可以像同步代码一样编写异步逻辑，提高可读性
     *
     * @param credentials WiFi 凭证对象，包含 SSID 和密码
     *
     * 用法：
     * ```
     * val credentials = WiFiCredentials(
     *     ssid = "MyNetwork",
     *     password = "password123"
     * )
     * viewModel.provision(credentials)
     * // 监听配网状态
     * viewModel.provisioningStatus.collect { status ->
     *     when(status) {
     *         SUCCESS -> showSuccessDialog()
     *         FAILED -> showErrorDialog()
     *     }
     * }
     * ```
     */
    fun provision(credentials: WiFiCredentials) {
        KLog.d(TAG, "provision() 参数 - ssid=${credentials.ssid}, passwordLength=${credentials.password.length}")

        // 验证凭证
        if (!credentials.isValid()) {
            KLog.w(TAG, "无效的凭证 - SSID 或密码为空")
            return
        }

        _provisioningStatus.value = ProvisioningStatus.RECEIVING_CREDENTIALS
        KLog.d(TAG, "配网开始，状态：${_provisioningStatus.value}")

        viewModelScope.launch {
            try {
                provisioningManager?.let { manager ->
                    // 步骤 1：发送 SSID
                    KLog.d(TAG, "准备发送 SSID：${credentials.ssid}")
                    manager.writeSSID(credentials.ssid).suspend()
                    KLog.d(TAG, "SSID 已发送")

                    // 步骤 2：发送密码
                    KLog.d(TAG, "准备发送密码，长度=${credentials.password.length}")
                    manager.writePassword(credentials.password).suspend()
                    KLog.d(TAG, "密码已发送")

                    // 步骤 3：等待设备的配网状态响应
                    _provisioningStatus.value = ProvisioningStatus.CONNECTING_TO_WIFI
                    KLog.d(TAG, "等待设备连接 WiFi，当前状态：${_provisioningStatus.value}")

                    // 持续监听服务器发送的状态通知
                    // 当状态为 SUCCESS 或 FAILED 时，配网过程完成
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
                            else -> {
                                KLog.d(TAG, "配网状态变化：$status")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                KLog.e(TAG, "配网错误：${e.message}", e)
                _provisioningStatus.value = ProvisioningStatus.FAILED
            }
        }
    }

    /**
     * 断开与设备的连接
     *
     * 清理 BLE 连接，释放资源
     * 重置所有状态为初始值
     */
    fun disconnect() {
        // 异步断开连接
        provisioningManager?.disconnect()?.enqueue()

        // 重置状态
        _connectionStatus.value = "未连接"
        _provisioningStatus.value = ProvisioningStatus.IDLE
    }

    /**
     * ViewModel 生命周期：当 ViewModel 将被销毁时调用
     *
     * 在此进行资源清理，确保 BLE 连接被关闭
     * viewModelScope 也会自动取消所有运行中的协程
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel 已清理")
        disconnect()
    }
}
