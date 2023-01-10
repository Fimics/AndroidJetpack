package com.mic.ui.rv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlin.properties.Delegates


class StarDecoration(context:Context): ItemDecoration() {
    private var mContext:Context?=null
    private var groupHeaderHeight = 0

    private var headPaint: Paint by Delegates.notNull<Paint>()
    private var textPaint: Paint by Delegates.notNull<Paint>()
    private var textRect: Rect by Delegates.notNull<Rect>()
    init {
        mContext=context!!
        
        groupHeaderHeight = dp2px(context, 100f)
        headPaint = Paint()
        headPaint.color = Color.RED
        textPaint = Paint()
        textPaint.textSize = 50f
        textPaint.color = Color.WHITE
        textRect = Rect()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
            val adapter =parent.adapter as StarAdapter
            // 当前屏幕的item个数
            val count: Int = parent.childCount
            val left: Int = parent.paddingLeft
            val right: Int = parent.width - parent.paddingRight
            for (i in 0 until count) {
                // 获取对应i的View
                val view: View = parent.getChildAt(i)
                // 获取View的布局位置
                val position: Int = parent.getChildLayoutPosition(view)
                // 是否是头部
                val isGroupHeader: Boolean = adapter.isGroupHeader(position)
                if (isGroupHeader && view.top - groupHeaderHeight - parent.paddingTop >= 0) {
                    c.drawRect(left.toFloat(), (view.top - groupHeaderHeight).toFloat(),
                        right.toFloat(), view.top.toFloat(), headPaint)
                    val groupName = adapter.getGroupName(position)
                    textPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                    c.drawText(
                        groupName, (left + 20).toFloat(), (view.top -
                                groupHeaderHeight / 2 + textRect.height() / 2).toFloat(), textPaint
                    )
                } else if (view.top - groupHeaderHeight - parent.paddingTop >= 0) {
                    // 分割线
                    c.drawRect(
                        left.toFloat(), (view.top - 4).toFloat(),
                        right.toFloat(), view.top.toFloat(), headPaint
                    )
                }
            }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
            val adapter = parent.adapter as StarAdapter
            // 返回可见区域内的第一个item的position
            val position = (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            // 获取对应position的View
            val itemView:View=parent.findViewHolderForAdapterPosition(position)?.itemView!!
            val left: Int = parent.paddingLeft
            val right: Int = parent.width - parent.paddingRight
            val top: Int = parent.paddingTop
            // 当第二个是组的头部的时候
            val isGroupHeader: Boolean = adapter.isGroupHeader(position + 1)
            if (isGroupHeader) {
                val bottom =
                    Math.min(groupHeaderHeight, itemView.bottom - parent.paddingTop)
                c.drawRect(
                    left.toFloat(), top.toFloat(), right.toFloat(),
                    (top + bottom).toFloat(), headPaint
                )
                val groupName = adapter.getGroupName(position)
                textPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                c.drawText(
                    groupName, (left + 20).toFloat(), (top + bottom
                            - groupHeaderHeight / 2 + textRect.height() / 2).toFloat(), textPaint
                )
            } else {
                c.drawRect(
                    left.toFloat(), top.toFloat(),
                    right.toFloat(), (top + groupHeaderHeight).toFloat(), headPaint
                )
                val groupName = adapter.getGroupName(position)
                textPaint.getTextBounds(groupName, 0, groupName.length, textRect)
                c.drawText(
                    groupName,
                    (left + 20).toFloat(),
                    (top + groupHeaderHeight / 2 + textRect.height() / 2).toFloat(), textPaint
                )
            }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
            val adapter:StarAdapter = parent.adapter as StarAdapter
            val position: Int = parent.getChildLayoutPosition(view)
            val isGroupHeader: Boolean = adapter.isGroupHeader(position)
            // 怎么判断 itemView是头部
            if (isGroupHeader) {
                // 如果是头部，预留更大的地方
                outRect[0, groupHeaderHeight, 0] = 0
            } else {
                // 1像素
                outRect[0, 4, 0] = 0
            }
        }

    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale * 0.5f).toInt()
    }
}
