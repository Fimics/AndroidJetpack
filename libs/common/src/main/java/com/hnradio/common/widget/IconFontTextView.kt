package com.hnradio.common.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView


/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.widget
 * @ClassName: IconFontTextView
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/7/5 10:22 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/5 10:22 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class IconFontTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        val iconfont = Typeface.createFromAsset(context.assets, "iconfont/iconfont.ttf")
        typeface = iconfont
    }
}