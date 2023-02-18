package com.hnradio.common.util.mqtt

import android.util.Base64
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util.mqtt
 * @ClassName: Tool
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/8/10 9:42 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/10 9:42 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
/**
 * @param text      要签名的文本
 * @param secretKey 阿里云MQ secretKey
 * @return 加密后的字符串
 * @throws InvalidKeyException
 * @throws NoSuchAlgorithmException
 */
@Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
fun macSignature(text: String, secretKey: String): String {
    val charset: Charset = Charset.forName("UTF-8")
    val algorithm = "HmacSHA1"
    val mac: Mac = Mac.getInstance(algorithm)
    mac.init(SecretKeySpec(secretKey.toByteArray(charset), algorithm))
    val bytes: ByteArray = mac.doFinal(text.toByteArray(charset))
    // android的base64编码注意换行符情况, 使用NO_WRAP
    return String(Base64.encode(bytes, Base64.NO_WRAP), charset)
}