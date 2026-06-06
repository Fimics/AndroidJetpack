package com.mic.guide.lib.extension

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import com.mic.guide.lib.common.AppGlobals

/**
 * 吐司工具（迁移自 libcore `ToastUtils`，转 Kotlin）。
 *
 * 主线程安全（内部 post 到主线程），复用同一个 [Toast] 实例避免堆叠。
 */
object ToastUtils {

    private val handler = Handler(Looper.getMainLooper())
    private var toast: Toast? = null

    private var gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
    private var xOffset = 0
    private var yOffset = (64 * AppGlobals.getApplication().resources.displayMetrics.density + 0.5).toInt()

    @JvmStatic
    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        this.gravity = gravity
        this.xOffset = xOffset
        this.yOffset = yOffset
    }

    @JvmStatic
    fun showShort(text: CharSequence) = show(text, Toast.LENGTH_SHORT)

    @JvmStatic
    fun showShort(resId: Int) =
        show(AppGlobals.getApplication().resources.getText(resId), Toast.LENGTH_SHORT)

    @JvmStatic
    fun showLong(text: CharSequence) = show(text, Toast.LENGTH_LONG)

    @JvmStatic
    fun showLong(resId: Int) =
        show(AppGlobals.getApplication().resources.getText(resId), Toast.LENGTH_LONG)

    @JvmStatic
    fun cancel() {
        toast?.cancel()
        toast = null
    }

    private fun show(text: CharSequence, duration: Int) {
        handler.post {
            cancel()
            toast = Toast.makeText(AppGlobals.getApplication(), text, duration).also {
                it.setGravity(gravity, xOffset, yOffset)
                it.show()
            }
        }
    }
}
