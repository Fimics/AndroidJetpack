package com.noetix.robotics.demo.explanation.data

/**
 * 讲解执行状态
 * 对应旧项目的 ExecuteState
 */
enum class ExplainExecuteState {
    IDLE,               // 空闲
    WELCOME,            // 欢迎语
    GO_POINT,           // 前往点位
    ARRIVED,            // 到达点位
    EXPLAINING,         // 正在讲解
    PAUSED,             // 暂停
    INTERVAL_POINT,     // 点位间隔
    INTERVAL_CONTENT,   // 内容间隔
    INTERVAL_CYCLE,     // 循环间隔
    FINISHED            // 讲解结束
}

/**
 * UI 呈现状态
 */
sealed class ExplainUiState {
    object Idle : ExplainUiState()
    object Loading : ExplainUiState()
    data class RouteLoaded(val route: ExplainRoute, val points: List<RealPoint>) : ExplainUiState()
    data class GoingToPoint(val pointName: String, val pointIndex: Int, val total: Int) : ExplainUiState()
    data class ShowContent(
        val pointName: String,
        val preachType: Int,
        val content: String,
        val contentItem: ContentItem,
        val contentIndex: Int,
        val totalContents: Int,
        val pointIndex: Int,
        val totalPoints: Int
    ) : ExplainUiState()
    data class Paused(val previousState: ExplainExecuteState) : ExplainUiState()
    data class Finished(val message: String) : ExplainUiState()
    data class Error(val message: String) : ExplainUiState()
}
