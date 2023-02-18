package com.yingding.lib_net.http

/**返回错误码*/
enum class HttpStatusCode(var code: Int, var dec: String) {
    HttpSuccess(200, "成功"),
    HttpTokenTimeOut(10000001, "token已过期"),
    HttpFail(0, "失败"),
}