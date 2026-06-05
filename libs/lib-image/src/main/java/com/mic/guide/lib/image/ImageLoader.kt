package com.mic.guide.lib.image

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

/**
 * 图片加载门面：薄封装 Glide，业务层统一调 `ImageLoader.load(imageView, url)`，
 * 便于后续替换底层图片库（Coil/Fresco）而不改调用点。
 *
 * 默认带占位/失败底图（[R.drawable.ic_image_placeholder]）；可选圆角（[cornerRadiusDp]>0 时启用）。
 *
 * @param placeholderRes 加载中占位图；默认本库内置浅灰圆角底图
 * @param errorRes 加载失败兜底图；默认同占位图
 * @param cornerRadiusDp 圆角半径（dp），>0 时对加载结果做圆角变换
 */
object ImageLoader {

    fun load(
        imageView: ImageView,
        url: String?,
        placeholderRes: Int = R.drawable.ic_image_placeholder,
        errorRes: Int = R.drawable.ic_image_placeholder,
        cornerRadiusDp: Int = 0,
    ) {
        var request = Glide.with(imageView)
            .load(url)
            .placeholder(placeholderRes)
            .error(errorRes)
        if (cornerRadiusDp > 0) {
            val px = (cornerRadiusDp * imageView.resources.displayMetrics.density).toInt()
            request = request.transform(RoundedCorners(px))
        }
        request.into(imageView)
    }
}