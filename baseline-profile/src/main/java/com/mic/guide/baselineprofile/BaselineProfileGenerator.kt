package com.mic.guide.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 生成 Baseline Profile：记录冷启动 + 首屏交互的热点代码路径，打包进 APK 后由 ART 预编译，
 * 显著降低首启耗时与卡顿。运行：`./gradlew :app:generateBaselineProfile`（需设备/受管模拟器）。
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = TARGET_PACKAGE) {
        pressHome()
        startActivityAndWait()
        // 可在此补充滚动/切 tab 等关键交互，让 Profile 覆盖更多路径
        device.waitForIdle()
    }
}

internal const val TARGET_PACKAGE = "com.mic.guide"
