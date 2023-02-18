package com.hnradio.common.util.pay

import android.app.Activity
import com.alipay.sdk.app.PayTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-27 15:44
 * @Version: 1.0
 */
class ThirdPayUtils(val activity: Activity) {
    suspend fun AliPay(payInfo: String) = withContext(Dispatchers.IO) {
        supervisorScope {
            val task = PayTask(activity)
            val result = task.payV2(payInfo, true)
            PayResult(result)
        }
    }

    fun WXPay(payReq: com.hnradio.common.util.pay.PayReq) {
        val api = WXAPIFactory.createWXAPI(activity, payReq.appId)
        val req = PayReq()
        req.appId = payReq.appId
        req.partnerId = payReq.partnerId
        req.prepayId = payReq.prepayId
        req.nonceStr = payReq.nonceStr
        req.timeStamp = payReq.timeStamp
        req.packageValue = payReq.packageValue
        req.sign = payReq.sign
        req.extData = payReq.extData
        api.sendReq(req)
    }
}