package com.mic.guide.support.router.ext

import android.net.Uri
import androidx.navigation.NavController

/**
 * 安全跳转：deepLink 在当前导航图里无匹配目标时（如目标模块被拔掉 / 未集成），
 * NavController.navigate(Uri) 会抛 [IllegalArgumentException]——这里捕获并交给 [onFail] 降级，
 * 而不是让 App 崩溃。这正是组件可插拔（§15）所需的运行期兜底。
 */
inline fun NavController.navigateSafe(
    uri: Uri,
    onFail: (Uri, Throwable) -> Unit = { _, _ -> },
) {
    try {
        navigate(uri)
    } catch (e: IllegalArgumentException) {
        onFail(uri, e)
    }
}
