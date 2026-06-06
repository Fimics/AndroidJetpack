package com.mic.guide.api.home

/** 首页对外的轻量信息（供其他模块展示角标/红点等，纯数据）。 */
data class HomeSummary(
    val unreadCount: Int,
    val latestTitle: String?,
)

/**
 * 首页能力接口（§6，与 `module-home` 对称）。
 *
 * 其他模块（如设置页要显示「首页有 N 条更新」）只依赖本接口、经 `ApiRegistry` 取实现，
 * 不依赖 `module-home`。实现类在 `module-home` 的 `HomeComponent.onCreate` 注册。
 */
interface HomeApi {

    /** 当前首页摘要（未读数 / 最新标题），无数据时 unreadCount=0。 */
    fun summary(): HomeSummary

    /** 请求首页后台刷新（不阻塞调用方）。 */
    fun refresh()
}
