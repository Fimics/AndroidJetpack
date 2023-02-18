package com.hnradio.jiguang.http.bean

import androidx.annotation.Keep

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-20 20:00
 * @Version: 1.0
 */
@Keep
data class JGLoginBean(

    val needBind: Boolean,
    val token: String?,
    val thirdPartyKey: String?,
    var loginType: Int?,
)
