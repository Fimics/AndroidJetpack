package com.noetix.robotics.demo.explanation.data

import android.content.Context
import com.google.gson.Gson
import com.noetix.libcore.utils.KLog
import java.io.InputStreamReader

/**
 * 智能讲解本地 Mock 数据加载器
 */
object ExplainMockDataSource {
    private const val TAG = "ExplainMockDataSource"
    private var mockData: ExplainMockData? = null

    /**
     * 从 assets 加载并解析 JSON 数据
     */
    fun loadData(context: Context): ExplainMockData {
        if (mockData != null) return mockData!!
        
        try {
            context.assets.open("mock/explain_data.json").use { inputStream ->
                InputStreamReader(inputStream, "UTF-8").use { reader ->
                    mockData = Gson().fromJson(reader, ExplainMockData::class.java)
                }
            }
            KLog.d(TAG, "Mock数据加载成功, 包含 ${mockData?.explainRoutes?.size} 条路线")
        } catch (e: Exception) {
            KLog.e(TAG, "加载 Mock 数据失败: ${e.message}")
            e.printStackTrace()
            // 如果加载失败，返回一个空的包装对象以防崩溃
            mockData = ExplainMockData()
        }
        return mockData!!
    }

    /** 获取所有路线 */
    fun getAllRoutes(): List<ExplainRoute> {
        return mockData?.explainRoutes ?: emptyList()
    }

    /** 获取指定讲解路线 */
    fun getRouteById(routeId: Long): ExplainRoute? {
        return mockData?.explainRoutes?.find { it.groupId == routeId }
    }

    /** 获取指定虚拟点位（包含所有讲解内容） */
    fun getVirtualPoint(pointId: Long): VirtualPoint? {
        return mockData?.virtualPoints?.find { it.pointId == pointId }
    }

    /** 获取真实点位对应的地图坐标信息 */
    fun getMapPoint(slocName: String): MapPoint? {
        return mockData?.slocList?.find { it.slocName == slocName }
    }
}
