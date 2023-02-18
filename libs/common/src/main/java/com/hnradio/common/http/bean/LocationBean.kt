package com.hnradio.common.http.bean

import androidx.annotation.Keep

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-20 20:00
 * @Version: 1.0
 */
@Keep
data class LocationBean(
    val title: String?,
    val address: String?,
    val province: String?,
    val city: String?,
    val district: String?,
    val longitude: Double?,//经度
    val latitude: Double?,//纬度
)
