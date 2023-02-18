package com.hnradio.common.util

import java.math.BigDecimal
import java.util.regex.Pattern

/**
 *
 * Created by liguangze on 2021/8/21.
 */
object RegexUtils {
    fun regexPhone(phone: String): Boolean {
        var mainRegex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,1,2,3,5-9])|(177))\\d{8}$"
        var p = Pattern.compile(mainRegex)
        val m = p.matcher(phone)
        return m.matches()
    }

}