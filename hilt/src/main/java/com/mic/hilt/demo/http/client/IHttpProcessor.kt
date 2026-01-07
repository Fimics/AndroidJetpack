package com.mic.hilt.demo.http.client

/**
 * 房产公司
 */
interface IHttpProcessor {
    //网络访问的能力
    fun post(url: String?, params: Map<String?, Any?>?, callback: ICallback?) //....
}