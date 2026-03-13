package com.mic.ble.client

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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.mic.ble.client.databinding.ActivityBleClientBinding
import com.kk.core.utils.KLog
import com.mic.ble.client.model.WiFiCredentials
import com.mic.ble.client.model.ProvisioningStatus

private const val TAG = "BleClientActivity"

/**
 * BLE WiFi 配网客户端 Activity
 *
 * 这是应用的主界面，提供以下功能：
 * 1. 扫描周围的 BLE 配网设备
 * 2. 选择并连接到目标设备
 * 3. 输入 WiFi SSID 和密码
 * 4. 发送凭证给设备
 * 5. 显示配网过程状态和结果
 *
 * UI 架构：
 * - 使用 ViewBinding 进行视图绑定（类型安全）
 * - 使用 ViewModel 管理业务逻辑状态
 * - 使用 StateFlow 进行响应式 UI 更新
 */
class BleClientActivity : AppCompatActivity() {

    /**
     * 视图绑定对象
     * 提供对所有 UI 控件的类型安全访问
     */
    private lateinit var binding: ActivityBleClientBinding

    /**
     * ViewModel 实例
     * 使用 by viewModels() 委托自动创建和管理生命周期
     */
    private val viewModel: BleClientViewModel by viewModels()

    /**
     * 当前选中的设备
     * 用户点击设备列表时，此变量会被更新
     */
    private var selectedDevice: com.mic.ble.client.DeviceAdapter.DeviceItem? = null

    /**
     * 权限请求结果处理器
     * 用户对权限请求做出响应后，此回调被触发
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 检查所有权限是否都被授予
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startScan()  // 权限已授予，开始扫描
        } else {
            Toast.makeText(this, "需要蓝牙权限才能继续", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Activity 创建回调
     *
     * 初始化 UI 和开始监听 ViewModel 状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化视图绑定
        binding = ActivityBleClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置 UI 控件的点击监听器
        setupUI()

        // 订阅 ViewModel 的状态变化，自动更新 UI
        observeViewModel()
    }

    /**
     * 设置 UI 控件的交互逻辑
     *
     * 配置各个按钮的点击监听器和文本框的输入
     */
    private fun setupUI() {
        KLog.d(TAG, "setupUI() - 初始化 UI 控件交互逻辑")
        binding.apply {
            // 扫描按钮点击监听
            buttonScan.setOnClickListener {
                KLog.d(TAG, "setupUI() - 扫描按钮被点击")
                if (isPermissionGranted()) {
                    // 权限已授予，直接扫描
                    KLog.d(TAG, "setupUI() - 权限已授予，开始扫描")
                    startScan()
                } else {
                    // 权限未授予，先请求权限
                    KLog.d(TAG, "setupUI() - 权限未授予，请求权限")
                    requestPermissions()
                }
            }

            // 连接按钮点击监听
            buttonConnect.setOnClickListener {
                KLog.d(TAG, "setupUI() - 连接按钮被点击")
                selectedDevice?.device?.let { device ->
                    // 已选中设备，执行连接
                    KLog.d(TAG, "setupUI() - 已选中设备：${device.name} (${device.address})，执行连接")
                    viewModel.connectToDevice(device)
                } ?: run {
                    // 未选中设备，提示用户
                    KLog.w(TAG, "setupUI() - 未选中设备，显示提示信息")
                    Toast.makeText(this@BleClientActivity, "请先选择一个设备", Toast.LENGTH_SHORT).show()
                }
            }

            // 配网按钮点击监听
            buttonProvision.setOnClickListener {
                KLog.d(TAG, "setupUI() - 配网按钮被点击")
                // 从文本框获取 SSID 和密码
                val ssid = editSSID.text.toString()
                val password = editPassword.text.toString()

                if (ssid.isBlank() || password.isBlank()) {
                    // 凭证不完整，提示用户
                    KLog.w(TAG, "setupUI() - SSID 或密码为空，ssid=${ssid.isBlank()}, password=${password.isBlank()}")
                    Toast.makeText(this@BleClientActivity, "请输入 SSID 和密码", Toast.LENGTH_SHORT).show()
                } else {
                    // 凭证有效，执行配网
                    KLog.d(TAG, "setupUI() - 凭证有效，执行配网，ssid=$ssid, passwordLength=${password.length}")
                    viewModel.provision(WiFiCredentials(ssid, password))
                }
            }

            // 断开连接按钮点击监听
            buttonDisconnect.setOnClickListener {
                KLog.d(TAG, "setupUI() - 断开连接按钮被点击")
                viewModel.disconnect()
            }
        }
    }

    /**
     * 观察 ViewModel 的状态变化
     *
     * 使用 Flow 的 collect 方法订阅状态变化
     * 当 ViewModel 中的状态更新时，UI 会自动刷新
     */
    private fun observeViewModel() {
        KLog.d(TAG, "observeViewModel() - 订阅 ViewModel 状态变化")
        // 观察扫描到的设备列表
        lifecycleScope.launch {
            viewModel.scannedDevices.collect { devices ->
                // 将设备列表转换为适配器项目
                KLog.d(TAG, "observeViewModel() - 扫描设备列表更新，设备数=${devices.size}")
                val items = devices.map { device ->
                    com.mic.ble.client.DeviceAdapter.DeviceItem(
                        device = device,
                        onSelected = { selectedDevice = it }  // 保存选中的设备
                    )
                }
                // 更新 RecyclerView 显示设备列表
                binding.deviceList.adapter = com.mic.ble.client.DeviceAdapter(items)
                binding.deviceList.layoutManager = LinearLayoutManager(this@BleClientActivity)
            }
        }

        // 观察连接状态
        lifecycleScope.launch {
            viewModel.connectionStatus.collect { status ->
                // 更新连接状态文本
                KLog.d(TAG, "observeViewModel() - 连接状态变化，status=$status")
                binding.textConnectionStatus.text = "连接状态：$status"
            }
        }

        // 观察配网状态
        lifecycleScope.launch {
            viewModel.provisioningStatus.collect { status ->
                // 更新配网状态文本
                KLog.d(TAG, "observeViewModel() - 配网状态变化，status=$status")
                binding.textProvisioningStatus.text = "配网状态：$status"

                // 根据配网结果显示提示信息
                when (status) {
                    ProvisioningStatus.SUCCESS -> {
                        KLog.i(TAG, "observeViewModel() - 配网成功！")
                        Toast.makeText(this@BleClientActivity, "配网成功！", Toast.LENGTH_SHORT).show()
                    }
                    ProvisioningStatus.FAILED -> {
                        KLog.w(TAG, "observeViewModel() - 配网失败！")
                        Toast.makeText(this@BleClientActivity, "配网失败！", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        KLog.d(TAG, "observeViewModel() - 配网中间状态，status=$status")
                    }
                }
            }
        }
    }

    /**
     * 启动 BLE 设备扫描
     *
     * 过程：
     * 1. 禁用扫描按钮，防止重复扫描
     * 2. 调用 ViewModel 开始扫描
     * 3. 10 秒后自动停止扫描
     * 4. 恢复扫描按钮
     */
    private fun startScan() {
        KLog.d(TAG, "startScan() - 启动扫描过程，设置按钮状态")
        binding.buttonScan.text = "扫描中..."
        binding.buttonScan.isEnabled = false

        // 通知 ViewModel 开始扫描
        KLog.d(TAG, "startScan() - 调用 ViewModel.startScan()")
        viewModel.startScan()

        // 10 秒后自动停止扫描
        binding.root.postDelayed({
            KLog.d(TAG, "startScan() - 10秒扫描超时，停止扫描")
            viewModel.stopScan()
            binding.buttonScan.text = "扫描设备"
            binding.buttonScan.isEnabled = true
            KLog.d(TAG, "startScan() - 恢复按钮状态")
        }, 10_000)
    }

    /**
     * 检查是否已授予所需的蓝牙权限
     *
     * 权限要求因 Android 版本而异：
     * - Android 12+：需要 BLUETOOTH_SCAN 和 BLUETOOTH_CONNECT
     * - Android 11-：需要 BLUETOOTH 权限
     *
     * @return true 如果已授予所需权限，false 否则
     */
    private fun isPermissionGranted(): Boolean {
        val sdkVersion = Build.VERSION.SDK_INT
        KLog.d(TAG, "isPermissionGranted() - 检查权限，sdkVersion=$sdkVersion")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：检查新的细分权限
            val scanGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val connectGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            KLog.d(TAG, "isPermissionGranted() - Android 12+，BLUETOOTH_SCAN=$scanGranted, BLUETOOTH_CONNECT=$connectGranted")
            scanGranted && connectGranted
        } else {
            // Android 11-：检查旧权限
            val bluetoothGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
            KLog.d(TAG, "isPermissionGranted() - Android 11-，BLUETOOTH=$bluetoothGranted")
            bluetoothGranted
        }
    }

    /**
     * 请求蓝牙权限
     *
     * 根据 Android 版本请求相应的权限
     * 用户的响应会通过 permissionLauncher 回调处理
     */
    private fun requestPermissions() {
        val sdkVersion = Build.VERSION.SDK_INT
        KLog.d(TAG, "requestPermissions() - 请求权限，sdkVersion=$sdkVersion")
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+：请求新权限
            KLog.d(TAG, "requestPermissions() - 请求 Android 12+ 权限：BLUETOOTH_SCAN, BLUETOOTH_CONNECT, ACCESS_FINE_LOCATION")
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            // Android 11-：请求旧权限
            KLog.d(TAG, "requestPermissions() - 请求 Android 11- 权限：BLUETOOTH, ACCESS_FINE_LOCATION")
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }
}
