package com.noetix.robotics.demo.explanation.data

import com.google.gson.annotations.SerializedName

/** 讲解 Mock 数据根节点 */
data class ExplainMockData(
    val cleverExplain: CleverExplainConfig = CleverExplainConfig(),
    val virtualPoints: List<VirtualPoint> = emptyList(),
    val explainRoutes: List<ExplainRoute> = emptyList(),
    val slocList: List<MapPoint> = emptyList()
)

/** 讲解全局配置 */
data class CleverExplainConfig(
    val startBroadcast: String = "",
    val endBroadcast: String = "",
    val preachImgList: List<String> = emptyList(),
    val autoResumeTaskDuration: Long = 30
)

/** 讲解路线 */
data class ExplainRoute(
    val groupId: Long = 0,
    val groupName: String = "",
    val realPoints: List<RealPoint> = emptyList()
)

/** 路线中的点位引用 */
data class RealPoint(
    val slocId: Long = 0,
    val slocName: String = "",
    val virtualSlocId: Long = 0
)

/** 虚拟点位（讲解内容组） */
data class VirtualPoint(
    val pointId: Long = 0,
    val pointName: String = "",
    val intervalTime: Long = 0,
    val contentOnTheWay: String = "",
    val items: List<ContentItem> = emptyList()
)

/** 单条讲解内容 */
data class ContentItem(
    val itemId: Long = 0,
    val pointId: Long = 0,
    val preachType: Int = 1,       // 1=文字+图 2=音频+图 3=视频 4=纯文字 5=纯音频
    val content: String = "",
    val imgUrls: List<String> = emptyList(),
    val voiceUrl: String = "",
    val videoUrl: String = "",
    val intervalTime: Long = 0,
    val actionCommandBefore: String = "",
    val actionCommandAfter: String = ""
)

/** 地图点位坐标 */
data class MapPoint(
    val slocId: Long = 0,
    val slocName: String = "",
    val location: String = ""      // "x,y,z,弧度,楼层"
)
