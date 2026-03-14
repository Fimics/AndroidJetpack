package com.mic.ble.client.scanner

import android.annotation.SuppressLint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.kk.core.utils.KLog
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

private const val TAG = "BleScanner"

/**
 * BLE 设备扫描工具
 *
 * 使用 Nordic Scanner 库扫描所有有名称的 BLE 设备
 */
@SuppressLint("MissingPermission")
class BleScanner {

    fun scanForDevices(): Flow<ScanResult> = callbackFlow {
        KLog.d(TAG, "scanForDevices() - 启动扫描")

        val scanner = BluetoothLeScannerCompat.getScanner()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val deviceName = result.scanRecord?.deviceName ?: result.device.name
                if (!deviceName.isNullOrBlank()) {
                    trySendBlocking(result)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                KLog.e(TAG, "扫描失败 - errorCode=$errorCode")
                close(Exception("扫描失败 (code=$errorCode)"))
            }
        }

        scanner.startScan(null, scanSettings, scanCallback)

        awaitClose {
            KLog.d(TAG, "停止扫描")
            scanner.stopScan(scanCallback)
        }
    }
}
