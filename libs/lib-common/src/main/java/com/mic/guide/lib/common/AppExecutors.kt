package com.mic.guide.lib.common

import android.os.Handler
import android.os.Looper
import android.os.Process
import java.lang.reflect.Method
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 线程/执行器门面（合并自 libcore 的 `ThreadUtils` + `TaskExecutors` + `ExecutorsPoller`
 * + `ThreadSchedulerUtil`，转 Kotlin）。
 *
 * 协程优先用 [AppDispatchers]；需要裸 Executor/Handler（如对接回调式旧 SDK）时用本类。
 */
object AppExecutors {

    /** 主线程 Handler。 */
    val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }

    /** IO 线程池：按 CPU 核数固定大小。 */
    private val ioExecutor: Executor by lazy {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
        Executors.newFixedThreadPool(cores, object : ThreadFactory {
            private val index = AtomicInteger(1)
            override fun newThread(r: Runnable) =
                Thread(r, "aiguide_io_${index.getAndIncrement()}")
        })
    }

    /** 定时轮询线程池。 */
    private val scheduledExecutor: ScheduledExecutorService by lazy {
        Executors.newScheduledThreadPool(2)
    }

    private val pollingTasks = mutableListOf<TimerTask>()

    /** 是否在主线程。 */
    @JvmStatic
    fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    /** 提交 IO 任务。 */
    @JvmStatic
    fun io(task: Runnable) = ioExecutor.execute(task)

    /** 切回主线程执行（已在主线程则立即执行）。 */
    @JvmStatic
    fun main(task: Runnable) {
        if (isMainThread()) task.run() else mainHandler.post(task)
    }

    @JvmStatic
    fun post(task: Runnable) = mainHandler.post(task)

    @JvmStatic
    fun postDelayed(task: Runnable, delayMillis: Long) {
        mainHandler.postDelayed(task, delayMillis)
    }

    @JvmStatic
    fun removeCallback(task: Runnable) = mainHandler.removeCallbacks(task)

    /** 固定频率轮询（默认 3s）。 */
    @JvmStatic
    @JvmOverloads
    fun poll(task: TimerTask, initialDelayMillis: Long = 3000, periodMillis: Long = 3000) {
        pollingTasks.add(task)
        scheduledExecutor.scheduleAtFixedRate(task, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS)
    }

    @JvmStatic
    fun shutdownPolling() {
        pollingTasks.clear()
        scheduledExecutor.shutdown()
    }

    /** 反射调用隐藏 API 设置线程调度策略（实时音频等场景，失败返回 false）。 */
    @JvmStatic
    fun setThreadScheduler(pid: Int, policy: Int, priority: Int): Boolean = try {
        @Suppress("DiscouragedPrivateApi")
        val m: Method = Process::class.java.getDeclaredMethod(
            "setThreadScheduler", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        )
        m.isAccessible = true
        m.invoke(null, pid, policy, priority)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    @JvmStatic
    fun setThreadGroupAndCpuset(tid: Int, group: Int): Boolean = try {
        @Suppress("DiscouragedPrivateApi")
        val m: Method = Process::class.java.getDeclaredMethod(
            "setThreadGroupAndCpuset", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        )
        m.isAccessible = true
        m.invoke(null, tid, group)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
