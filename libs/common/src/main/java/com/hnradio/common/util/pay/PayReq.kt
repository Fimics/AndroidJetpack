package com.hnradio.common.util.pay

/**
 *
 * @Description: 微信支付参数
 * @Author: huqiang
 * @CreateDate: 2021-07-28 16:33
 * @Version: 1.0
 */
data class PayReq(
    val appId: String,
    val partnerId: String,
    val prepayId: String,
    val nonceStr: String,
    val timeStamp: String,
    val packageValue: String,
    val sign: String,
    val extData: String,
    val orderString: String, //支付宝
    val mchBillNo: String //订单号
)
