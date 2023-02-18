package com.hnradio.common.util.upgrade

import com.hnradio.common.http.bean.AppVersionBean

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-23 10:59
 * @Version: 1.0
 */
interface CheckCallBack {
    fun onCheckResult(version: AppVersionBean)
}