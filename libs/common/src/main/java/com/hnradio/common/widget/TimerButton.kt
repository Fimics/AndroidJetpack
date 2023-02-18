package com.hnradio.common.widget

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.button.MaterialButton
import com.hnradio.common.R

/**
 *
 * @Description: 公用倒计时按钮
 * @Author: huqiang
 * @CreateDate: 2021-07-19 10:43
 * @Version: 1.0
 */
class TimerButton(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {
    private val duration: Long
    //计时总长,默认120s
    private val interval: Long

    init {
        isAllCaps = false   //字母小写
        //解析自定义属性
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimerButton)
        duration = (1000 * typedArray.getInteger(R.styleable.TimerButton_duration, 120)).toLong()
        interval = (1000 * typedArray.getInteger(R.styleable.TimerButton_interval, 1)).toLong()
        typedArray.recycle();
    }

    /**
     * 倒计时
     */
    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(duration, interval) {
            override fun onFinish() {
                isEnabled = true
                text = "重新获取"
            }

            override fun onTick(t: Long) {
                text = (t / 1000).toString() + "秒后再次发送"
            }
        }
    }

    /**
     * 开始倒计时,同时按钮不可按
     */
    fun startTimer() {
        isEnabled = false
        countDownTimer.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer.cancel()   //防止内存泄漏
    }

}