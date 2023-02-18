package com.hnradio.common.util

import android.os.Looper
import com.hnradio.common.manager.UserManager
import com.yingding.lib_net.http.NetExceptionInterface

/**
 *Copyright:   2020 www.yingding.com Inc. All rights reserved.
 *project:     ydm_online
 *Data:        2020-02-10
 *Author:      yingding
 *Mail:        shaoguotong@yingding.org
 *Version：    V1.0
 *Title:       网络统一处理
 */
class NetExceptionProcess : NetExceptionInterface {
    override fun onNetFail(e: Throwable, isToast: Boolean) {
        e.printStackTrace()
        if (Looper.getMainLooper().thread.id == Thread.currentThread().id
            && isToast
        ) {
            when {
                e.message?.contains("Failed to connect") == true
                -> ToastUtils.show("无法连接服务器，请检查网络")
                e.message?.contains("timeout") == true
                -> ToastUtils.show("请求超时，请稍后再试")
                else
                -> ToastUtils.show("请求异常")
            }
        }
    }

    override fun onCodeFail(code: Int, errorMsg: String?, isToast: Boolean) {
        if (Looper.getMainLooper().thread.id == Thread.currentThread().id && isToast)
            ToastUtils.show(errorMsg)
        if (code == 401) {
            //token 失效
            UserManager.saveUserToken("")
            UserManager.checkIsGotoLogin()
        }
    }
}