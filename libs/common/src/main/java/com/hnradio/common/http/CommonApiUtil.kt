package com.hnradio.common.http

import com.hnradio.common.http.bean.*
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetroFitResultFailListener
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
 * @Author: shaoguotong
 * @CreateDate: 2021/7/20 6:14 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/20 6:14 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class CommonApiUtil {
    companion object {
        private var service =
            RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, CommonService::class.java)

        /**
         * 获取阿里 oss配置
         */
        fun getAliOSSConfig(retrofitInterface: RetrofitResultListener<BaseResBean<AliOSSConfigBean>>): Disposable? {
            return RetrofitRequest.request(service.getAliOSSConfig(), retrofitInterface)
        }

        /**
         * 获取mqtt 配置
         */
        fun getMqttConfig(
            retrofitInterface: RetrofitResultListener<BaseResBean<MqttConfigBean>>,
            failListener: RetroFitResultFailListener
        ): Disposable? {
            return RetrofitRequest.request(
                service.getMqttConfig(BuildConfig.  LiveApiUrl + urlMqttConfig),
                retrofitInterface,
                failListener
            )
        }

        /**
         * 获取mqtt 配置
         */
        fun mqttSendMessage(
            param: String,
            retrofitInterface: RetrofitResultListener<BaseResBean<String>>,
        ): Disposable? {
            val body = RequestBody.create(MediaType.parse("application/json"), param)
            return RetrofitRequest.request(
                service.mqttSendMessage(
                    BuildConfig.LiveApiUrl + urlMqttSendMessage,
                    body
                ), retrofitInterface
            )
        }

        /**
         * 获取OSS服务中的  图片宽高
         */
        fun getOSSImageInfo(
            imageUrl: String,
            retrofitInterface: RetrofitResultListener<ImageInfoBean>,
            failListener: RetroFitResultFailListener
        ): Disposable? {
            return RetrofitRequest.request(
                service.getOSSImageInfo("${imageUrl}?x-oss-process=image/info"),
                retrofitInterface,
                failListener,
                false
            )
        }

        /**
         * 检查版本
         */
        fun checkAppVersion(
            retrofitInterface: RetrofitResultListener<BaseResBean<AppVersionBean>>,
            failListener: RetroFitResultFailListener
        ): Disposable? {
            return RetrofitRequest.request(
                service.checkVersion(0),
                retrofitInterface,
                failListener
            )
        }
    }
}