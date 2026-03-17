package com.mic.ble.server.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.kk.core.utils.KLog

private const val TAG = "WiFiConnector"

/**
 * WiFi 网络连接工具
 *
 * 负责将设备连接到指定的 WiFi 网络
 * 支持 Android 10+ 的新 API（NetworkRequest）和旧 API（WifiConfiguration）
 *
 * 两种连接方式：
 * 1. Android 10+：使用 WifiNetworkSpecifier + ConnectivityManager.requestNetwork()
 *    - 推荐方式，更安全、更可靠
 *    - 不需要启用 WIFI_STATE 权限
 *    - 支持隐藏网络
 *
 * 2. Android 9-：使用 WifiManager.addNetwork() + enableNetwork()
 *    - 旧方式，已废弃（deprecated）
 *    - 需要 CHANGE_WIFI_STATE 权限
 *    - 功能有限
 *
 * @param context Android 上下文
 */
class WiFiConnector(private val context: Context) {

    /**
     * WiFi 管理器
     * 用于访问 WiFi 相关操作（旧 API）
     */
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    /**
     * 连接管理器
     * 用于管理网络连接（新 API）
     */
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    /**
     * 连接到 WiFi 网络
     *
     * 根据 Android 版本自动选择合适的连接方式
     * 这是一个挂起函数，可以在协程中使用
     *
     * @param ssid WiFi 网络名称，例如 "MyNetwork"
     * @param password WiFi 网络密码，例如 "password123"
     * @return true 如果连接成功（或已排队），false 如果失败
     *
     * 使用示例：
     * ```
     * viewModelScope.launch {
     *     val success = wifiConnector.connectToNetwork(ssid, password)
     *     if (success) {
     *         Log.d(TAG, "WiFi 连接成功")
     *     } else {
     *         Log.e(TAG, "WiFi 连接失败")
     *     }
     * }
     * ```
     */
    suspend fun connectToNetwork(ssid: String, password: String): Boolean = withContext(Dispatchers.Default) {
        KLog.d(TAG, "connectToNetwork() - 连接到 WiFi，ssid=$ssid, passwordLength=${password.length}, sdkVersion=${Build.VERSION.SDK_INT}")
        return@withContext try {
            // 根据 Android 版本选择连接方式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+：使用新 API
                KLog.d(TAG, "connectToNetwork() - 使用 Android 10+ 新 API（NetworkRequest）连接")
                connectViaNetworkRequest(ssid, password)
            } else {
                // Android 9-：使用旧 API
                KLog.d(TAG, "connectToNetwork() - 使用 Android 9- 旧 API（WifiConfiguration）连接")
                connectViaWifiConfiguration(ssid, password)
            }
        } catch (e: Exception) {
            KLog.e(TAG, "connectToNetwork() - WiFi 连接异常，ssid=$ssid, 异常=${e.message}", e)
            false
        }
    }

    /**
     * 旧 API 连接方式（Android 9 及以下）
     *
     * 使用已废弃的 WifiManager API
     * 过程：
     * 1. 创建 WifiConfiguration 对象，包含网络信息
     * 2. 通过 WifiManager.addNetwork() 添加网络
     * 3. 通过 enableNetwork() 启用网络
     * 4. 通过 reconnect() 触发连接
     *
     * 注意：此方式不保证连接成功，只是启用网络
     *
     * @param ssid WiFi SSID
     * @param password WiFi 密码
     * @return true 如果网络已启用
     */
    @Suppress("DEPRECATION")
    private suspend fun connectViaWifiConfiguration(ssid: String, password: String): Boolean {
        return withContext(Dispatchers.Default) {
            KLog.d(TAG, "connectViaWifiConfiguration() - 使用旧 API 连接，ssid=$ssid")
            // 创建 WiFi 配置对象
            val config = WifiConfiguration().apply {
                SSID = "\"$ssid\""              // SSID 需要用引号括起
                preSharedKey = "\"$password\""  // 密码也需要用引号括起
                hiddenSSID = false              // 假设网络不是隐藏的
            }

            // 添加网络配置
            KLog.d(TAG, "connectViaWifiConfiguration() - 添加网络配置")
            val networkId = wifiManager?.addNetwork(config) ?: -1
            if (networkId < 0) {
                KLog.e(TAG, "connectViaWifiConfiguration() - 网络添加失败，networkId=$networkId")
                return@withContext false
            }
            KLog.d(TAG, "connectViaWifiConfiguration() - 网络添加成功，networkId=$networkId")

            // 启用网络
            KLog.d(TAG, "connectViaWifiConfiguration() - 启用网络，networkId=$networkId")
            val enabled = wifiManager?.enableNetwork(networkId, true) ?: false
            if (!enabled) {
                KLog.w(TAG, "connectViaWifiConfiguration() - 网络启用失败，networkId=$networkId")
            } else {
                KLog.d(TAG, "connectViaWifiConfiguration() - 网络启用成功")
            }

            // 尝试连接（这可能不会立即连接，只是启用网络）
            KLog.d(TAG, "connectViaWifiConfiguration() - 尝试重新连接")
            val connected = wifiManager?.reconnect() ?: false
            KLog.d(TAG, "connectViaWifiConfiguration() - 网络连接尝试结果，connected=$connected")

            true
        }
    }

    /**
     * 新 API 连接方式（Android 10+）
     *
     * 使用 WifiNetworkSpecifier + ConnectivityManager.requestNetwork()
     * 这是 Android 10+ 推荐的连接方式
     *
     * 过程：
     * 1. 创建 WifiNetworkSpecifier，指定 SSID 和密码
     * 2. 创建 NetworkRequest，包含 WiFi 传输类型
     * 3. 请求连接，系统会触发连接过程
     * 4. 通过回调监听连接结果
     * 5. 30 秒超时：如果 30 秒内未连接，视为失败
     *
     * @param ssid WiFi SSID
     * @param password WiFi 密码
     * @return true 如果网络可用，false 如果连接失败或超时
     */
    private suspend fun connectViaNetworkRequest(ssid: String, password: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                KLog.d(TAG, "connectViaNetworkRequest() - 使用新 API 连接，ssid=$ssid, passwordLength=${password.length}")
                // 步骤 1：创建网络说明符
                KLog.d(TAG, "connectViaNetworkRequest() - 创建 WifiNetworkSpecifier")
                val specifier = WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)                  // 设置网络名称
                    .setWpa2Passphrase(password)    // 设置 WPA2 密码
                    .build()

                // 步骤 2：创建网络请求
                KLog.d(TAG, "connectViaNetworkRequest() - 创建 NetworkRequest")
                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)  // 只要 WiFi
                    .setNetworkSpecifier(specifier)
                    .build()

                // 步骤 3：创建结果容器（用于接收异步回调结果）
                val future = CompletableFuture<Boolean>()

                // 步骤 4：创建网络回调处理器
                val callback = object : ConnectivityManager.NetworkCallback() {
                    /**
                     * 当网络可用时调用
                     * 表示已成功连接到 WiFi
                     */
                    override fun onAvailable(network: android.net.Network) {
                        KLog.i(TAG, "connectViaNetworkRequest() - 网络可用，WiFi 连接成功")
                        future.complete(true)
                    }

                    /**
                     * 当网络不可用时调用
                     * 表示连接失败或网络不存在
                     */
                    override fun onUnavailable() {
                        KLog.w(TAG, "connectViaNetworkRequest() - 网络不可用，连接失败")
                        future.complete(false)
                    }

                    /**
                     * 当网络丢失时调用
                     * 表示之前连接的网络被断开
                     */
                    override fun onLost(network: android.net.Network) {
                        KLog.d(TAG, "connectViaNetworkRequest() - 网络已丢失，连接中断")
                        future.complete(false)
                    }
                }

                // 步骤 5：请求连接（30 秒超时）
                KLog.d(TAG, "connectViaNetworkRequest() - 请求网络连接，超时=30000ms")
                connectivityManager?.requestNetwork(networkRequest, callback, 30_000)

                // 步骤 6：等待结果（最多 30 秒）
                return@withContext try {
                    KLog.d(TAG, "connectViaNetworkRequest() - 等待连接结果")
                    future.get(30_000, java.util.concurrent.TimeUnit.MILLISECONDS)
                } catch (e: Exception) {
                    KLog.e(TAG, "connectViaNetworkRequest() - 网络请求超时或异常，异常=${e.message}", e)
                    false
                }
            } catch (e: Exception) {
                KLog.e(TAG, "connectViaNetworkRequest() - WiFi 连接请求失败，异常=${e.message}", e)
                false
            }
        }
    }

    /**
     * 检查当前是否已连接到 WiFi
     *
     * @return true 如果已连接到 WiFi，false 否则
     */
    fun isConnected(): Boolean {
        KLog.d(TAG, "isConnected() - 检查 WiFi 连接状态")
        val activeNetwork = connectivityManager?.activeNetwork ?: run {
            KLog.d(TAG, "isConnected() - 未发现活跃网络")
            return false
        }
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork) ?: run {
            KLog.d(TAG, "isConnected() - 无法获取网络能力")
            return false
        }
        val isWifi = caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        KLog.d(TAG, "isConnected() - WiFi 连接状态，isConnected=$isWifi")
        return isWifi
    }
}
