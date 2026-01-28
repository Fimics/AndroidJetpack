package com.mic.libpy

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object PyBridge {

    // 确保只初始化一次
    private fun ensureStarted() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(AppContextHolder.app))
        }
    }

    fun hello(name: String): String {
        ensureStarted()
        val py = Python.getInstance()
        val module = py.getModule("greet")
        return module.callAttr("hello", name).toString()
    }

    fun add(a: Int, b: Int): Int {
        ensureStarted()
        val py = Python.getInstance()
        val module = py.getModule("mymath")
        return module.callAttr("add", a, b).toInt()
    }

    fun fib(n: Int): List<Int> {
        ensureStarted()
        val py = Python.getInstance()
        val module = py.getModule("mymath")
        val pyObj = module.callAttr("fib", n)
        // Chaquopy 会把 python list 转成可迭代对象
        return pyObj.asList().map { it.toInt() }
    }
}