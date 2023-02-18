package com.hnradio.common.util.recyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by shao on 2016/11/24.垂直水平间隔线
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    /*
     * RecyclerView的布局方向，默认先赋值
     * 为纵向布局
     * RecyclerView 布局可横向，也可纵向
     * 横向和纵向对应的分割想画法不一样
     * */
    private int mOrientation = LinearLayoutManager.VERTICAL;

    /**
     * item之间分割线的size，默认为1
     */
    private int mItemSize = 1;

    /**
     * 绘制item分割线的画笔，和设置其属性
     * 来绘制个性分割线
     */
    private Paint mPaint;

    private boolean isDrawLast;//最后一条条目是否绘制

    /**
     * 构造方法传入布局方向，不可不传
     *
     * @param orientation
     */
    public DividerItemDecoration(int orientation, int color, int size, boolean isDrawLast) {
        this.mOrientation = orientation;
        this.isDrawLast = isDrawLast;
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请传入正确的参数");
        }
        mItemSize = size;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        /*设置填充*/
        mPaint.setStyle(Paint.Style.FILL);
    }

    public DividerItemDecoration(int orientation, int color, int size) {
        this(orientation, color, size, false);
    }

    public DividerItemDecoration(int color, int size) {
        this(LinearLayoutManager.VERTICAL, color, size);
    }

    public DividerItemDecoration(int color, int size, boolean isDrawLast) {
        this(LinearLayoutManager.VERTICAL, color, size, isDrawLast);
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /**
     * 绘制纵向 item 分割线
     *
     * @param canvas
     * @param parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = isDrawLast ? parent.getChildCount() : parent.getChildCount() - 1;
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /**
     * 绘制横向 item 分割线
     *
     * @param canvas
     * @param parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = isDrawLast ? parent.getChildCount() : parent.getChildCount() - 1;
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            if (parent.indexOfChild(child) == 0)
                continue;
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    /**
     * 设置item分割线的size
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            if (isDrawLast) {
                outRect.set(0, 0, 0, mItemSize);
            } else {
                if ((parent.getLayoutManager().getPosition(view) == (state.getItemCount() - 1)))
                    outRect.set(0, 0, 0, 0);
                else
                    outRect.set(0, 0, 0, mItemSize);
            }

        } else {
            if (isDrawLast) {
                outRect.set((parent.getLayoutManager().getPosition(view)) == 0 ? mItemSize : 0, 0, mItemSize, 0);
            } else {
                if ((parent.getLayoutManager().getPosition(view) == (state.getItemCount() - 1)))
                    outRect.set(0, 0, 0, 0);
                else
                    outRect.set(0, 0, mItemSize, 0);
            }
        }
    }

}
