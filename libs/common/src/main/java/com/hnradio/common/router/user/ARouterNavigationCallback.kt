package com.hnradio.common.router.user

import android.app.Activity
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.hnradio.common.router.MainRouter

/**
 * @author ytf
 * Created by on 2021/11/11 16:45
 */
class ARouterNavigationCallback : NavigationCallback {

    override fun onFound(postcard: Postcard?) {
        
    }

    override fun onLost(postcard: Postcard?) {
        
    }

    override fun onArrival(postcard: Postcard?) {
        
    }

    override fun onInterrupt(postcard: Postcard?) {
        postcard?.let {
            if(it.context is Activity){
                ((it.context) as Activity).finish()
            }
            val bundle = it.extras
            ARouter.getInstance().build(MainRouter.LoginActivityPath)
                .with(bundle)
                .navigation()
        }
    }
}