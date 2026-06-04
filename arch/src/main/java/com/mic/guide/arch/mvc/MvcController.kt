package com.mic.guide.arch.mvc

import android.content.Context

/**
 * MVC 控制器基类。承载页面的数据/业务处理，由 Activity（View）持有并调用，
 * 让 Activity 专注于视图，避免膨胀。
 */
abstract class MvcController(protected val context: Context) {

    /** 控制器创建时回调。 */
    open fun onCreate() {}

    /** 控制器销毁时回调，释放资源。 */
    open fun onDestroy() {}
}
