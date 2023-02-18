package com.hnradio.common.util

import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * ProjectName:    sfm-android
 * Package:        cn.cbct.seefm.base.utils
 * Description:    数字各种转换
 * Author:         ZGCS
 * CreateDate:     2021/3/25 16:37
 * UpdateUser:     更新者：
 * UpdateDate:     2021/3/25 16:37
 * UpdateRemark:   更新说明：
 * Version:        1.0
 */

//数量转换为相应格式化
fun Int.numFormat(): String {
    return if (this < 10000)
        this.toString()
    else
        "${this / 10000}.${this % 10000 / 1000}W"
}

//长整型转换为时间
fun Long.toTime(format: String = "yyyy-mm-dd HH:MM:ss"): String {
    val dateFormat = SimpleDateFormat(format, Locale.CHINA)
    return dateFormat.format(Date(this))
}

//获取时长字符串
fun Long.durationStr(): String {
    val m = this / 60000
    val s = (this - m * 60000) / 1000
    return "${if (m < 10) "0" else ""}${m}:${if (s < 10) "0" else ""}${s}"
}

//获取与当前时间的差距时分秒
fun Long.dhmsStr(): String {
    var cha = System.currentTimeMillis() - this
    val tian = cha / (1000 * 60 * 60 * 24)
    cha -= tian * (1000 * 60 * 60 * 24)
    val xiaoshi = cha / (1000 * 60 * 60)
    cha -= xiaoshi * (1000 * 60 * 60)
    val fenzhong = cha / (1000 * 60)
    cha -= fenzhong * (1000 * 60)
    val miao = cha / 1000
    return "${tian}天${xiaoshi}时${fenzhong}分${miao}秒"
}


/**
 * 属性扩展 Int to dp
 */
val Int.dp: Int
    get() = ScreenUtils.dip2px(Global.application, this.toFloat())

/**
 * 直接依据dimen id获取相应dp 转px值
 */
val Int.idDp: Int
    get() {
        val res = Global.application.resources
        val resId = res.getIdentifier("dp_$this", "dimen", Global.application.packageName)
        return if (resId == 0) dp else res.getDimension(resId).toInt()
    }