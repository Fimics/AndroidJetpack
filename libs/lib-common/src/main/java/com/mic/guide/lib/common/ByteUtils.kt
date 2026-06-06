package com.mic.guide.lib.common

/**
 * 字节/十六进制转换工具（迁移自 libcore `ByteUtils`，转 Kotlin）。
 * 串口/蓝牙等二进制协议常用。
 */
object ByteUtils {

    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    /** 字节数组 → 大写十六进制字符串。 */
    @JvmStatic
    fun bytes2HexStr(src: ByteArray?): String {
        if (src == null || src.isEmpty()) return ""
        val sb = StringBuilder(src.size * 2)
        for (b in src) {
            sb.append(HEX_CHARS[(b.toInt() ushr 4) and 0x0F])
            sb.append(HEX_CHARS[b.toInt() and 0x0F])
        }
        return sb.toString()
    }

    /** 指定区间的字节数组 → 十六进制字符串。 */
    @JvmStatic
    fun bytes2HexStr(src: ByteArray, offset: Int, length: Int): String =
        bytes2HexStr(src.copyOfRange(offset, offset + length))

    /** 十六进制字符串 → 十进制。 */
    @JvmStatic
    fun hexStr2Decimal(hex: String): Long = hex.toLong(16)

    /** 十进制 → 足位（偶数长度）大写十六进制。 */
    @JvmStatic
    fun decimal2FitHex(num: Long): String {
        val hex = num.toString(16).uppercase()
        return if (hex.length % 2 != 0) "0$hex" else hex
    }

    /** 十进制 → 指定长度、前补 0 的十六进制。 */
    @JvmStatic
    fun decimal2FitHex(num: Long, strLength: Int): String =
        decimal2FitHex(num).padStart(strLength, '0')

    /** 字符串 → 十六进制字符串（UTF-8）。 */
    @JvmStatic
    fun str2HexString(str: String): String {
        val sb = StringBuilder()
        for (b in str.toByteArray(Charsets.UTF_8)) {
            sb.append(HEX_CHARS[(b.toInt() and 0xF0) shr 4])
            sb.append(HEX_CHARS[b.toInt() and 0x0F])
        }
        return sb.toString()
    }

    /** 十六进制字符串 → 字节数组。 */
    @JvmStatic
    fun hexStr2Bytes(hex: String): ByteArray {
        val clean = hex.trim()
        val len = clean.length / 2
        val result = ByteArray(len)
        val chars = clean.uppercase().toCharArray()
        for (i in 0 until len) {
            val pos = i * 2
            result[i] = ((hexChar2Byte(chars[pos]) shl 4) or hexChar2Byte(chars[pos + 1])).toByte()
        }
        return result
    }

    private fun hexChar2Byte(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'A'..'F' -> c - 'A' + 10
        in 'a'..'f' -> c - 'a' + 10
        else -> -1
    }
}
