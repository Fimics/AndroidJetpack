package com.hnradio.common.util

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment


/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: ResourceUtil
 * @Description: 资源辅助类
 * @Author: shaoguotong
 * @CreateDate: 2021/7/25 6:42 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/25 6:42 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

fun Activity.getMyColor(@ColorRes color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(color, theme)
    } else {
        resources.getColor(color)
    }
}

fun Fragment.getMyColor(@ColorRes color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(color, activity?.theme)
    } else {
        resources.getColor(color)
    }
}

/**通过名字获取资源id*/
fun getResourceIdByName(name: String, defType: String = "drawable"): Int  {
    val ctx: Context = Global.application
    //如果没有在"mipmap"下找到imageName,将会返回0
    return ctx.resources.getIdentifier(name, defType, ctx.packageName)
}

