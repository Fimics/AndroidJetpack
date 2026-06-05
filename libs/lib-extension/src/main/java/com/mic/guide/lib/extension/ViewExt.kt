package com.mic.guide.lib.extension

import android.view.View

/** 显示视图。 */
fun View.visible() {
    visibility = View.VISIBLE
}

/** 占位隐藏（仍占布局空间）。 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/** 彻底隐藏（不占布局空间）。 */
fun View.gone() {
    visibility = View.GONE
}

/** 按布尔显隐：true → visible，false → gone。 */
fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/** 防抖点击：默认 500ms 内重复点击被忽略，避免连点导致的重复跳转/提交。 */
fun View.onSingleClick(intervalMs: Long = 500L, action: (View) -> Unit) {
    var lastClick = 0L
    setOnClickListener { v ->
        val now = System.currentTimeMillis()
        if (now - lastClick >= intervalMs) {
            lastClick = now
            action(v)
        }
    }
}