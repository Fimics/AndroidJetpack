package com.hnradio.common.widget

import android.app.Activity
import android.text.TextUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.hnradio.common.R
import com.hnradio.common.base.BaseQuickDecorWindow

/**
 * Data:    2020/2/11
 * Author:  chuyanheng
 * Des:     统一loading弹窗
 */
class AppLoading(activity: Activity) : BaseQuickDecorWindow(activity, R.layout.app_loading) {
    private val loadingText: TextView = rootView.findViewById(R.id.loading_text)
    private val loadingImg: ImageView = rootView.findViewById(R.id.loading_img)
    private val animation: Animation = AnimationUtils.loadAnimation(activity, R.anim.app_loading_roatate)
    var isLocked: Boolean = false

    fun show(text: String?) {
        if (isLocked || TextUtils.isEmpty(text)) return
        if (!isShowing()) {
            loadingImg.startAnimation(animation)
            super.show()
        }
        loadingText.text = text
    }

    fun update(text: String){
        if(isShowing()){
            loadingText.text = text
        }
    }

    override fun hide() {
        if (isLocked) return
        loadingImg.clearAnimation()
        super.hide()
    }
}
