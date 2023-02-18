package com.hnradio.common.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-10 16:30
 * @Version: 1.0
 */
object StringUtils {
    /**
     * 元转分，确保price保留两位有效数字
     * @return
     */
    fun changeY2F(price: Double): Int {
        var price = price
        val df = DecimalFormat("#.00")
        price = df.format(price).toDouble()
        return (price * 100).toInt()
    }

    /**
     * 分转元，转换为bigDecimal在toString
     * @return
     */
    fun changeF2Y(price: Long): String? {
        val df = DecimalFormat("0.00")
        var price: Double = price.toDouble() / 100
        return df.format(price)
    }

    fun getPhoneHiddenNum(num: String): String {
        return if (!TextUtils.isEmpty(num) && num.length == 11) {
            num.replace("(\\d{3})\\d{4}(\\d{4})".toRegex(), "$1****$2")
        } else num
    }

    fun copyText(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("", text)
        cm.setPrimaryClip(clipData)
        if (text.trim().isNotEmpty()) {
            ToastUtils.show("内容已复制")
        }
    }
}

//是否为手机号
fun String.isPhone(): Boolean {
    val pattern = Pattern.compile("[0-9]\\d{10}\$")
    return pattern.matcher(this).matches()
}

//是否为邮箱
fun String.isEmail(): Boolean {
    val pattern = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+\$")
    return pattern.matcher(this).matches()
}

//是否为网页
fun String.isUrl(): Boolean {
    val pattern =
        Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$")
    return pattern.matcher(this).matches()
}

/**
 * 身份证id正则校验
 */
fun  String.isIDNumber(): Boolean {
    if (this == null || "" == this) {
        return false
    }
    // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
    val regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" + "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)"
    //假设18位身份证号码:41000119910101123X  410001 19910101 123X
    //^开头
    //[1-9] 第一位1-9中的一个      4
    //\\d{5} 五位数字           10001（前六位省市县地区）
    //(18|19|20)                19（现阶段可能取值范围18xx-20xx年）
    //\\d{2}                    91（年份）
    //((0[1-9])|(10|11|12))     01（月份）
    //(([0-2][1-9])|10|20|30|31)01（日期）
    //\\d{3} 三位数字            123（第十七位奇数代表男，偶数代表女）
    //[0-9Xx] 0123456789Xx其中的一个 X（第十八位为校验值）
    //$结尾

    //假设15位身份证号码:410001910101123  410001 910101 123
    //^开头
    //[1-9] 第一位1-9中的一个      4
    //\\d{5} 五位数字           10001（前六位省市县地区）
    //\\d{2}                    91（年份）
    //((0[1-9])|(10|11|12))     01（月份）
    //(([0-2][1-9])|10|20|30|31)01（日期）
    //\\d{3} 三位数字            123（第十五位奇数代表男，偶数代表女），15位身份证不含X
    //$结尾


    val matches = this.matches(regularExpression.toRegex())

    //判断第18位校验值
    if (matches) {

        if (this.length == 18) {
            try {
                val charArray = this.toCharArray()
                //前十七位加权因子
                val idCardWi = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
                //这是除以11后，可能产生的11位余数对应的验证码
                val idCardY = arrayOf("1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2")
                var sum = 0
                for (i in idCardWi.indices) {
                    val current = Integer.parseInt(charArray[i].toString())
                    val count = current * idCardWi[i]
                    sum += count
                }
                val idCardLast = charArray[17]
                val idCardMod = sum % 11
                if (idCardY[idCardMod].toUpperCase() == idCardLast.toString().toUpperCase()) {
                    return true
                } else {
                    println("身份证最后一位:" + idCardLast.toString().toUpperCase() +
                            "错误,正确的应该是:" + idCardY[idCardMod].toUpperCase())
                    return false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                println("异常:$this")
                return false
            }

        }

    }
    return matches
}


