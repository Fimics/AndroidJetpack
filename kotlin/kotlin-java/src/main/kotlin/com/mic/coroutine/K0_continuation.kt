package com.mic.coroutine

import java.util.concurrent.Executors
import kotlin.coroutines.*

fun main() {
    suspend {
        println("coroutine start")
        val value = suspendFunction1()
        println(value)
        println("suspendFunction----执行之后")
        "1"
    }.startCoroutine(object : Continuation<Any?> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Any?>) {
            println(result.getOrNull())
        }

    })
}
suspend fun suspendFunction1() = suspendCoroutine<Int> { continuation ->
    val threadGroup = ThreadGroup("thread-suspend-1")
    val threadExecutor = Executors.newSingleThreadExecutor {
        Thread(threadGroup, it, "thread")
    }
    threadExecutor.submit {
        Thread.sleep(2000)
        continuation.resume(0)
    }
}
//执行结果
//09:05:44:934 [main] coroutine start
//09:05:46:960 [thread] 0
//09:05:46:961 [thread] suspendFunction----执行之后
//09:05:46:961 [thread] 1
