package com.mic.guide.support.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * 单设备 GATT 连接门面（Android 原生 [BluetoothGatt]）：连接 / 发现服务 / 读 / 写 / 订阅通知。
 *
 * 状态用 [state]（StateFlow）观察，收到的数据（通知/读取）用 [incoming]（SharedFlow）观察。
 * 标准客户端用法：[connect] → 等 [BleConnectionState.SERVICES_DISCOVERED] → [enableNotifications] / [write]。
 *
 * 调用方需先申请 `BLUETOOTH_CONNECT`（API31+）权限。
 */
@SuppressLint("MissingPermission")
class BleConnection(context: Context) {

    private val appContext = context.applicationContext
    private val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private var gatt: BluetoothGatt? = null

    private val _state = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val state: StateFlow<BleConnectionState> = _state.asStateFlow()

    private val _incoming = MutableSharedFlow<BleData>(extraBufferCapacity = 32)
    val incoming: SharedFlow<BleData> = _incoming.asSharedFlow()

    private val cccDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _state.value = BleConnectionState.CONNECTED
                    Logger.d("GATT connected, discovering services", tag = "Ble")
                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    _state.value = BleConnectionState.DISCONNECTED
                    closeGatt()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            _state.value = if (status == BluetoothGatt.GATT_SUCCESS) {
                BleConnectionState.SERVICES_DISCOVERED
            } else {
                BleConnectionState.FAILED
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            emit(characteristic, characteristic.value)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) emit(characteristic, characteristic.value)
        }
    }

    private fun emit(characteristic: BluetoothGattCharacteristic, value: ByteArray?) {
        if (value == null) return
        _incoming.tryEmit(
            BleData(
                serviceUuid = characteristic.service.uuid.toString(),
                characteristicUuid = characteristic.uuid.toString(),
                value = value,
            ),
        )
    }

    /** 按 MAC 地址连接设备。 */
    fun connect(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: run {
            _state.value = BleConnectionState.FAILED
            return
        }
        _state.value = BleConnectionState.CONNECTING
        gatt = device.connectGatt(appContext, false, callback)
    }

    /** 主动断开。 */
    fun disconnect() {
        gatt?.disconnect()
    }

    private fun characteristic(
        service: UUID,
        char: UUID,
    ): BluetoothGattCharacteristic? = gatt?.getService(service)?.getCharacteristic(char)

    /** 写特征值（默认带响应写）。 */
    @Suppress("DEPRECATION")
    fun write(service: UUID, char: UUID, value: ByteArray): Boolean {
        val c = characteristic(service, char) ?: return false
        c.value = value
        return gatt?.writeCharacteristic(c) ?: false
    }

    /** 读特征值（结果经 [incoming] 回发）。 */
    fun read(service: UUID, char: UUID): Boolean {
        val c = characteristic(service, char) ?: return false
        return gatt?.readCharacteristic(c) ?: false
    }

    /** 订阅特征值通知（写 CCCD 描述符）。 */
    @Suppress("DEPRECATION")
    fun enableNotifications(service: UUID, char: UUID): Boolean {
        val c = characteristic(service, char) ?: return false
        gatt?.setCharacteristicNotification(c, true)
        val descriptor = c.getDescriptor(cccDescriptor) ?: return false
        descriptor.value = BluetoothGattDescriptorValues.ENABLE_NOTIFICATION
        return gatt?.writeDescriptor(descriptor) ?: false
    }

    private fun closeGatt() {
        gatt?.close()
        gatt = null
    }

    private object BluetoothGattDescriptorValues {
        val ENABLE_NOTIFICATION = byteArrayOf(0x01, 0x00)
    }
}
