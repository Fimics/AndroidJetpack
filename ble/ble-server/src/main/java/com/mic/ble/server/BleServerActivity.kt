package com.mic.ble.server

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.kk.core.utils.KLog
import com.mic.ble.server.databinding.ActivityBleServerBinding

private const val TAG = "BleServerActivity"

/**
 * BLE WiFi 配网服务器 Activity
 *
 * 这是配网设备的主界面，提供以下功能：
 * 1. 启动 BLE 广告广播，让手机能发现此设备
 * 2. 启动 GATT 服务器，准备接收手机的连接
 * 3. 接收手机发来的 WiFi 凭证
 * 4. 使用凭证连接到 WiFi 网络
 * 5. 显示配网过程的实时状态
 *
 * UI 架构：
 * - 使用 ViewBinding 进行视图绑定
 * - 使用 ViewModel 管理所有业务逻辑
 * - 使用 StateFlow 进行响应式 UI 更新
 */
class BleServerActivity : AppCompatActivity() {

    /**
     * 视图绑定对象
     * 提供对所有 UI 控件的类型安全访问
     */
    private lateinit var binding: ActivityBleServerBinding

    /**
     * ViewModel 实例
     * 管理所有的 BLE 和 WiFi 配网逻辑
     */
    private val viewModel: BleServerViewModel by viewModels()

    /**
     * 权限请求结果处理器
     * 用户同意或拒绝权限请求时触发
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 检查所有权限是否都被授予
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // 权限已授予，开始配网服务
            viewModel.startProvisioning()
        } else {
            // 权限未完全授予，提示用户
            Toast.makeText(this, "需要蓝牙和 WiFi 权限才能继续", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Activity 创建回调
     *
     * 初始化视图、设置 UI 逻辑、订阅 ViewModel 状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化视图绑定
        binding = ActivityBleServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置按钮等 UI 控件的交互逻辑
        setupUI()

        // 订阅 ViewModel 状态，自动更新显示
        observeViewModel()
    }

    /**
     * 设置 UI 控件的交互逻辑
     *
     * 配置"开始"和"停止"按钮的点击监听器
     */
    private fun setupUI() {
        KLog.d(TAG, "setupUI() - 初始化 UI 控件交互逻辑")
        binding.apply {
            // "开始广播"按钮点击监听
            buttonStart.setOnClickListener {
                KLog.d(TAG, "setupUI() - 开始广播按钮被点击")
                if (isPermissionGranted()) {
                    // 权限已授予，直接启动配网
                    KLog.d(TAG, "setupUI() - 权限已授予，启动配网")
                    viewModel.startProvisioning()
                } else {
                    // 权限未授予，先请求权限
                    KLog.d(TAG, "setupUI() - 权限未授予，请求权限")
                    requestPermissions()
                }
            }

            // "停止广播"按钮点击监听
            buttonStop.setOnClickListener {
                KLog.d(TAG, "setupUI() - 停止广播按钮被点击")
                viewModel.stopProvisioning()
            }
        }
    }

    /**
     * 观察 ViewModel 的状态变化
     *
     * 订阅以下状态流：
     * - isAdvertising：广播是否活跃
     * - provisioningStatus：配网过程状态
     * - connectionCount：已连接的客户端数
     */
    private fun observeViewModel() {
        KLog.d(TAG, "observeViewModel() - 订阅 ViewModel 状态变化")
        // 观察广播状态
        lifecycleScope.launch {
            viewModel.isAdvertising.collect { isAdvertising ->
                KLog.d(TAG, "observeViewModel() - 广播状态变化，isAdvertising=$isAdvertising")
                // 根据广播状态更新按钮的启用状态
                binding.buttonStart.isEnabled = !isAdvertising
                binding.buttonStop.isEnabled = isAdvertising

                // 更新广播状态显示
                binding.textAdvertiseStatus.text = if (isAdvertising) {
                    "广播中：激活"
                } else {
                    "广播中：已停止"
                }
            }
        }

        // 观察配网状态文本
        lifecycleScope.launch {
            viewModel.provisioningStatus.collect { status ->
                // 更新状态显示文本
                KLog.d(TAG, "observeViewModel() - 配网状态变化，status=$status")
                binding.textProvisioningStatus.text = "状态：$status"
            }
        }

        // 观察已连接的客户端数量
        lifecycleScope.launch {
            viewModel.connectionCount.collect { count ->
                // 更新已连接的客户端数显示
                KLog.d(TAG, "observeViewModel() - 连接数变化，count=$count")
                binding.textConnectionCount.text = "已连接客户端：$count"
            }
        }
    }

    /**
     * 检查是否已授予所需的 BLE 和 WiFi 权限
     *
     * 所需权限包括：
     * - BLE 相关：BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT
     * - WiFi 相关：ACCESS_WIFI_STATE, CHANGE_WIFI_STATE
     *
     * 权限要求因 Android 版本而异：
     * - Android 12+：更细分的权限
     * - Android 11-：更宽泛的权限
     *
     * @return true 如果已授予所需权限，false 否则
     */
    private fun isPermissionGranted(): Boolean {
        val sdkVersion = Build.VERSION.SDK_INT
        KLog.d(TAG, "isPermissionGranted() - 检查权限，sdkVersion=$sdkVersion")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：检查细分的 BLE 和 WiFi 权限
            val advertiseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
            val connectGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val wifiStateGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
            val changeWifiGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
            KLog.d(TAG, "isPermissionGranted() - Android 12+，BLUETOOTH_ADVERTISE=$advertiseGranted, BLUETOOTH_CONNECT=$connectGranted, ACCESS_WIFI_STATE=$wifiStateGranted, CHANGE_WIFI_STATE=$changeWifiGranted")
            advertiseGranted && connectGranted && wifiStateGranted && changeWifiGranted
        } else {
            // Android 11-：检查旧权限
            val bluetoothGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            val wifiStateGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
            KLog.d(TAG, "isPermissionGranted() - Android 11-，BLUETOOTH=$bluetoothGranted, ACCESS_WIFI_STATE=$wifiStateGranted")
            bluetoothGranted && wifiStateGranted
        }
    }

    /**
     * 请求应用所需的所有权限
     *
     * 根据 Android 版本请求相应的权限列表
     * 用户的响应会通过 permissionLauncher 回调处理
     */
    private fun requestPermissions() {
        val sdkVersion = Build.VERSION.SDK_INT
        KLog.d(TAG, "requestPermissions() - 请求权限，sdkVersion=$sdkVersion")
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：请求新的细分权限
            KLog.d(TAG, "requestPermissions() - 请求 Android 12+ 权限")
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,    // 需要广播 BLE 信号
                Manifest.permission.BLUETOOTH_CONNECT,      // 需要接受 BLE 连接
                Manifest.permission.ACCESS_WIFI_STATE,      // 需要读取 WiFi 状态
                Manifest.permission.CHANGE_WIFI_STATE,      // 需要修改 WiFi 连接
                Manifest.permission.CHANGE_NETWORK_STATE,   // 需要修改网络状态
                Manifest.permission.ACCESS_FINE_LOCATION    // WiFi 连接时需要位置权限
            )
        } else {
            // Android 11-：请求旧权限
            KLog.d(TAG, "requestPermissions() - 请求 Android 11- 权限")
            arrayOf(
                Manifest.permission.BLUETOOTH,              // BLE 基本权限
                Manifest.permission.ACCESS_WIFI_STATE,      // WiFi 访问权限
                Manifest.permission.CHANGE_WIFI_STATE       // WiFi 修改权限
            )
        }
        permissionLauncher.launch(permissions)
    }
}
