package com.hnradio.common.adapter.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 设置 水平或竖直 recyclerview间隔
 * created by 乔岩 on 2021/6/29
 */
class LinearItemDecoration(private val divider: Int, private val orientation: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.right = divider
        outRect.bottom = divider

        when (orientation) {
            LinearLayoutManager.VERTICAL -> {
                outRect.left = divider
                if (parent.getChildAdapterPosition(view) == 0)
                    outRect.top = divider
            }
            LinearLayoutManager.HORIZONTAL -> {
                outRect.top = divider
                if (parent.getChildAdapterPosition(view) == 0)
                    outRect.left = divider
            }
        }

    }


}