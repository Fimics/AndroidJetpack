package com.hnradio.common.base

import android.view.View
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.base
 * @ClassName: BaseViewBindViewHolder
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/8/15 9:31 上午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/15 9:31 上午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Suppress("UNCHECKED_CAST")
open class BaseViewBindViewHolder<VB : ViewBinding>(val view: View) : BaseViewHolder(view) {
    val viewBinding: VB

    init {
        val type: ParameterizedType = javaClass.genericSuperclass as ParameterizedType
        val bind: Method = (type.actualTypeArguments[0] as Class<*>)
            .getDeclaredMethod(
                "bind", View::class.java
            )
        viewBinding = bind.invoke(null, view) as VB
    }
}