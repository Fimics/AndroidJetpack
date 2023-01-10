package com.mic.ui.flow

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

open class FlowLayout(context: Context?) : ViewGroup(context) {

    private val mHorizontalSpacing = dp2px(16)//每个item 横向间距
    private val mVerticalSpacing = dp2px(8)
    private var allLines:ArrayList<ArrayList<View>> = ArrayList()//记录所有的行，一行一行的存储用于layout
    private var lineHeights:ArrayList<Int> = ArrayList() //记录每一行的高

    constructor(context: Context?, attrs: AttributeSet?) : this(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context,null)

    private fun clearMeasureParams(){
        allLines.clear()
        lineHeights.clear()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //widthMeasureSpec 来自于父亲
        clearMeasureParams()

        val selfWidth = MeasureSpec.getSize(widthMeasureSpec)///ViewGroup解析的父亲给我的宽度
        val selfHeight = MeasureSpec.getSize(heightMeasureSpec)

        var parentNeededWidth = 0 // measure过程中，子View要求的父ViewGroup的宽
        var parentNeededHeight = 0 // measure过程中，子View要求的父ViewGroup的高


        var lineViews:ArrayList<View> = ArrayList()//保存一行中所有的View
        var lineWidthUsed=0 //记录这行已经使用了多宽
        var lineHeight =0//一行的高

        //先度量孩子  孩子的大小怎么来的？
        val childCount = childCount
        for (i in 0 until childCount){
            val childView = getChildAt(i)

            if (childView.visibility!=View.GONE){
                val childPParams = childView.layoutParams

                //把layoutParams 转变为measureSpec  paddingLeft为父亲的
                val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,paddingLeft+paddingRight,childPParams.width)
                val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,paddingTop+paddingBottom,childPParams.height)
                childView.measure(childWidthMeasureSpec,childHeightMeasureSpec)

                //获取子view的度量宽高
                val childMeasureWidth = childView.measuredWidth
                val childMeasureHeight = childView.measuredHeight

                //如果需要换行
                if (childMeasureWidth+lineWidthUsed+mHorizontalSpacing>selfWidth){
                    //一旦换行，我们就可以判断当前行需要的宽和高了，所以此时要记录下来
                    allLines.add(lineViews)
                    lineHeights.add(lineHeight)

                    parentNeededHeight+=lineHeight+mVerticalSpacing
                    parentNeededWidth= Math.max(parentNeededWidth,lineWidthUsed+mHorizontalSpacing)

                    //重置数据
                    lineViews = ArrayList()
                    lineWidthUsed=0
                    lineHeight=0
                }
                //view 是分行的layout的 所以要记录每一行有哪些View 这样可以方便layout布局
                lineViews.add(childView)
                //每行都会有自己的宽和高
                lineWidthUsed += childMeasureWidth + mHorizontalSpacing
                lineHeight=Math.max(lineHeight,childMeasureHeight)

                //处理最后一行数据
                if (i == childCount - 1) {
                    allLines.add(lineViews);
                    lineHeights.add(lineHeight);
                    parentNeededHeight += lineHeight + mVerticalSpacing;
                    parentNeededWidth = Math.max(parentNeededWidth, lineWidthUsed + mHorizontalSpacing);
                }
            }
        }

        //如果viewGroup 是match_parent 是受父亲限制的
        //如果viewGroup 是warp_patent 是父亲大小和孩子大小的限制，自己的大小不能一步计算出来，
        // 所以要先度量孩子的大小，然后再度量自己的大小 递归算法
        //后度量自己并保存 自己的大小怎么来的？？
        // 作为一个ViewGroup，它自己也是一个View,它的大小也需要根据它的父亲给它提供的宽高来度量
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val realWidth = if (widthMode == MeasureSpec.EXACTLY) selfWidth else parentNeededWidth
        val realHeight = if (heightMode == MeasureSpec.EXACTLY) selfHeight else parentNeededHeight
        setMeasuredDimension(realWidth, realHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val lineCount = allLines.size

        var curL = paddingLeft
        var curT = paddingTop

        for (i in 0 until lineCount) {
            val lineViews: List<View> = allLines[i]
            val lineHeight = lineHeights[i]
            for (j in lineViews.indices) {
                val view = lineViews[j]
                val left = curL
                val top = curT

//                int right = left + view.getWidth();???
//                int bottom = top + view.getHeight();
                val right = left + view.measuredWidth
                val bottom = top + view.measuredHeight
                view.layout(left, top, right, bottom)
                curL = right + mHorizontalSpacing
            }
            curT += lineHeight + mVerticalSpacing
            curL = paddingLeft
        }
    }

    companion object{
        private const val TAG = "flow"
        fun dp2px(dp:Int):Int{
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics).toInt()
        }
    }
}