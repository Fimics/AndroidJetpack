package com.mic.guide.support.serial

import android.serialport.SerialPort
import android.serialport.SerialPortFinder
import com.mic.guide.lib.log.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import java.io.OutputStream

/**
 * 串口通信门面（§2.2 硬件能力下沉 support）：基于 licheedev `android-serialport`。
 *
 * 用法：[availablePorts] 列设备 → [open] 打开 → 收 [incoming]（读线程实时回发）/ [send] 发送 → [close]。
 * 串口读写必须在子线程：本类内部起一条读线程循环读 [InputStream]，写在调用线程同步执行。
 *
 * 注意：非 root 设备访问 `/dev/ttySx` 通常需要相应权限（厂商定制系统/串口节点可读写）。
 */
class SerialManager {

    private var serialPort: SerialPort? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    @Volatile
    private var running = false
    private var readThread: Thread? = null

    private val _state = MutableStateFlow(false)
    /** 是否已打开。 */
    val opened: StateFlow<Boolean> = _state.asStateFlow()

    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    /** 串口读到的原始字节流（粘包/拆包由上层协议处理）。 */
    val incoming: SharedFlow<ByteArray> = _incoming.asSharedFlow()

    /** 枚举系统串口节点路径。 */
    fun availablePorts(): List<String> =
        runCatching { SerialPortFinder().allDevicesPath?.toList() }.getOrNull().orEmpty()

    /**
     * 打开串口。
     * @param parity 0=NONE 1=ODD 2=EVEN；[dataBits] 5~8；[stopBits] 1 或 2。
     */
    @Synchronized
    fun open(
        path: String,
        baudRate: Int,
        dataBits: Int = 8,
        parity: Int = 0,
        stopBits: Int = 1,
    ): Boolean {
        if (running) close()
        return runCatching {
            val port = SerialPort.newBuilder(path, baudRate)
                .dataBits(dataBits)
                .parity(parity)
                .stopBits(stopBits)
                .build()
            serialPort = port
            inputStream = port.inputStream
            outputStream = port.outputStream
            running = true
            _state.value = true
            startReadLoop()
            Logger.d("serial opened: $path @$baudRate", tag = "Serial")
            true
        }.getOrElse {
            Logger.e("serial open failed: $path", it, tag = "Serial")
            close()
            false
        }
    }

    private fun startReadLoop() {
        readThread = Thread {
            val buffer = ByteArray(1024)
            val ins = inputStream ?: return@Thread
            while (running) {
                try {
                    val count = ins.read(buffer)
                    if (count > 0) {
                        _incoming.tryEmit(buffer.copyOf(count))
                    }
                } catch (e: Exception) {
                    if (running) Logger.e("serial read error", e, tag = "Serial")
                    break
                }
            }
        }.apply { isDaemon = true; start() }
    }

    /** 发送字节（同步写）。 */
    fun send(data: ByteArray): Boolean = runCatching {
        outputStream?.apply { write(data); flush() } != null
    }.getOrDefault(false)

    /** 发送十六进制字符串（如 "A1 B2 03"）。 */
    fun sendHex(hex: String): Boolean {
        val bytes = hex.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return send(bytes)
    }

    @Synchronized
    fun close() {
        running = false
        readThread?.interrupt()
        readThread = null
        runCatching { inputStream?.close() }
        runCatching { outputStream?.close() }
        runCatching { serialPort?.close() }
        inputStream = null
        outputStream = null
        serialPort = null
        _state.value = false
    }
}
