package com.hnradio.common.base

import android.app.Dialog
import android.content.Context
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.hnradio.common.R

abstract class BaseDialog(context: Context) : Dialog(context, R.style.CustomDialog) {
    private val mWindowManager: WindowManager
    private val mDisplay: Display
    protected var mInflater: LayoutInflater

    // TODO 缺少横竖屏切换问题处理
    protected abstract fun initView(): View?
    override fun show() {
        show(DEFAULT_HEIGHT_PERCENT, DEFAULT_WIDTH_PERCENT)
    }

    fun show(heightPercent: Float, widthPercent: Float) {
        super.show()
        val mParams: WindowManager.LayoutParams = this.window!!.attributes
        mParams.width = ((mDisplay.getWidth() * widthPercent).toInt())
        if (heightPercent != DEFAULT_HEIGHT_PERCENT) {
            mParams.height = ((mDisplay.getHeight() * heightPercent).toInt())
        }
        this.window!!.attributes = mParams
    }

    interface BaseDialogClickListener {
        interface OnCancelListener {
            fun onClick()
        }

        interface OnClickListener {
            fun onClick()
        }

        interface OnActiconListener {
            fun onClick()
        }

        interface OnOkListener {
            fun onClick()
        }

        interface OnDimissListener {
            fun onDimiss()
        }
    }

    companion object {
        private const val DEFAULT_WIDTH_PERCENT = 0.75f
        private const val DEFAULT_HEIGHT_PERCENT = 0.0f
    }

    init {
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplay = mWindowManager.getDefaultDisplay()
        mInflater = LayoutInflater.from(context)
        setContentView(initView()!!)
    }
}