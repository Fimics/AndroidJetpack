package com.mic.guide.support.python

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.mic.guide.lib.log.Logger

/**
 * 内嵌 Python 执行门面（§2.2 脚本能力下沉 support）：封装 Chaquopy。
 *
 * 用法：
 * 1. 进程启动调一次 [init]（幂等）；
 * 2. [call] 调 `src/main/python/<module>.py` 里的函数；[runCode] 直接执行一段 Python 源码。
 *
 * Python 调用是同步阻塞的，重负载请放协程 `Dispatchers.Default` 执行（见 [callAsync]）。
 */
object PythonEngine {

    /** 启动 Python 运行时（幂等，必须在任何 [call] 前调用一次，通常在 Application/Component.onCreate）。 */
    fun init(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
            Logger.d("python runtime started", tag = "Python")
        }
    }

    private fun instance(): Python = Python.getInstance()

    /** 调用某模块的函数并把返回值转成字符串。 */
    fun call(module: String, function: String, vararg args: Any?): String =
        callRaw(module, function, *args).toString()

    /** 调用某模块的函数，返回原始 [PyObject]（可进一步 `.toInt()` / `.asList()` 等）。 */
    fun callRaw(module: String, function: String, vararg args: Any?): PyObject {
        val py = instance()
        return py.getModule(module).callAttr(function, *args)
    }

    /**
     * 直接执行一段 Python 源码，返回名为 [resultVar] 的全局变量值的字符串表示。
     * 例：`runCode("result = 6 * 7")` → "42"。
     */
    fun runCode(code: String, resultVar: String = "result"): String {
        val py = instance()
        val builtins = py.getBuiltins()
        val globals = py.getModule("__main__").get("__dict__")
        builtins.callAttr("exec", code, globals)
        return globals?.callAttr("get", resultVar)?.toString() ?: ""
    }
}
