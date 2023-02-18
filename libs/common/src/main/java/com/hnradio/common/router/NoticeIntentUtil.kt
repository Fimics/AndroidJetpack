package com.hnradio.common.router

import android.content.Context
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.http.bean.PushExtrasBean
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.ToastUtils
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 *  跳转工具类
 * created by qiaoyan on 2021/8/2
 */
class NoticeIntentUtil {

    companion object {

        /**
         * 首页跳转到其他界面
         */
        fun startView(
            context: Context,
            extras: PushExtrasBean

        ) {
            val pushType = extras.messageType ?: 0
            val linkType = extras.linkType
            val linkId = extras.linkId
            val linkUrl = extras.linkUrl
            val linkAppId = extras.linkAppId

            if (extras.isSystemMsg()) {
                when (linkType) {
                    0 -> {//0-音频
                        linkId?.let { RouterUtil.gotoAudioPlayActivity(it) }
                    }
                    1 -> {//1-视频
                        linkId?.let { RouterUtil.gotoVideoPlay(it) }
                    }
                    2 -> {//2-图文
                        linkId?.let { RouterUtil.gotoInfoDetailPlatformActivity(it) }
                    }
                    3 -> {//3-专辑
                        linkId?.let { RouterUtil.gotoAlbumActivity(it) }
                    }
                    4 -> {//4-商品
                        linkId?.let { RouterUtil.gotoGoodsDetailActivity(it) }
                    }
                    5 -> {//5-报名
                        if (!UserManager.checkIsGotoLogin()) {
                            linkId?.let { RouterUtil.gotoSignUp(it) }
                        }
                    }
                    6 -> {//6-投票
                        linkId?.let { RouterUtil.gotoVoteList(it) }
                    }
                    7 -> {//7-直播间
                        linkId?.let { RouterUtil.gotoLivePlay(it) }
                    }
                    8 -> {//8-微主页
                        linkId?.let { RouterUtil.gotoMicroHomepage(it) }
                    }
                    9 -> {//9-H5链接
                        linkUrl?.let {
                            RouterUtil.gotoWebViewActivity("", linkUrl)
                        }
                    }
                    10 -> {//10-小程序
                        if (!linkUrl.isNullOrBlank() && !linkAppId.isNullOrBlank()) {
                            StartViewUtil.OpenMiniProgram(context, linkAppId, linkUrl)
                        }
                    }
                    11 -> {//11-问答
                        RouterUtil.gotoQuestionAnswerHome()
                    }
                    12 -> {//12-为民热线
                        RouterUtil.gotoHotlineHome()
                    }
                    13 -> {//13-优惠券
                        RouterUtil.gotoMineCardActivity()
                    }
                    14 -> {//14-我要爆料
                        RouterUtil.gotoGoNewsActivity()
                    }
                    15 -> {//15-签到
                        RouterUtil.gotoGoMineSignInActivity()
                    }
                    16 -> {//16-铁粉生活图文
                        linkId?.let { RouterUtil.gotoInfoDetailUserActivity(it) }
                    }
                    17 -> {//17-铁粉生活视频
                        linkId?.let { RouterUtil.gotoShortVideoPlay(it) }
                    }
                }
            } else if(extras.isUserMsg()){
               routerUserMsg(linkType, linkId)
            }else if(extras.isLifeTypeMsg()){
                //图文作品linkType=1,视频作品linkType=3
                when (linkType) {
                    1 ->{
                        linkId?.let { RouterUtil.gotoInfoDetailUserActivity(it) }
                    }
                    3 ->{
                        linkId?.let { RouterUtil.gotoShortVideoPlay(it, -1, true) }
                    }
                }
            }else if(extras.isExpertAnswerMsg()){
                linkId?.let { RouterUtil.gotoExpertQuestionDetailActivity(it) }
            }
        }

        fun routerUserMsg(linkType: Int, linkId: Int?) {
            when (linkType) {
                0, 1 -> {//图文点赞评论
                    linkId?.let { RouterUtil.gotoInfoDetailUserActivity(it) }
                }
                2, 3 -> {//视频点赞评论
                    linkId?.let { RouterUtil.gotoShortVideoPlay(it) }
                }
                4 -> {//关注-我的铁粉生活
                    RouterUtil.gotoMineWorks()
                }
                5 -> {//我的问答
                    linkId?.let { RouterUtil.gotoGoMineQAActivity() }
                }
                6 -> {//投诉反馈
                    if (!UserManager.checkIsGotoLogin()) {
                        RouterUtil.gotoMineComplaintHome()
                    }
                }
                7, 8 -> {//订单
                    linkId?.let { RouterUtil.gotoOrderDetailActivity(it) }
                }
                10 ->{//预约点歌详情
                    linkId?.let { RouterUtil.OrderSong.gotoOrderSongDetailPage(it) }
                }
                11 ->{//直播间
                    linkId?.let { RouterUtil.gotoLivePlay(it) }
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
            }
            return ""
        }

    }
}