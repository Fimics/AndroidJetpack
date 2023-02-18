package com.hnradio.common.constant

import com.yingding.lib_net.BuildConfig

/**
 * Created by liguangze on 2021/7/22.
 */
object UrlConstant {
    //用户服务协议
    const val USER_AGREEMENT = "${BuildConfig.ShareApiUrl}agreement.html"

    //用户隐私政策
    const val USER_SECRETA_GREEMENT = "${BuildConfig.ShareApiUrl}policy.html"

    // 用户充值协议
    const val USER_PROTOCOL = "${BuildConfig.ShareApiUrl}protocol.html"

    //联系我们
    const val USER_CONTACT = "${BuildConfig.ShareApiUrl}contact.html"

    //邀请好友
    const val SHRARE_INVITATION = "${BuildConfig.ShareApiUrl}download?inviteId="

    //直播间分享
    const val SHARE_LIVE_ROOM = "${BuildConfig.ShareApiUrl}liveroom?id="

    //宠粉首页
    const val MALL_HOME = "${BuildConfig.ShareApiUrl}tflive.html"

    //三方配置
    const val WX_APPID = "wxd98e9e44182b4efd"
    
    const val SINA_APPID = "3864013087"

    const val UMENG_APPID = "6164fd31ac9567566e92f225"

    const val UMENG_CHANNEL = "tfLife"
    //客服电话
    const val CUSTOMER_SERVICE_PHONE = "0731-85547259"
}