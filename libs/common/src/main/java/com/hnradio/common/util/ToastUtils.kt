package com.hnradio.common.util

import android.text.TextUtils
import android.view.Gravity
import android.widget.Toast

/**
 * Data:    2020/2/11
 * Author:  chuyanheng
 * Des:     统一toast
 */
object ToastUtils {
    private var toast: Toast? = null

    @JvmOverloads
    fun show(msg: String?, isCenter: Boolean = false, duration: Int = Toast.LENGTH_SHORT) {
        if (TextUtils.isEmpty(msg)) return
        val instance = Global.application
        if (toast == null || toast!!.view?.isShown == true) toast =
            Toast.makeText(instance.applicationContext, msg, duration)
        if (duration != toast?.duration) toast?.duration = duration
        toast?.setGravity(
            if (isCenter) Gravity.CENTER else Gravity.BOTTOM,
            0,
            ScreenUtils.getScreenHeight(instance) / 10
        )
        toast?.setText(msg)
        toast?.show()
    }
}
