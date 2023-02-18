package com.hnradio.common.util

import android.content.Intent
import android.os.Parcelable
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 *
 * @Description: Intent传值扩展
 * @Author: huqiang
 * @CreateDate: 2021-07-21 09:33
 * @Version: 1.0
 */
class IntentExtra(private val key: String? = null) {
    private val KProperty<*>.extraName: String
        get() = this@IntentExtra.key ?: name

    operator fun getValue(intent: Intent, property: KProperty<*>): Any? =
        intent.extras?.get(property.extraName)


    operator fun setValue(intent: Intent, property: KProperty<*>, any: Any?) {
        //简单添加几个常用类型,可以扩展添加其他类型
        when (any) {
            is String ->
                intent.putExtra(property.extraName, any)
            is Int ->
                intent.putExtra(property.extraName, any)
            is Boolean ->
                intent.putExtra(property.extraName, any)
            is Parcelable ->
                intent.putExtra(property.extraName, any)
            is Serializable ->
                intent.putExtra(property.extraName, any)


        }
    }
}