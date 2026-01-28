package com.mic.autolog.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoLogPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            // 只在 debug 变体插桩（你也可以改成由开关控制）
            val isDebug = variant.buildType == "debug"
            if (!isDebug) return@onVariants

            // 计算栈帧：建议使用官方推荐模式，减少 frame 问题
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )

            // 注册 ASM visitor
            variant.instrumentation.transformClassesWith(
                AutoLogClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) { params ->
                // 可配置：只插你自己的包
                params.enabled.set(true)
                params.includePackages.set(listOf("com.example")) // 改成你的业务包前缀
                params.excludePackages.set(
                    listOf(
                        "com.example.autolog.runtime" // 防止 AutoLog 自己被插桩递归
                    )
                )

                // 堆栈是否在 enter 时记录（强烈建议默认 false）
                params.logStack.set(false)

                // 是否记录 enter/exit
                params.logEnter.set(true)
                params.logExit.set(true)
                params.logError.set(true)
            }
        }
    }
}