package com.mic.ble.server

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.kk.core.utils.KLog
import com.mic.ble.server.advertiser.BleAdvertiser
import com.mic.ble.server.gatt.ProvisioningGattServer
import com.mic.ble.server.wifi.WiFiConnector

private const val TAG = "BleServerViewModel"

/**
 * BLE 配网服务器 ViewModel
 *
 * 这是 MVVM 架构中的视图模型层，负责管理服务器端的所有状态和业务逻辑
 * 主要功能：
 * 1. 启动 BLE 广告广播，使得客户端能发现此设备
 * 2. 启动 GATT 服务器，准备接收客户端的连接和数据
 * 3. 监听客户端的连接状态
 * 4. 接收客户端发送的 WiFi 凭证
 * 5. 使用凭证连接到 WiFi 网络
 * 6. 向客户端推送配网状态更新
 *
 * ViewModel 的优势：
 * - 生命周期感知：屏幕旋转时不会重置状态
 * - 线程安全：所有状态通过 StateFlow 进行
 * - 可测试：业务逻辑与 UI 分离
 * - 响应式：自动通知 UI 层状态变化
 *
 * @param application 应用上下文，用于获取系统服务
 */
class BleServerViewModel(application: Application) : AndroidViewModel(application) {

    // ==================== 系统服务初始化 ====================

    private val context: Context = application.applicationContext

    /**
     * 蓝牙管理器
     */
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    /**
     * 蓝牙适配器
     */
    private val bluetoothAdapter = bluetoothManager?.adapter

    /**
     * BLE 广告广播工具
     */
    private val advertiser = BleAdvertiser(bluetoothAdapter)

    /**
     * GATT 服务器实例
     * 与客户端进行 GATT 通信
     */
    private var gattServer: ProvisioningGattServer? = null

    /**
     * WiFi 连接工具
     * 用于连接到客户端发来的 WiFi 网络
     */
    private val wifiConnector = WiFiConnector(context)

    // ==================== UI 状态 (StateFlow) ====================

    /**
     * BLE 广告是否正在运行
     */
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    /**
     * 配网过程的文字状态描述
     * 例如："等待中...", "客户端已连接", "连接到 WiFi 中...", "成功" 等
     */
    private val _provisioningStatus = MutableStateFlow("等待中...")
    val provisioningStatus: StateFlow<String> = _provisioningStatus.asStateFlow()

    /**
     * 当前已连接的客户端数量
     * 通常应该是 0 或 1
     */
    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount.asStateFlow()

    // ==================== 公开方法 ====================

    /**
     * 启动配网过程
     *
     * 完整流程：
     * 1. 启动 BLE GATT 服务器，准备接收连接
     * 2. 启动 BLE 广告广播，让附近设备发现此设备
     * 3. 监听客户端连接
     * 4. 接收 WiFi 凭证
     * 5. 连接到 WiFi
     * 6. 推送配网结果
     */
    fun startProvisioning() {
        KLog.d(TAG, "startProvisioning() - 启动配网过程")
        // 防止重复启动
        if (_isAdvertising.value) {
            KLog.w(TAG, "startProvisioning() - 配网已在运行，防止重复启动")
            return
        }

        _isAdvertising.value = true
        _provisioningStatus.value = "服务器启动中..."
        KLog.d(TAG, "startProvisioning() - 设置广告状态为运行")

        viewModelScope.launch {
            try {
                // 步骤 1：创建并启动 GATT 服务器
                KLog.d(TAG, "startProvisioning() - 创建 GATT 服务器实例")
                gattServer = ProvisioningGattServer(context)

                gattServer?.startServer(context)?.collect { started ->
                    if (started) {
                        KLog.i(TAG, "startProvisioning() - GATT 服务器已启动")
                        _provisioningStatus.value = "服务器运行中，等待客户端..."

                        // 监听客户端连接状态
                        launch {
                            gattServer?.connectionStatus?.collect { devices ->
                                KLog.d(TAG, "startProvisioning() - 客户端连接状态变化，连接数=${devices.size}")
                                _connectionCount.value = devices.size
                                if (devices.isNotEmpty()) {
                                    KLog.d(TAG, "startProvisioning() - 设备已连接，deviceAddress=${devices.first().address}")
                                    _provisioningStatus.value = "客户端已连接，等待凭证..."
                                }
                            }
                        }

                        // 监听配网状态变化
                        launch {
                            gattServer?.provisioningStatus?.collect { status ->
                                KLog.d(TAG, "startProvisioning() - 配网状态变化，status=$status")
                                _provisioningStatus.value = status
                                when (status) {
                                    // 当收到凭证并进入连接 WiFi 状态时
                                    "CONNECTING_TO_WIFI" -> {
                                        KLog.d(TAG, "startProvisioning() - 准备连接到 WiFi...")
                                        connectToWiFi()  // 执行 WiFi 连接
                                    }
                                    else -> {
                                        KLog.d(TAG, "startProvisioning() - 其他配网状态，status=$status")
                                    }
                                }
                            }
                        }
                    }
                }

                // 步骤 2：启动 BLE 广告广播
                KLog.d(TAG, "startProvisioning() - 启动 BLE 广告广播")
                advertiser.startAdvertising().collect {
                    KLog.i(TAG, "startProvisioning() - BLE 广告已启动")
                }
            } catch (e: Exception) {
                KLog.e(TAG, "startProvisioning() - 启动配网失败，异常=${e.message}", e)
                _isAdvertising.value = false
                _provisioningStatus.value = "启动失败"
            }
        }
    }

    /**
     * 连接到 WiFi 网络
     *
     * 此方法由配网状态监听器自动调用
     * 使用从客户端收到的 SSID 和密码进行连接
     *
     * 过程：
     * 1. 获取 GATT 服务器存储的 SSID 和密码
     * 2. 调用 WiFiConnector 进行连接
     * 3. 根据结果更新状态
     * 4. 推送状态更新给客户端
     */
    private suspend fun connectToWiFi() {
        KLog.d(TAG, "connectToWiFi() - 准备连接到 WiFi")
        gattServer?.let { server ->
            // 从 GATT 服务器获取收到的凭证
            val ssid = server.getSSID()
            val password = server.getPassword()
            KLog.d(TAG, "connectToWiFi() - 获取凭证，ssid=$ssid, passwordLength=${password.length}")

            if (ssid.isNotEmpty() && password.isNotEmpty()) {
                _provisioningStatus.value = "正在连接到 WiFi：$ssid"
                KLog.d(TAG, "connectToWiFi() - 凭证有效，准备执行WiFi连接，ssid=$ssid")

                // 执行 WiFi 连接（异步操作）
                val success = wifiConnector.connectToNetwork(ssid, password)

                if (success) {
                    // 连接成功
                    _provisioningStatus.value = "WiFi 连接成功！"
                    server.updateStatus("SUCCESS")  // 推送成功状态给客户端
                    KLog.i(TAG, "connectToWiFi() - WiFi 连接成功！ssid=$ssid")
                } else {
                    // 连接失败
                    _provisioningStatus.value = "WiFi 连接失败"
                    server.updateStatus("FAILED")   // 推送失败状态给客户端
                    KLog.w(TAG, "connectToWiFi() - WiFi 连接失败，ssid=$ssid")
                }
            } else {
                KLog.w(TAG, "connectToWiFi() - 凭证不完整，ssid=${ssid.isNotEmpty()}, password=${password.isNotEmpty()}")
            }
        }
    }

    /**
     * 停止配网过程
     *
     * 清理资源：
     * 1. 停止 BLE 广告广播
     * 2. 关闭 GATT 服务器
     * 3. 重置所有状态
     */
    fun stopProvisioning() {
        KLog.d(TAG, "stopProvisioning() - 停止配网过程，释放资源")
        _isAdvertising.value = false
        gattServer?.close()  // 关闭 GATT 服务器
        gattServer = null
        _provisioningStatus.value = "已停止"
        _connectionCount.value = 0
        KLog.d(TAG, "stopProvisioning() - 配网已停止，所有资源已释放")
    }

    /**
     * ViewModel 生命周期：当 ViewModel 将被销毁时调用
     *
     * 确保清理所有资源，防止泄漏
     */
    override fun onCleared() {
        super.onCleared()
        KLog.d(TAG, "onCleared() - ViewModel 已清理")
        stopProvisioning()
    }
}
