package com.hnradio.common.ktx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

inline fun <reified T : Activity> Activity.act(
    flags: Int? = null,
    extra: Bundle? = null,
    vararg params: Pair<String, Any> = arrayOf()
) {
    startActivity(buildIt<T>(flags, extra, *params))
}

inline fun <reified T : Activity> Fragment.act(
    flags: Int? = null,
    extra: Bundle? = null, vararg params: Pair<String, Any> = arrayOf()){
    activity?.let {
        startActivity(it.buildIt<T>(flags, extra, *params))
    }
}

inline fun <reified T : Activity> Context.act(
    flags: Int? = null,
    extra: Bundle? = null,
    vararg params: Pair<String, Any> = arrayOf()) {
    startActivity(buildIt<T>(flags, extra, *params))
}

inline fun <reified T : Activity> Activity.acts(
    requestCode: Int,
    flags: Int? = null,
    extra: Bundle? = null,
    vararg params: Pair<String, Any> = arrayOf()) {
    startActivityForResult(buildIt<T>(flags, extra, *params), requestCode)
}

inline fun <reified T : Activity> Fragment.acts(
    requestCode: Int,
    flags: Int? = null,
    extra: Bundle? = null,
    vararg params: Pair<String, Any> = arrayOf()) =
    activity?.let {
        startActivityForResult(it.buildIt<T>(flags, extra, *params), requestCode)
    }

inline fun <reified T : Context> Context.buildIt(
    flags: Int? = null,
    extra: Bundle? = null,
    vararg pairs: Pair<String, Any> = arrayOf()
): Intent = Intent(this, T::class.java).apply {
        flags?.let { setFlags(flags) }
        extra?.let { putExtras(extra) }
        if(pairs.isNotEmpty()){
           pairs.forEach { pair->
               val name = pair.first
               when (val value = pair.second) {
                   is Int -> putExtra(name, value)
                   is Byte -> putExtra(name, value)
                   is Char -> putExtra(name, value)
                   is Short -> putExtra(name, value)
                   is Boolean -> putExtra(name, value)
                   is Long -> putExtra(name, value)
                   is Float -> putExtra(name, value)
                   is Double -> putExtra(name, value)
                   is String -> putExtra(name, value)
                   is CharSequence -> putExtra(name, value)
                   is Parcelable -> putExtra(name, value)
                   is Array<*> -> putExtra(name, value)
                   is ArrayList<*> -> putExtra(name, value)
                   is Serializable -> putExtra(name, value)
                   is BooleanArray -> putExtra(name, value)
                   is ByteArray -> putExtra(name, value)
                   is ShortArray -> putExtra(name, value)
                   is CharArray -> putExtra(name, value)
                   is IntArray -> putExtra(name, value)
                   is LongArray -> putExtra(name, value)
                   is FloatArray -> putExtra(name, value)
                   is DoubleArray -> putExtra(name, value)
                   is Bundle -> putExtra(name, value)
                   is Intent -> putExtra(name, value)
                   else -> { }
               }
           }
        }
    }