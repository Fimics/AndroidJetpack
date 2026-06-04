package com.mic.guide.arch.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 所有 Repository 的基类，统一在 IO 线程执行并把异常包装为 [Result]，
 * 屏蔽业务层的 try/catch 与线程切换细节。
 */
abstract class BaseRepository {

    /**
     * 安全调用：在 IO 线程执行 [block]，成功返回 [Result.success]，失败返回 [Result.failure]。
     */
    protected suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
}
