package com.yingding.lib_net.bean.base

import androidx.annotation.Keep

/**
 *Copyright:   2020 www.yingding.com Inc. All rights reserved.
 *project:     ydm_online
 *Data:        2020-03-11
 *Author:      yingding
 *Mail:        shaoguotong@yingding.org
 *Version：    V1.0
 *Title:       基础分页
 */
@Keep
data class BasePageBean<T>(
    val current: Int,
    val orders: List<Any>,
    val pages: Int,
    val records: List<T>,
    val searchCount: Boolean,
    val size: Int,
    val total: Int
)