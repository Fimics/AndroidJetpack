package com.hnradio.common.http

import com.hnradio.common.http.bean.ReqBasePayBean
import com.hnradio.common.util.pay.PayReq
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetroFitResultFailListener
import com.yingding.lib_net.http.RetrofitRequest
import com.yingding.lib_net.http.RetrofitResultListener
import com.yingding.lib_net.http.RetrofitUtil
import io.reactivex.disposables.Disposable

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-24 15:44
 * @Version: 1.0
 */
object PayApiUtil {
    private var service =
        RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, CommonService::class.java)
    /**
     * 继续支付
     */
    fun orderPay(
        body: ReqBasePayBean,
        onSuccess: RetrofitResultListener<BaseResBean<PayReq>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.orderPay(body),
            onSuccess,
            onFailure
        )
    }
}