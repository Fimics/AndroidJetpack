package com.mic.guide.support.serial

import android.serialport.SerialPort
import com.mic.guide.lib.common.ByteUtils
import com.mic.guide.lib.log.Logger
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * 串口辅助（**合并自 libcore `SerialUtils` + `Reader`**，转 Kotlin）。
 *
 * 原 `Reader` 用 RxJava 轮询读取；这里改为一条守护读线程（与 [SerialManager] 同范式），
 * 去掉 RxJava 依赖。回调式 API，适合对接旧逻辑；需要 Flow 用 [SerialManager]。
 */
class SerialUtils(path: String, baudrate: Baudrate) {

    private var serialPort: SerialPort? = try {
        SerialPort.newBuilder(path, baudrate.value).build()
    } catch (e: Exception) {
        Logger.e("串口打开失败: ${e.message}", tag = "Serial")
        null
    }

    @Volatile
    private var reading = false
    private var readThread: Thread? = null

    /** 开始读取，按 [dataType] 把数据回调成字符串。 */
    fun startReadData(callback: SerialCallback, dataType: DataType) {
        val input: InputStream = serialPort?.inputStream ?: return
        if (reading) return
        reading = true
        readThread = Thread {
            val bis = BufferedInputStream(input)
            val buffer = ByteArray(512)
            while (reading) {
                try {
                    if (bis.available() > 0) {
                        val size = bis.read(buffer)
                        if (size > 0) {
                            val result = when (dataType) {
                                DataType.STRING -> String(buffer, 0, size)
                                DataType.HEX -> ByteUtils.bytes2HexStr(buffer, 0, size)
                            }
                            if (result.isNotEmpty()) callback.onResult(result)
                        }
                    } else {
                        Thread.sleep(10)
                    }
                } catch (e: Exception) {
                    if (reading) Logger.e("读取数据异常: ${e.message}", tag = "Serial")
                    break
                }
            }
        }.apply { isDaemon = true; start() }
    }

    fun stopReadData() {
        reading = false
        readThread?.interrupt()
        readThread = null
        runCatching { serialPort?.tryClose() }
        serialPort = null
    }

    /** 向串口写数据。 */
    @Synchronized
    fun setData(data: ByteArray) {
        val output: OutputStream = serialPort?.outputStream ?: return
        try {
            output.write(data)
        } catch (e: Exception) {
            Logger.i("写入数据异常: ${e.message}", tag = "Serial")
        }
    }
}
