package com.mic.guide.support.network.model

/** 网络层业务异常：携带后端 code 与 message，便于上层统一处理。 */
class NetworkException(
    val code: Int,
    override val message: String,
) : RuntimeException(message)
