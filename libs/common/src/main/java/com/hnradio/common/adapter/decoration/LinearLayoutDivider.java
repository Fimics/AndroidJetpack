package com.hnradio.common.adapter.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ytf on 2017/4/3 003.
 * Description: 线性布局的分割线, 传进来的drawable布局注意设置size属性，不然分割线会异常
 * bug report: drawable返回-1表示没有设置size
 */

public class LinearLayoutDivider extends RecyclerView.ItemDecoration
{

    public static final int LINEAR_H = LinearLayoutManager.HORIZONTAL;
    public static final int LINEAR_V = LinearLayoutManager.VERTICAL;

    private int linearDividerOritention;
    private Drawable mDivider;
    private int thickness = 1;
    private int offset;
    private int dividerColor = 0xFFE0E0E0;

    public LinearLayoutDivider(Context ctx, int linearDividerOritention)
    {
        this(ctx,linearDividerOritention, null);
    }

    public LinearLayoutDivider(Context ctx, int linearDividerOritention, @DrawableRes int dividerRes)
    {
        this(ctx,linearDividerOritention, ctx.getResources().getDrawable(dividerRes));
    }

    /***
     * 创建一个分割线
     * @param ctx
     * @param linearDividerOritention   布局方向，可用的值是LinearLayoutDivider.LINEAR_H, LinearLayoutDivider.LINEAR_V
     * @param dividerDrawable
     */
    public LinearLayoutDivider(Context ctx, int linearDividerOritention, Drawable dividerDrawable)
    {
        super();
        if(dividerDrawable == null)
        {
            mDivider = createDividerLineDrawable(linearDividerOritention);
        }else
        {
            mDivider = dividerDrawable;
        }
        setLinearOritention(linearDividerOritention);
    }

    private Drawable createDividerLineDrawable(int dec)
    {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(dividerColor);
        if(dec != LINEAR_H && dec != LINEAR_V)
            throw new IllegalArgumentException("Invalid orientation, only LinearLayoutDivider.LINEAR_H and LinearLayoutDivider.LINEAR_V aviable");
        if(dec == LINEAR_H)
        {
            drawable.setSize(thickness, 0);
        }else
        {
            drawable.setSize(0, thickness);
        }
        return drawable;
    }

    /***
     * 设置分割线颜色厚度，仅用于在构造函数里面dividerDrawable参数传null的情况
     * @param thickness 厚度
     * @param color     颜色
     */
    public void setDividerStyle(int thickness, int color)
    {
        this.thickness = thickness;
        dividerColor = color;
        mDivider = createDividerLineDrawable(linearDividerOritention);
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    private void setLinearOritention(int linearDividerOritention)
    {
        if(linearDividerOritention != LINEAR_H && linearDividerOritention != LINEAR_V)
            throw new IllegalArgumentException("invalid orientation");
        this.linearDividerOritention = linearDividerOritention;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        int p = parent.getChildLayoutPosition(view);
        RecyclerView.Adapter a = parent.getAdapter();
        if(a != null)
        {
            int count = a.getItemCount();
            if(count-1 == p)
                return;
        }
        if (linearDividerOritention == LINEAR_V)
        {
            thickness = mDivider.getIntrinsicHeight();
            thickness = thickness == -1 ? 1 : thickness;
            outRect.bottom = thickness;
        } else
        {
            int w = mDivider.getIntrinsicWidth();
            w = w == -1 ? 1 : w;
            outRect.right = w;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state)
    {
        if (linearDividerOritention == LINEAR_V)
        {
            drawVertical(c, parent);
        } else
        {
            drawHorizontal(c, parent);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent)
    {
        final int top = parent.getPaddingTop() + offset;
        final int bottom = parent.getHeight() - parent.getPaddingBottom() - offset;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount-1; i++)
        {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent)
    {
        final int left = parent.getPaddingLeft() + offset;
        final int right = parent.getWidth() - parent.getPaddingRight() - offset;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount-1; i++)
        {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            thickness = mDivider.getIntrinsicHeight();
            if(thickness == -1)
                thickness = 1;
            mDivider.setBounds(left, top, right, top + thickness);
            mDivider.draw(c);
        }
    }
}
