package com.mic.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.View
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mic.R


/**
 * 调用 startActivity(DetailActivity)
 * T:Activity T继承Activity
 * 如果扩展函数的方法与系统的同名，优先使用系统的方法，所以这个方法不能调用
 */
inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(this, T::class.java))
}


inline fun <reified T : Activity> Activity.start() {
    startActivity(Intent(this, T::class.java))
}

// findViewById()
fun <T:View> Activity._View(@IdRes id:Int):T{
    return findViewById(id) as T
}

/**
 * snackbar  TODO No suitable parent found from the given view. Please provide a valid view.
 */

//inline fun Activity.snackbar(msg: String) =
//    Snackbar.make(findViewById(R.id.tv_sncakbar), msg, 1).show()
//
//inline fun Fragment.snackbar(msg: String) =
//    Snackbar.make(activity!!.findViewById(R.id.tv_sncakbar), msg, 1).show()
//
//inline fun View.snackbar(msg:String) {
//    val activity = context
//    if (activity is Activity) {
//        Snackbar.make(activity.findViewById(R.id.tv_sncakbar), msg, 1).show()
//    } else {
//        throw IllegalAccessException("View 必须要添加到Activity上")
//    }
//
//}

//fun Context.isConnected(): Boolean {
//    val mNetworkInfo = connectivityManager.activeNetworkInfo
//    if (mNetworkInfo != null) {
//        return mNetworkInfo.isAvailable
//    }
//    return false
//}
