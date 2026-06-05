package com.mic.guide.support.router

import android.net.Uri
import androidx.navigation.NavController
import com.mic.guide.support.router.ext.navigateSafe

/**
 * 统一跳转门面（§5.4 / §5.7）：持有 [NavController]，把跨模块跳转收敛成类型化方法，
 * 内部走 deepLink + [navigateSafe] 兜底降级，并做基础的防重复点击。
 *
 * 调用方（业务模块）只依赖 support-router，不依赖目标业务模块，符合 §15 的零编译依赖约束。
 *
 * @param navController 宿主 NavController（由 `app` 的 Activity 注入）
 * @param onUnavailable 目标不可用时的降级回调（如 Toast / 打点 / 跳默认页）
 */
class AppNavigator(
    private val navController: NavController,
    private val onUnavailable: (Uri) -> Unit = {},
) {

    private var lastNavMillis = 0L

    /** 跨模块：打开聊天详情（目标在 module-chat，本类零依赖该模块）。 */
    fun toChatDetail(conversationId: String) = navigate(Routes.chatDetail(conversationId))

    /** 通用 deepLink 跳转：防抖 + 安全降级。 */
    fun navigate(uriString: String) {
        val now = System.currentTimeMillis()
        if (now - lastNavMillis < DEBOUNCE_MS) return   // 防重复点击
        lastNavMillis = now
        navController.navigateSafe(Uri.parse(uriString)) { uri, _ -> onUnavailable(uri) }
    }

    /** 返回上一页。 */
    fun back(): Boolean = navController.navigateUp()

    private companion object {
        const val DEBOUNCE_MS = 500L
    }
}
