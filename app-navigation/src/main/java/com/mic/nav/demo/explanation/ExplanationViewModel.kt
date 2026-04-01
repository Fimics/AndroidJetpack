package com.noetix.robotics.demo.explanation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noetix.robotics.common.speech.SpeechHelper
import com.noetix.robotics.demo.explanation.data.ContentItem
import com.noetix.robotics.demo.explanation.data.ExplainExecuteState
import com.noetix.robotics.demo.explanation.data.ExplainMockDataSource
import com.noetix.robotics.demo.explanation.data.ExplainUiState
import com.noetix.robotics.demo.explanation.data.RealPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 单点讲解 ViewModel
 * 负责管理讲解点位的数据加载、内容播放状态机和 TTS 语音驱动。
 */
class ExplanationViewModel : ViewModel() {

    // --- UI 状态：驱动 Fragment 视图渲染 ---
    private val _uiState = MutableStateFlow<ExplainUiState>(ExplainUiState.Idle)
    val uiState: StateFlow<ExplainUiState> = _uiState

    // --- 执行状态：标记当前讲解流程所处阶段 ---
    private val _executeState = MutableStateFlow(ExplainExecuteState.IDLE)
    val executeState: StateFlow<ExplainExecuteState> = _executeState

    private var preachJob: Job? = null

    // --- 单点讲解播放状态 ---
    private var currentVirtualPointId: Long = 0
    private var currentContentItems = listOf<ContentItem>()
    private var currentContentIndex = 0
    private var currentPointName = ""

    // ==============================
    // 数据初始化
    // ==============================

    /** 加载 Mock 数据（在 Fragment onViewCreated 时调用） */
    fun loadMockData(context: Context) {
        ExplainMockDataSource.loadData(context)
    }

    /**
     * 获取所有可讲解点位列表，供 SinglePointFragment 展示选择。
     * 取第一条路线下的所有 RealPoint。
     */
    fun getPoints(): List<RealPoint> {
        return ExplainMockDataSource.getAllRoutes().firstOrNull()?.realPoints ?: emptyList()
    }

    // ==============================
    // 单点讲解控制
    // ==============================

    /**
     * 启动单点讲解。
     * @param slocId 目标点位的物理位置 ID
     */
    fun startSinglePoint(slocId: Long) {
        // 先停止可能正在进行的讲解
        stopPreach()

        preachJob = viewModelScope.launch {
            _executeState.value = ExplainExecuteState.EXPLAINING

            // 根据 slocId 查找对应的物理点位
            val realPoint = getPoints().find { it.slocId == slocId }
            if (realPoint == null) {
                _uiState.value = ExplainUiState.Error("找不到点位数据")
                return@launch
            }
            currentPointName = realPoint.slocName

            // 通过虚拟点位 ID 查找讲解内容
            val virtualPoint = ExplainMockDataSource.getVirtualPoint(realPoint.virtualSlocId)
            if (virtualPoint == null || virtualPoint.items.isEmpty()) {
                _uiState.value = ExplainUiState.Error("该点位无讲解内容")
                return@launch
            }

            // 初始化播放状态
            currentVirtualPointId = virtualPoint.pointId
            currentContentItems = virtualPoint.items
            currentContentIndex = 0

            // 进入内容播放状态机
            playCurrentContentFlow()
        }
    }

    /**
     * 停止当前讲解，重置状态到 Idle。
     */
    fun stopPreach() {
        cancelAllActions()
        SpeechHelper.stop()
        preachJob?.cancel()
        _executeState.value = ExplainExecuteState.FINISHED
        _uiState.value = ExplainUiState.Idle
    }

    /**
     * 暂停当前讲解。
     */
    fun pausePreach() {
        if (_executeState.value == ExplainExecuteState.EXPLAINING) {
            _uiState.value = ExplainUiState.Paused(_executeState.value)
            _executeState.value = ExplainExecuteState.PAUSED
            SpeechHelper.stop()
        }
    }

    /**
     * 恢复当前讲解。
     */
    fun resumePreach() {
        if (_executeState.value == ExplainExecuteState.PAUSED) {
            _executeState.value = ExplainExecuteState.EXPLAINING
            playCurrentContentFlow()
        }
    }

    // ==============================
    // 内容播放状态机（私有）
    // ==============================

    /**
     * 内容播放状态机核心方法：
     * 按顺序播放 currentContentItems 中的每一条内容，
     * 每条内容播放完毕后自动推进索引，直到全部完成。
     */
    private fun playCurrentContentFlow() {
        // 所有内容播放完毕 → 单点讲解结束
        if (currentContentIndex >= currentContentItems.size) {
            stopPreach()
            _uiState.value = ExplainUiState.Finished("单点讲解完成")
            return
        }

        val item = currentContentItems[currentContentIndex]

        // 更新 UI：展示当前内容
        _uiState.value = ExplainUiState.ShowContent(
            pointName = currentPointName,
            preachType = item.preachType,
            content = item.content,
            contentItem = item,
            contentIndex = currentContentIndex,
            totalContents = currentContentItems.size,
            pointIndex = 1,   // 单点讲解固定为第 1 个点
            totalPoints = 1   // 单点讲解固定共 1 个点
        )

        // 执行当前内容的前置动作（如机械臂姿态）
        if (item.actionCommandBefore.isNotEmpty()) {
            executeAction(item.actionCommandBefore)
        }

        val textToSpeak = item.content
        if (textToSpeak.isNotEmpty() && (item.preachType == 1 || item.preachType == 4)) {
            // 有语音文本 → 调用 TTS 驱动播放，完成后自动推进
            SpeechHelper.speak(textToSpeak, object : SpeechHelper.SpeechCallback {
                override fun onEnd(error: String?) {
                    if (_executeState.value == ExplainExecuteState.EXPLAINING) {
                        viewModelScope.launch {
                            delay(item.intervalTime)
                            // 执行后置动作
                            if (item.actionCommandAfter.isNotEmpty()) {
                                executeAction(item.actionCommandAfter)
                            }
                            currentContentIndex++
                            playCurrentContentFlow()
                        }
                    }
                }
            })
        } else {
            // 无语音文本（纯图片/视频类型）→ 固定延迟后自动推进
            viewModelScope.launch {
                delay(2000)
                currentContentIndex++
                playCurrentContentFlow()
            }
        }
    }

    // ==============================
    // SDK 预留接口（待硬件对接时填充实现）
    // ==============================

    /** 取消所有正在进行的底盘/机械臂动作 */
    private fun cancelAllActions() {}

    /** 执行动作指令，actionIds 为动作编号字符串（逗号分隔） */
    private fun executeAction(actionIds: String) {}
}
