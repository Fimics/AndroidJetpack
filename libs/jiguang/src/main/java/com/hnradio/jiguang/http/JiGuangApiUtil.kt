package com.hnradio.jiguang.http

import com.google.gson.Gson
import com.hnradio.common.http.bean.UserInfo
import com.hnradio.jiguang.http.bean.JGLoginBean
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetrofitRequest
import com.yingding.lib_net.http.RetrofitResultListener
import com.yingding.lib_net.http.RetrofitUtil
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.http
 * @ClassName: CommonApiUtil
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021/7/20 6:14 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/20 6:14 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class JiGuangApiUtil {
    companion object {
        private var service =
            RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, JiGuangService::class.java)

        fun login(
            jsonObject: Any,
            retrofitInterface: RetrofitResultListener<BaseResBean<JGLoginBean>>
        ): Disposable? {
            val str = Gson().toJson(jsonObject)
            val body: RequestBody =
                RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    str
                )
            return RetrofitRequest.request(service.login(body), retrofitInterface)
        }

        fun getUserInfo(
            retrofitInterface: RetrofitResultListener<BaseResBean<UserInfo>>
        ): Disposable? {
            return RetrofitRequest.request(service.getUserInfo(), retrofitInterface)
        }

    }
}