package com.mic.guide.lib.extension

import android.content.Context
import android.util.TypedValue
import android.widget.Toast

/** dp 转 px（基于当前屏幕密度）。 */
fun Context.dp2px(dp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics,
).toInt()

/** 短 Toast 便捷封装。 */
fun Context.toast(msg: CharSequence) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}