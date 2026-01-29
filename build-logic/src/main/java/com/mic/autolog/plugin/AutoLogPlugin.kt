package com.mic.autolog.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoLogPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // ⚠️ 只对 Android 模块生效；避免应用到非 Android module 时直接抛异常
        val androidComponents = project.extensions.findByType(AndroidComponentsExtension::class.java)
            ?: run {
                // 不是 Android 模块就跳过
                return
            }

        androidComponents.onVariants { variant ->
            println("✅ AutoLog enabled: module=${project.path}, variant=${variant.name}")

            // 只对 Debug 变体插桩
            val isDebug = variant.name.endsWith("Debug", ignoreCase = true)
            if (!isDebug) return@onVariants

            // 计算栈帧（减少 frame 校验问题）
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )

            // 注册 ASM visitor
            variant.instrumentation.transformClassesWith(
                AutoLogClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) { params ->
                params.enabled.set(true)

                // ✅ 你的业务包前缀（非常关键：否则 isInstrumentable 会全部 false）
                params.includePackages.set(listOf("com.mic"))

                // ✅ 排除 runtime，避免 AutoLog 自己被插桩导致递归/栈溢出
                params.excludePackages.set(
                    listOf(
                        "com.mic.log.runtime",      // runtime
                        "com.mic.apppy.databinding",// databinding 生成类包
                        "com.mic.apppy.BR",         // databinding BR
                        "com.mic.apppy.DataBindingTriggerClass"
                    )
                )


                // 堆栈是否在 enter 时记录（强烈建议默认 false）
                params.logStack.set(false)

                // 是否记录 enter/exit/error
                params.logEnter.set(true)
                params.logExit.set(true)
                params.logError.set(true)
            }
        }
    }
}
