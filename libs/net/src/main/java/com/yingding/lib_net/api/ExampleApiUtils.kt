package com.yingding.lib_net.api

import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetrofitRequest
import com.yingding.lib_net.http.RetrofitResultListener
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.http.RetrofitUtil
import io.reactivex.disposables.Disposable


class ExampleApiUtils : RetrofitRequest() {
    companion object {
        private var exampleService =
            RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, ExampleService::class.java)

        fun doExampleGet(retrofitInterface: RetrofitResultListener<BaseResBean<List<String>>>): Disposable? {
            return request(exampleService.doExampleGet(), retrofitInterface)
        }
    }
}
