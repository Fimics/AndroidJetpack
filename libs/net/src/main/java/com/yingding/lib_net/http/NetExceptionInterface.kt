package com.yingding.lib_net.http

/**
 * Copyright:   2019 www.yingding.com Inc. All rights reserved.
 * project:     ydm_schoolyard
 * Data:        2019/4/11
 * Author:      yingding
 * Mail:        shaoguotong@yingding.org
 * Version：    V1.0
 * Title:       网络服务器异常统一处理接口
 */
interface NetExceptionInterface {
    /**
     * 网络出现异常处理
     */
    fun onNetFail(e: Throwable,isToast: Boolean)

    /**
     * 服务器返回结果异常处理
     */
    fun onCodeFail(code: Int, errorMsg: String?,isToast: Boolean)
}
