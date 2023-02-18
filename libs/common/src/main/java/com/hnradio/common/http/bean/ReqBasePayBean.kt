package com.hnradio.common.http.bean

import androidx.annotation.Keep

/**
 * Created by liguangze on 2021/7/29.
 */
@Keep

data class ReqBasePayBean(
    val mchBillNo : String,
    val payChannel : Int,//0:微信，1:支付宝
)