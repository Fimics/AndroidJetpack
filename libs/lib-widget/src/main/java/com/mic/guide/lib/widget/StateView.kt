package com.mic.guide.lib.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

/**
 * 多状态容器控件：在「内容 / 加载中 / 空 / 错误」之间切换，列表/详情页统一缺省态体验。
 *
 * 用法（把真实内容作为唯一子 View 放进来，其余态由本控件内置）：
 * ```xml
 * <com.mic.guide.lib.widget.StateView android:id="@+id/stateView" ...>
 *     <androidx.recyclerview.widget.RecyclerView ... />
 * </com.mic.guide.lib.widget.StateView>
 * ```
 * 代码：`stateView.showLoading()` / `showEmpty("暂无数据")` / `showError("...") { retry() }` / `showContent()`。
 */
class StateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var contentView: View? = null

    private val loadingView: View by lazy { buildLoading() }
    private val emptyView: TextView by lazy { buildMessage() }
    private val errorContainer: LinearLayout by lazy { buildError() }
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    enum class State { CONTENT, LOADING, EMPTY, ERROR }

    var state: State = State.CONTENT
        private set

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 第一个非内置子 View 视为「内容」
        if (childCount > 0) contentView = getChildAt(0)
    }

    fun showContent() = switchTo(State.CONTENT)

    fun showLoading() = switchTo(State.LOADING)

    fun showEmpty(message: String = "暂无数据") {
        emptyView.text = message
        switchTo(State.EMPTY)
    }

    fun showError(message: String = "加载失败", onRetry: (() -> Unit)? = null) {
        errorText.text = message
        retryButton.visibility = if (onRetry != null) View.VISIBLE else View.GONE
        retryButton.setOnClickListener { onRetry?.invoke() }
        switchTo(State.ERROR)
    }

    private fun switchTo(target: State) {
        state = target
        ensureAttached(loadingView)
        ensureAttached(emptyView)
        ensureAttached(errorContainer)
        contentView?.visibility = if (target == State.CONTENT) View.VISIBLE else View.GONE
        loadingView.visibility = if (target == State.LOADING) View.VISIBLE else View.GONE
        emptyView.visibility = if (target == State.EMPTY) View.VISIBLE else View.GONE
        errorContainer.visibility = if (target == State.ERROR) View.VISIBLE else View.GONE
    }

    private fun ensureAttached(view: View) {
        if (view.parent == null) {
            addView(
                view,
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT,
                ).apply { gravity = Gravity.CENTER },
            )
        }
    }

    private fun buildLoading(): View = ProgressBar(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
        ).apply { gravity = Gravity.CENTER }
        visibility = View.GONE
    }

    private fun buildMessage(): TextView = TextView(context).apply {
        gravity = Gravity.CENTER
        visibility = View.GONE
    }

    private fun buildError(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        visibility = View.GONE
        errorText = TextView(context).apply { gravity = Gravity.CENTER }
        retryButton = Button(context).apply { text = "重试" }
        addView(errorText)
        addView(retryButton)
    }
}
