package com.hnradio.common.ktx

import android.annotation.SuppressLint
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.DrawableRes

/**
 *
 * ProjectName:    sfm-android
 * Package:        cn.cbct.seefm.base.utils
 * Description:    TextView kotlin 扩展类
 * Author:         ZGCS
 * CreateDate:     2020/10/23 18:03
 * UpdateUser:     更新者：
 * UpdateDate:     2020/10/23 18:03
 * UpdateRemark:   更新说明：
 * Version:        1.0
 */

/**
 * 设置不同大小
 */
fun TextView.setDiffSizeString(content: String,
    bigSizePx: Int, littleSizePx: Int,
    bigStart: Int, bigEnd: Int,
    littleStart: Int, littleEnd: Int
) {
    val ss = SpannableString(content)
    ss.setSpan(
        AbsoluteSizeSpan(bigSizePx, false),
        bigStart,
        bigEnd,
        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
    )
    ss.setSpan(
        AbsoluteSizeSpan(littleSizePx, false),
        littleStart,
        littleEnd,
        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
    )
    text = ss
}

/**
 * 设置不同颜色值
 */
fun TextView.setSpanColorText(content: String,
                              selectStr: String,
                              color: Int) {
    if (TextUtils.isEmpty(content) || TextUtils.isEmpty(selectStr))
        return
    val spanString = SpannableStringBuilder(content)
    var index = 0
    val length = selectStr.length
    while (content.indexOf(selectStr, index) != -1) {
        index = content.indexOf(selectStr, index)
        spanString.setSpan(ForegroundColorSpan(color), index, index + length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        index += length - 1
    }
    text = spanString
}

fun TextView.setSpanColorText(content: String,
                              selectStr: List<String>,
                              color: Int) {
    if (TextUtils.isEmpty(content) || selectStr.isEmpty())
        return
    val spanString = SpannableStringBuilder(content)
    selectStr.forEach {
        var index = 0
        val length = it.length
        while (content.indexOf(it, index) != -1) {
            index = content.indexOf(it, index)
            spanString.setSpan(ForegroundColorSpan(color), index, index + length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            index += length - 1
        }
    }

    text = spanString
}

@SuppressLint("UseCompatLoadingForDrawables")
fun TextView.setTextDrawable(
        @DrawableRes res: Int,
        direction: Int
) {
    val drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        context.resources.getDrawable(res, null)
    else
        context.resources.getDrawable(res)
    drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
    when (direction) {
        0 -> this.setCompoundDrawables(drawable, null, null, null)
        1 -> this.setCompoundDrawables(null, drawable, null, null)
        2 -> this.setCompoundDrawables(null, null, drawable, null)
        else -> this.setCompoundDrawables(null, null, null, drawable)
    }
}