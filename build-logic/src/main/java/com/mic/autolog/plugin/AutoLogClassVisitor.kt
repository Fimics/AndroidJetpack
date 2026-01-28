package com.mic.autolog.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

class AutoLogClassVisitor(
    api: Int,
    next: ClassVisitor,
    private val params: AutoLogParams
) : ClassVisitor(api, next) {

    private lateinit var ownerInternalName: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        ownerInternalName = name // e.g. com/example/Foo
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)

        // 过滤：<clinit> 静态初始化块不插
        if (name == "<clinit>") return mv

        // interface/abstract/native 不可插
        val isAbstract = (access and Opcodes.ACC_ABSTRACT) != 0
        val isNative = (access and Opcodes.ACC_NATIVE) != 0
        if (isAbstract || isNative) return mv

        return AutoLogMethodVisitor(
            api = api,
            mv = mv,
            access = access,
            methodName = name,
            methodDesc = descriptor,
            ownerInternalName = ownerInternalName,
            params = params
        )
    }
}

private class AutoLogMethodVisitor(
    api: Int,
    mv: MethodVisitor,
    access: Int,
    private val methodName: String,
    private val methodDesc: String,
    private val ownerInternalName: String,
    private val params: AutoLogParams
) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

    companion object {
        // ✅ 这里改成你真实的 runtime 包名：com.mic.log.runtime.AutoLog
        private val AUTOLOG_TYPE: Type = Type.getType("Lcom/mic/log/runtime/AutoLog;")

        private val ENTER = Method("enter", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
        private val EXIT  = Method("exit",  "(Ljava/lang/String;Ljava/lang/String;J)V")
        private val ERROR = Method("error", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V")

        private val SYS_TYPE: Type = Type.getType("Ljava/lang/System;")
        private val NANO_TIME = Method("nanoTime", "()J")
    }

    private var startNsLocal: Int = -1

    // try/catch(Throwable) 记录异常
    private val tryStart = Label()
    private val tryEnd = Label()
    private val catchHandler = Label()

    override fun visitCode() {
        visitTryCatchBlock(tryStart, tryEnd, catchHandler, "java/lang/Throwable")
        super.visitCode()
    }

    override fun onMethodEnter() {
        mark(tryStart)

        // long start = System.nanoTime()
        startNsLocal = newLocal(Type.LONG_TYPE)
        invokeStatic(SYS_TYPE, NANO_TIME)
        storeLocal(startNsLocal)

        if (params.logEnter.getOrElse(true)) {
            // AutoLog.enter(owner, name, desc)
            pushOwnerDot()
            visitLdcInsn(methodName)
            visitLdcInsn(methodDesc)
            invokeStatic(AUTOLOG_TYPE, ENTER)
        }
    }

    override fun onMethodExit(opcode: Int) {
        // 正常 return 都会走这里；异常我们交给 catchHandler
        if (!params.logExit.getOrElse(true)) return
        if (opcode == ATHROW) return

        // long cost = System.nanoTime() - start
        val costLocal = newLocal(Type.LONG_TYPE)
        invokeStatic(SYS_TYPE, NANO_TIME)
        loadLocal(startNsLocal)
        math(SUB, Type.LONG_TYPE)
        storeLocal(costLocal)

        // AutoLog.exit(owner, name, cost)
        pushOwnerDot()
        visitLdcInsn(methodName)
        loadLocal(costLocal)
        invokeStatic(AUTOLOG_TYPE, EXIT)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        mark(tryEnd)

        // catch(Throwable t)
        mark(catchHandler)
        val tLocal = newLocal(Type.getType("Ljava/lang/Throwable;"))
        storeLocal(tLocal)

        if (params.logError.getOrElse(true)) {
            // AutoLog.error(owner, name, t)
            pushOwnerDot()
            visitLdcInsn(methodName)
            loadLocal(tLocal)
            invokeStatic(AUTOLOG_TYPE, ERROR)
        }

        // 异常时也记录耗时（可选：你这里保持原逻辑）
        if (params.logExit.getOrElse(true)) {
            val costLocal = newLocal(Type.LONG_TYPE)
            invokeStatic(SYS_TYPE, NANO_TIME)
            loadLocal(startNsLocal)
            math(SUB, Type.LONG_TYPE)
            storeLocal(costLocal)

            pushOwnerDot()
            visitLdcInsn(methodName)
            loadLocal(costLocal)
            invokeStatic(AUTOLOG_TYPE, EXIT)
        }

        // throw t
        loadLocal(tLocal)
        throwException()

        super.visitMaxs(maxStack, maxLocals)
    }

    private fun pushOwnerDot() {
        // "com/example/Foo" -> "com.example.Foo"
        visitLdcInsn(ownerInternalName.replace('/', '.'))
    }
}
