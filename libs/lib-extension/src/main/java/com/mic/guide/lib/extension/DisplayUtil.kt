package com.mic.guide.lib.extension

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * 屏幕度量工具（迁移自 libcore `DisplayUtil`，转 Kotlin）。
 * 提供常用的 dp/px 换算与屏幕尺寸读取（原类仅打印日志，这里改为返回可用值）。
 */
object DisplayUtil {

    @Suppress("DEPRECATION")
    private fun metrics(context: Context, real: Boolean): DisplayMetrics {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        if (real) wm.defaultDisplay.getRealMetrics(dm) else wm.defaultDisplay.getMetrics(dm)
        return dm
    }

    /** 可用区域宽（px）。 */
    fun screenWidth(context: Context): Int = metrics(context, false).widthPixels

    /** 可用区域高（px）。 */
    fun screenHeight(context: Context): Int = metrics(context, false).heightPixels

    /** 物理屏宽（px，含系统栏）。 */
    fun realScreenWidth(context: Context): Int = metrics(context, true).widthPixels

    /** 物理屏高（px，含系统栏）。 */
    fun realScreenHeight(context: Context): Int = metrics(context, true).heightPixels

    fun dp2px(context: Context, dp: Float): Int =
        (dp * context.resources.displayMetrics.density + 0.5f).toInt()

    fun px2dp(context: Context, px: Float): Int =
        (px / context.resources.displayMetrics.density + 0.5f).toInt()

    fun sp2px(context: Context, sp: Float): Int =
        (sp * context.resources.displayMetrics.scaledDensity + 0.5f).toInt()
}
