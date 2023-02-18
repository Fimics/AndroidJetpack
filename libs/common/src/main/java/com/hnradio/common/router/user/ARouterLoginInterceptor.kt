package com.hnradio.common.router.user

import android.content.Context
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.hnradio.common.manager.UserManager
import com.hnradio.common.router.MainRouter
import com.hnradio.common.util.L
import java.lang.IllegalArgumentException


/**
 * @author ytf
 * Created by on 2021/11/11 16:39
 */
@Interceptor(priority = 10)
class ARouterLoginInterceptor : IInterceptor{
    override fun init(context: Context?) {
        L.e("拦截器初始化")
    }

    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        val path = postcard?.path
        val isLogin = UserManager.isLogin()

        if (isLogin) { // 如果已经登录不拦截
            callback?.onContinue(postcard)
        } else {  // 如果没有登录
            when (path) {
                MainRouter.LivePlayPath2 -> callback?.onInterrupt(IllegalArgumentException("not login"))
                else -> callback!!.onContinue(postcard)
            }
        }
    }
}