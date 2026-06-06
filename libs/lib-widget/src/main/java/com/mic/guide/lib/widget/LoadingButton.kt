package com.mic.guide.lib.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton

/**
 * 带加载态的按钮：[loading]=true 时禁用点击并显示转圈，避免重复提交（表单/网络请求场景）。
 *
 * 用法：`btn.text = "登录"`；提交时 `btn.loading = true`，回调后 `btn.loading = false`。
 */
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val button = AppCompatButton(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }
    private val progress = ProgressBar(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
        ).apply { gravity = Gravity.CENTER }
        visibility = View.GONE
    }

    init {
        addView(button)
        addView(progress)
    }

    var text: CharSequence
        get() = button.text
        set(value) { button.text = value }

    var loading: Boolean = false
        set(value) {
            field = value
            progress.visibility = if (value) View.VISIBLE else View.GONE
            button.text = if (value) "" else savedText
            isEnabled = !value
            button.isEnabled = !value
        }

    private var savedText: CharSequence = ""

    fun setOnClick(listener: () -> Unit) {
        button.setOnClickListener { if (!loading) listener() }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        button.isEnabled = enabled
    }

    fun setButtonText(value: CharSequence) {
        savedText = value
        if (!loading) button.text = value
    }
}
