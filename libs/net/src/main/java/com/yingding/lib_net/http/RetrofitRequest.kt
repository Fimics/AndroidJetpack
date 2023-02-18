package com.yingding.lib_net.http

import com.yingding.lib_net.bean.base.BaseResBean
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

open class RetrofitRequest {
    companion object {

        /**
         * 发送请求
         * @param successInterface 请求返回结果，如果不自己处理错误信息，则返回结果为null后者code为fail
         * @param failInterface 请求服务器返回错误结果
         * @param isToast 错误信息统一处理时，是否弹Toast提示
         * @param failCommonInterface 错误信息统一处理，传null则不再统一处理
         * @return 返回订阅，在调用的地方统一管理该订阅
         */
        @JvmOverloads
        @JvmStatic
        fun <T> request(
            observable: Observable<T>?,
            successInterface: RetrofitResultListener<T>?,
            failInterface: RetroFitResultFailListener? = null,
            isToast: Boolean = true,
            failCommonInterface: NetExceptionInterface? = RetrofitUtil.instance.exceptionInterface,
            subscribeOn: Scheduler = Schedulers.io(),
            observeOn: Scheduler = AndroidSchedulers.mainThread()
        ): Disposable? {
            return observable?.subscribeOn(subscribeOn)?.observeOn(observeOn)
                ?.subscribe({
                    if (it is BaseResBean<*>) {
                        val baseResBean = it as BaseResBean<*>
                        if (baseResBean.code == HttpStatusCode.HttpSuccess.code) {
                            successInterface?.onResult(it)
                        } else {
                            //统一处理服务器返回的异常结果
                            failCommonInterface?.onCodeFail(
                                baseResBean.code,
                                baseResBean.msg,
                                isToast
                            )
                            //如果不自己处理错误，则返回服务器结果
                            failInterface?.onResultFail(it.msg)
                                ?: successInterface?.onResult(it)
                        }
                    } else
                        successInterface?.onResult(it)
                }, {
                    //统一处理异常信息
                    failCommonInterface?.onNetFail(it, isToast)
                    failInterface?.onResultFail(it.message) ?: successInterface?.onResult(null)
                })
        }
    }
}
