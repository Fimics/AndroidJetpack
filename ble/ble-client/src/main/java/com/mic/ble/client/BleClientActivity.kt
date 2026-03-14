package com.mic.ble.client

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
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
 */
class BleClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBleClientBinding
    private val viewModel: BleClientViewModel by viewModels()
    private var selectedDevice: DeviceAdapter.DeviceItem? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startScan()
        } else {
            Toast.makeText(this, "需要蓝牙和位置权限才能扫描设备", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBleClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            buttonScan.setOnClickListener {
                if (!isPermissionGranted()) {
                    requestPermissions()
                    return@setOnClickListener
                }
                if (!isLocationEnabled()) {
                    Toast.makeText(this@BleClientActivity, "请打开位置服务（GPS），BLE 扫描需要位置服务", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                startScan()
            }

            buttonConnect.setOnClickListener {
                selectedDevice?.device?.let { device ->
                    viewModel.connectToDevice(device)
                } ?: Toast.makeText(this@BleClientActivity, "请先选择一个设备", Toast.LENGTH_SHORT).show()
            }

            buttonProvision.setOnClickListener {
                val ssid = editSSID.text.toString()
                val password = editPassword.text.toString()
                if (ssid.isBlank() || password.isBlank()) {
                    Toast.makeText(this@BleClientActivity, "请输入 WiFi 名称和密码", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.provision(WiFiCredentials(ssid, password))
                }
            }

            buttonDisconnect.setOnClickListener {
                viewModel.disconnect()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.scannedDevices.collect { devices ->
                KLog.d(TAG, "设备列表更新，设备数=${devices.size}")
                val items = devices.map { scannedDevice ->
                    DeviceAdapter.DeviceItem(
                        device = scannedDevice.device,
                        displayName = scannedDevice.name,
                        rssi = scannedDevice.rssi,
                        onSelected = { item ->
                            onDeviceSelected(item)
                        }
                    )
                }
                binding.deviceList.adapter = DeviceAdapter(items)
                binding.deviceList.layoutManager = LinearLayoutManager(this@BleClientActivity)
            }
        }

        lifecycleScope.launch {
            viewModel.connectionStatus.collect { status ->
                binding.textConnectionStatus.text = "连接状态：$status"
            }
        }

        lifecycleScope.launch {
            viewModel.provisioningStatus.collect { status ->
                binding.textProvisioningStatus.text = "配网状态：$status"
                when (status) {
                    ProvisioningStatus.SUCCESS ->
                        Toast.makeText(this@BleClientActivity, "配网成功！", Toast.LENGTH_SHORT).show()
                    ProvisioningStatus.FAILED ->
                        Toast.makeText(this@BleClientActivity, "配网失败！", Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
    }

    /**
     * 点击蓝牙设备后的处理：
     * 1. 选中设备
     * 2. 自动连接
     * 3. 自动获取手机当前 WiFi 名称填入输入框
     */
    private fun onDeviceSelected(item: DeviceAdapter.DeviceItem) {
        selectedDevice = item
        val deviceName = item.displayName
        KLog.d(TAG, "选中设备：$deviceName (${item.device.address})")
        Toast.makeText(this, "已选中：$deviceName", Toast.LENGTH_SHORT).show()

        // 自动连接
        viewModel.connectToDevice(item.device)

        // 自动填入当前手机连接的 WiFi 名称
        val currentSsid = getCurrentWifiSsid()
        if (!currentSsid.isNullOrBlank()) {
            binding.editSSID.setText(currentSsid)
            KLog.d(TAG, "自动填入 WiFi 名称：$currentSsid")
        }

        // 让密码框获取焦点，方便用户直接输入密码
        binding.editPassword.requestFocus()
    }

    /**
     * 获取手机当前连接的 WiFi 名称
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentWifiSsid(): String? {
        return try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            val ssid = wifiManager?.connectionInfo?.ssid
            // WifiManager 返回的 SSID 带引号，如 "\"MyWiFi\""，需要去掉
            ssid?.removeSurrounding("\"")?.takeIf { it != "<unknown ssid>" }
        } catch (e: Exception) {
            KLog.e(TAG, "获取 WiFi 名称失败：${e.message}")
            null
        }
    }

    private fun startScan() {
        binding.buttonScan.text = "扫描中..."
        binding.buttonScan.isEnabled = false

        viewModel.startScan()

        binding.root.postDelayed({
            viewModel.stopScan()
            binding.buttonScan.text = "扫描设备"
            binding.buttonScan.isEnabled = true
        }, 10_000)
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.BLUETOOTH) &&
                hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return true
        val locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }
}
