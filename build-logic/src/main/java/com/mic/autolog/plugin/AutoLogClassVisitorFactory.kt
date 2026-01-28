package com.mic.autolog.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor

abstract class AutoLogClassVisitorFactory : AsmClassVisitorFactory<AutoLogParams> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        val p = parameters.get()
        if (!p.enabled.get()) return false

        val className = classData.className // e.g. com.example.Foo
        // 过滤 Android / Kotlin 生成物（你可按需增加）
        if (className.endsWith(".R") || className.contains(".R$")) return false
        if (className.endsWith(".BuildConfig")) return false
        if (className.startsWith("kotlin.")) return false
        if (className.startsWith("kotlinx.")) return false
        if (className.startsWith("androidx.")) return false
        if (className.startsWith("android.")) return false

        // include / exclude 包过滤
        val includes = p.includePackages.getOrElse(emptyList())
        val excludes = p.excludePackages.getOrElse(emptyList())

        val inInclude = includes.isEmpty() || includes.any { className.startsWith(it) }
        val inExclude = excludes.any { className.startsWith(it) }

        return inInclude && !inExclude
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return AutoLogClassVisitor(
            api = org.objectweb.asm.Opcodes.ASM9,
            next = nextClassVisitor,
            params = parameters.get()
        )
    }
}