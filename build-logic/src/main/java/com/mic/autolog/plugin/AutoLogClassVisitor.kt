package com.mic.autolog.plugin

import org.objectweb.asm.ClassVisitor
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
        ownerInternalName = name
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

        // 不插 <clinit>
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
        private val AUTOLOG_TYPE: Type = Type.getType("Lcom/mic/log/runtime/AutoLog;")

        private val ENTER = Method("enter", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
        private val EXIT  = Method("exit",  "(Ljava/lang/String;Ljava/lang/String;J)V")

        private val SYS_TYPE: Type = Type.getType("Ljava/lang/System;")
        private val NANO_TIME = Method("nanoTime", "()J")
    }

    private var startNsLocal: Int = -1

    override fun onMethodEnter() {
        // long start = System.nanoTime()
        startNsLocal = newLocal(Type.LONG_TYPE)
        invokeStatic(SYS_TYPE, NANO_TIME)
        storeLocal(startNsLocal)

        if (params.logEnter.getOrElse(true)) {
            pushOwnerDot()
            visitLdcInsn(methodName)
            visitLdcInsn(methodDesc)
            invokeStatic(AUTOLOG_TYPE, ENTER)
        }
    }

    override fun onMethodExit(opcode: Int) {
        if (!params.logExit.getOrElse(true)) return
        if (opcode == ATHROW) return // 异常先不处理（稳定优先）

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

    private fun pushOwnerDot() {
        visitLdcInsn(ownerInternalName.replace('/', '.'))
    }
}
