package com.mic.guide.support.router

/**
 * 全局持有当前 [AppNavigator]，供业务层无侵入获取（§5.5 / §5.7 方式1）。
 *
 * 由 `app` 的入口 Activity 在 `onCreate` 注册、`onDestroy` 置空，避免泄漏 NavController/Activity。
 * 若已接入 Hilt，可改为注入 [AppNavigator] 替代该全局单例。
 */
object NavigatorProvider {

    @JvmStatic
    var navigator: AppNavigator? = null
}
