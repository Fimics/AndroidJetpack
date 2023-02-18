package com.hnradio.jiguang

import android.content.Context
import cn.jiguang.analytics.android.api.JAnalyticsInterface
import cn.jiguang.share.android.api.JShareInterface
import cn.jiguang.share.android.api.PlatformConfig
import cn.jiguang.verifysdk.api.JVerificationInterface
import cn.jpush.android.api.JPushInterface
import com.hnradio.common.AppContext
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.util.ToastUtils
import com.hnradio.common.util.upgrade.XHttpUpdateHttpService
import com.umeng.commonsdk.UMConfigure
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.utils.UpdateUtils

/**
 *
 * @Description:
 * @Author: huqiang
 * @CreateDate: 2021-07-09 09:21
 * @Version: 1.0
 */

const val VERIFY_CONSISTENT: Int = 9000 //手机号验证一致
const val FETCH_TOKEN_SUCCESS = 2000 //获取token成功
const val CODE_LOGIN_SUCCESS = 6000
const val CODE_LOGIN_FAILED = 6001
const val CODE_LOGIN_CANCELD = 6002

class JGConfig {
    companion object {
        fun init(context: Context) {
            JVerificationInterface.setDebugMode(BuildConfig.DEBUG)
            JVerificationInterface.init(context)
            //统计
            JAnalyticsInterface.init(context)
            JAnalyticsInterface.setDebugMode(BuildConfig.DEBUG)
            //开启crashlog日志上报
            JAnalyticsInterface.initCrashHandler(context)

            JShareInterface.setDebugMode(BuildConfig.DEBUG)
            val platformConfig =
                PlatformConfig().setWechat(UrlConstant.WX_APPID, "bb5ec0f907c27a5639c94b84705d1ea8")
                    .setSinaWeibo(
                        "3864013087",
                        "caed9628612ce43b471956096ec556e2",
                        "https://api.weibo.com/oauth2/default.html"
                    )
            JShareInterface.init(context, platformConfig)

        }

        fun setPushAlias(context: Context, alias: String) {

            JPushInterface.setAlias(
                context,
                1,
                alias
            )
        }

        fun deleteAlias(context: Context) {
            JPushInterface.deleteAlias(
                context,
                1,
            )
        }

        fun setBadgeNumber(context: Context, num: Int) {
            JPushInterface.setBadgeNumber(context, num)
        }


    }
}