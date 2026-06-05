package com.mic.guide.support.network.model

/**
 * 统一后端响应包装：`code / message / data`。
 *
 * 供**自家后端**（有统一信封）的业务接口使用：`suspend fun xxx(): ApiResponse<T>`，
 * 在 Repository 里判 [isSuccess] 再取 [data]。本工程 home 示范接的是公共测试 API（无信封），
 * 故 `HomeApiService` 直接返回裸 DTO 列表，不经本类。
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
) {
    fun isSuccess(): Boolean = code == 0
}
