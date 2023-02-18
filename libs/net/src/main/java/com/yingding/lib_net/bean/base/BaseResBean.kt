package com.yingding.lib_net.bean.base

import androidx.annotation.Keep

@Keep
class BaseResBean<T> {
    /**
     * code :    0:成功  其他失败
     * message : 错误信息  分布
     * result : 返回值
     */
    var code: Int = 0
    var msg: String? = null
    var data: T? = null

    constructor(code: Int, msg: String?) {
        this.code = code
        this.msg = msg
    }

    constructor()

    override fun toString(): String {
        return "BaseResBean(code=$code, msg=$msg, result=$data)"
    }
}
