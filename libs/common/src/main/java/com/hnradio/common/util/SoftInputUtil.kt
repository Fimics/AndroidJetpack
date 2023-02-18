package com.hnradio.common.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.orhanobut.logger.Logger

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: SoftInputUtil
 * @Description: 同一个ViewTree里的windowToken都是一致的，因此不一定要传入EditText，可以传入Button等，只要属于同一个ViewTree即可。
 * @Author: shaoguotong
 * @CreateDate: 2021/8/14 12:01 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/14 12:01 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

typealias SoftCallBack = (Boolean, Int) -> Unit

@SuppressLint("StaticFieldLeak")
object SoftInputUtil {

    //显示键盘
    fun showSoftInput(view: View?) {
        if (view == null) return
        val inputMethodManager: InputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
    }

    //隐藏键盘
    fun hideSoftInput(view: View?) {
        if (view == null) return
        val inputMethodManager: InputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    var callBack: SoftCallBack? = null
    var rootView: View? = null
    var rootViewVisibleHeight = 0

    //注册监听activity键盘弹出消失
    fun registerSoftKeyBoardListener(
        activity: Activity,
        callBack: SoftCallBack
    ) {
        this.callBack = callBack
        //获取activity的根视图
        rootView = activity.window.decorView
        val firstRect = Rect()
        rootView?.getWindowVisibleDisplayFrame(firstRect)
        rootViewVisibleHeight = firstRect.height()
        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(globalListener)
    }

    //注册监听activity键盘弹出消失
    fun registerSoftKeyBoardListener(
        view: View,
        callBack: SoftCallBack
    ) {
        this.callBack = callBack
        //获取activity的根视图
        rootView = view
        val firstRect = Rect()
        rootView?.getWindowVisibleDisplayFrame(firstRect)
        rootViewVisibleHeight = firstRect.height()
        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(globalListener)
    }

    private val globalListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            //获取当前根视图在屏幕上显示的大小
            val r = Rect()
            rootView?.getWindowVisibleDisplayFrame(r)

            Logger.e("布局变化：$r")
            val visibleHeight = r.height()
            if (rootViewVisibleHeight == 0) {
                rootViewVisibleHeight = visibleHeight
                return
            }

            //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
            if (rootViewVisibleHeight == visibleHeight) {
                return
            }

            //根视图显示高度变小超过200，可以看作软键盘显示了
            if (rootViewVisibleHeight - visibleHeight > 200) {
                callBack?.let {
                    it(true, rootViewVisibleHeight - visibleHeight)
                }
                rootViewVisibleHeight = visibleHeight
                return
            }

            //根视图显示高度变大超过200，可以看作软键盘隐藏了
            if (visibleHeight - rootViewVisibleHeight > 200) {
                callBack?.let {
                    it(false, rootViewVisibleHeight - visibleHeight)
                }
                rootViewVisibleHeight = visibleHeight
            }
        }

    }

    fun unregisterSoftKeyBoardListener(view: View) {
        callBack = null
        rootView = null
        view.viewTreeObserver.removeOnGlobalLayoutListener(globalListener)
    }
}