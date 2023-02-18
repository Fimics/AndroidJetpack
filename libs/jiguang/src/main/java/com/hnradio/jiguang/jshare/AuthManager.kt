package com.hnradio.jiguang.jshare

import android.content.Context
import cn.jiguang.share.android.api.AuthListener
import cn.jiguang.share.android.api.JShareInterface
import cn.jiguang.share.android.api.Platform
import cn.jiguang.share.android.model.AccessTokenInfo
import cn.jiguang.share.android.model.BaseResponseInfo
import cn.jiguang.share.wechat.Wechat
import cn.jiguang.share.weibo.SinaWeibo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-15 15:15
 * @Version: 1.0
 */
class AuthManager(val context: Context) {
    companion object {
        val AuthTypeWechat = Wechat.Name
        val AuthTypeWeibo = SinaWeibo.Name
    }

    suspend fun authorizing(type: String): AccessBean = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            JShareInterface.authorize(type, object : AuthListener {
                override fun onComplete(platform: Platform?, action: Int, data: BaseResponseInfo?) {
                    //授权信息
                    var access = (data as AccessTokenInfo)
                    val accessBean = AccessBean(
                        access.token,
                        access.refeshToken,
                        access.openid,
                        access.expiresIn,
                        null,
                        null,
                        null
                    )
                    continuation.resume(accessBean)
                }

                override fun onError(
                    platform: Platform?,
                    action: Int,
                    errorCode: Int,
                    error: Throwable?
                ) {
                    val accessBean = AccessBean(
                        errorCode = errorCode,
                        errorMsg = "授权失败"
                    )
                    continuation.resume(accessBean)
                }

                override fun onCancel(platform: Platform?, action: Int) {
                    val accessBean = AccessBean(
                        errorCode = -1,
                        errorMsg = "授权取消"
                    )
                    continuation.resume(accessBean)
                }
            })
        }
    }

}


