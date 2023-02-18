package com.yingding.lib_net.http

/**
 * Created by Chuyh on 2017/12/5.
 */
class HttpException(message: String, val code: Int) : RuntimeException(message)
