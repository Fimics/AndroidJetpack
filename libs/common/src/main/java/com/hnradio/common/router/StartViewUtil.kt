package com.hnradio.common.router

import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.manager.UserManager
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 *  跳转工具类
 * created by qiaoyan on 2021/8/2
 */
class StartViewUtil {

    companion object {

        /**
         * 首页跳转到其他界面
         */
        fun startView(
            context: Context,
            linkType: Int,
            linkId: Int?,
            linkUrl: String?,
            linkAppId: String? = ""
        ) {
            when (linkType) {
                0 -> {//0-音频
                    linkId?.let { RouterUtil.gotoAudioPlayActivity(it) }
                }
                1 -> {//1-视频
                    linkId?.let {
                        RouterUtil.gotoVideoPlay(it)
                    }

                }
                2 -> {//2-图文
                    linkId?.let {
                        RouterUtil.gotoInfoDetailPlatformActivity(it)
                    }

                }
                3 -> {//3-专辑
                    linkId?.let {
                        RouterUtil.gotoAlbumActivity(it)
                    }

                }
                4 -> {//4-商品
                    linkId?.let {
                        RouterUtil.gotoGoodsDetailActivity(it)
                    }

                }
                5 -> {//5-报名
                    if (!UserManager.checkIsGotoLogin()) {
                        linkId?.let {
                            RouterUtil.gotoSignUp(it)
                        }

                    }
                }
                6 -> {//6-投票
                    linkId?.let {
                        RouterUtil.gotoVoteList(it)
                    }

                }
                7 -> {//7-直播间
                    linkId?.let {
                        RouterUtil.gotoLivePlay(it)
                    }
                }
                8 -> {//8-微主页
                    linkId?.let {
                        RouterUtil.gotoMicroHomepage(it)
                    }

                }
                9 -> {//9-H5链接
                    if (!linkUrl.isNullOrBlank()) {
                        RouterUtil.gotoWebViewActivity("", linkUrl)
                    }
                }
                10 -> {//10-小程序
                    if (!linkUrl.isNullOrBlank() && !linkAppId.isNullOrBlank()) {
                        OpenMiniProgram(context, linkAppId, linkUrl)
                    }
                }
                11 -> {//11-问答
                    RouterUtil.gotoQuestionAnswerHome()
                }
                12 -> {//12-为民热线
                    RouterUtil.gotoHotlineHome()
                }
                13 -> {//13-优惠券
                    linkId?.let {
                        RouterUtil.gotoCollectCouponActivity(it)
                    }
                }
                14 -> {//14-我要爆料
                    if (!UserManager.checkIsGotoLogin()) {
                        RouterUtil.gotoGoNewsActivity()
                    }
                }
                15 -> {//15-签到
                    RouterUtil.gotoGoMineSignInActivity()
                }
                16 ->{//预约点歌
                    RouterUtil.OrderSong.gotoSongOrderPage()
                }
            }
        }

        /**
         * 获取界面类型名称
         */
        fun getViewTypeName(linkType: Int): String {
            when (linkType) {
                0 -> return "音频"
                1 -> return "视频"
                2 -> return "图文"
                3 -> return "专辑"
                4 -> return "商品"
                5 -> return "报名"
                6 -> return "投票"
                7 -> return "直播间"
                8 -> return "微主页"
                9 -> return "链接"
                10 -> return "小程序"
                11 -> return "问答"
                12 -> return "为民热线"
                13 -> return "优惠券"
                14 -> return "爆料"
                15 -> return "签到"
            }
            return ""
        }

        fun OpenMiniProgram(
            context: Context,
            programId: String,
            programPath: String? = null
        ) {
            val appId = UrlConstant.WX_APPID // 填移动应用(App)的 AppId，非小程序的 AppID
            val api = WXAPIFactory.createWXAPI(context, appId)
            val req = WXLaunchMiniProgram.Req()
            //  req.userName = "gh_d43f693ca31f" // 填小程序原始id
            req.userName = programId // 填小程序原始id
            programPath?.let { req.path = programPath }
            ////拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
            req.miniprogramType =
                WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE // 可选打开 开发版，体验版和正式版
            api.sendReq(req)
        }

    }
}