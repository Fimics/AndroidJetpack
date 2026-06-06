package com.mic.guide.support.ble

import android.annotation.SuppressLint
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

/**
 * BLE 扫描门面：用 Nordic [BluetoothLeScannerCompat] 抹平各 Android 版本扫描差异，
 * 把扫描结果包成冷 [Flow]——下游开始收集即开扫，取消即停扫（[awaitClose]）。
 *
 * 调用方需先申请 `BLUETOOTH_SCAN`（API31+）或 `ACCESS_FINE_LOCATION`（API30-）权限。
 */
class BleScanner {

    /**
     * 开始扫描，逐个发现的设备发到下游。
     * @param lowLatency true 用低延迟高功耗模式（前台主动扫描），false 用均衡模式。
     */
    @SuppressLint("MissingPermission")
    fun scan(lowLatency: Boolean = true): Flow<BleDevice> = callbackFlow {
        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder()
            .setScanMode(
                if (lowLatency) ScanSettings.SCAN_MODE_LOW_LATENCY
                else ScanSettings.SCAN_MODE_BALANCED,
            )
            .setReportDelay(0)
            .setUseHardwareBatchingIfSupported(false)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(
                    BleDevice(
                        name = result.device.name,
                        address = result.device.address,
                        rssi = result.rssi,
                    ),
                )
            }

            override fun onScanFailed(errorCode: Int) {
                Logger.e("BLE scan failed: $errorCode", tag = "Ble")
                close(IllegalStateException("BLE scan failed: $errorCode"))
            }
        }

        Logger.d("BLE scan start", tag = "Ble")
        scanner.startScan(emptyList(), settings, callback)

        awaitClose {
            Logger.d("BLE scan stop", tag = "Ble")
            scanner.stopScan(callback)
        }
    }
}
