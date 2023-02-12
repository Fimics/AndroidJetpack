package com.mic.jetpack.http.client

/**
 * 顶层的回调接口   string---->json,xml,protobuff
 */
interface ICallback {
    fun onSuccess(result: String?)
    fun onFailure(e: String?)
}