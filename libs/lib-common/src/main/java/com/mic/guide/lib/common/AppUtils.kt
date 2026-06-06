package com.mic.guide.lib.common

import android.app.ActivityManager
import android.content.Context

/** 应用进程相关工具（迁移自 libcore `AppUtils`，转 Kotlin）。 */
object AppUtils {

    /** 当前应用是否在前台。 */
    @JvmStatic
    fun isAppOnForeground(context: Context?): Boolean {
        if (context == null) return false
        return try {
            val am = context.applicationContext
                .getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
            val packageName = context.applicationContext.packageName
            am.runningAppProcesses?.any {
                it.processName == packageName &&
                    it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
