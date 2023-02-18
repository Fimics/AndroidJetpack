package com.qzinfo.commonlib.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable

/**
 * Created by ytf on 2016/08/10.
 * Description:
 */
object DrawableUtils {

    /***
     * 动态创建一个圆角矩形的shape
     * @param radiusPx 圆角半径
     * @param color    颜色
     * @return
     */
    fun createShape(radiusPx: Float, color: Int): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.let {
            drawable.cornerRadius = radiusPx
            drawable.setColor(color)
            return it
        }
    }

    /***
     * 创建一个圆角矩形，可自定义边角的半径
     * @param leftTopRadius         左上角半径
     * @param rightTopRadius        右上角半径
     * @param leftBottomRadius      左下角半径
     * @param rightBottomRadius     右下角半径
     * @param color                  颜色
     * @return
     */
    fun createRoundRectShape(
        leftTopRadius: Float, rightTopRadius: Float,
        leftBottomRadius: Float, rightBottomRadius: Float,
        color: Int, isStroke: Boolean, strokeWidth: Int
    ): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.mutate()
        drawable.cornerRadii = floatArrayOf(
            leftTopRadius,
            leftTopRadius,
            rightTopRadius,
            rightTopRadius,
            rightBottomRadius,
            rightBottomRadius,
            leftBottomRadius,
            leftBottomRadius
        )
        if (isStroke) {
            drawable.setStroke(strokeWidth, color)
        } else {
            drawable.setColor(color)
        }
        return drawable
    }

    /***
     * 创建一个圆角矩形，可自定义边角的半径
     * @param leftTopRadius         左上角半径
     * @param rightTopRadius        右上角半径
     * @param leftBottomRadius      左下角半径
     * @param rightBottomRadius     右下角半径
     * @param color                  颜色
     * @return
     */
    fun createRoundRectShape(
        leftTopRadius: Float, rightTopRadius: Float,
        leftBottomRadius: Float, rightBottomRadius: Float,
        color: Int, isStroke: Boolean, strokeColor: Int, strokeWidth: Int
    ): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.mutate()
        drawable.cornerRadii = floatArrayOf(
            leftTopRadius,
            leftTopRadius,
            rightTopRadius,
            rightTopRadius,
            rightBottomRadius,
            rightBottomRadius,
            leftBottomRadius,
            leftBottomRadius
        )
        drawable.setColor(color)
        if (isStroke) {
            drawable.setStroke(strokeWidth, strokeColor)
        }
        return drawable
    }

    /***
     * 动态创建创建选择器
     * @param activeState   激活是的drawable
     * @param normalState   未激活是的drawable
     * @return
     */
    fun createSelectorDrawable(activeState: Drawable, normalState: Drawable): StateListDrawable {
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_checked), activeState)
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), activeState)
        stateListDrawable.addState(intArrayOf(), normalState)
        stateListDrawable.setEnterFadeDuration(300)
        stateListDrawable.setExitFadeDuration(300)
        return stateListDrawable
    }

    /***
     * 动态创建颜色选择器
     * @param activeColor
     * @param normalColor
     * @return
     */
    fun createColorStateList(activeColor: Int, normalColor: Int): ColorStateList {
        val states = arrayOfNulls<IntArray>(3)
        states[0] = intArrayOf(android.R.attr.state_checked)
        states[1] = intArrayOf(android.R.attr.state_pressed)
        states[2] = intArrayOf()
        return ColorStateList(states, intArrayOf(activeColor, activeColor, normalColor))
    }

    /***
     * 从资源创建颜色选择器
     * @param context
     * @param resId
     * @return
     */
    fun createColorStateList(context: Context, resId: Int): ColorStateList {
        return context.resources.getColorStateList(resId)
    }

    /***
     * 从资源创建选择器
     * @param context
     * @param resId
     * @return
     */
    fun createSelectorDrawable(context: Context, resId: Int): StateListDrawable {
        return context.resources.getDrawable(resId) as StateListDrawable
    }

}
