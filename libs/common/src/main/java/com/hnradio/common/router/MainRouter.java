package com.hnradio.common.router;

/**
 * Copyright:   2020 www.yingding.com Inc. All rights reserved.
 * project:     ydm_online
 * Data:        2020-02-19
 * Author:      yingding
 * Mail:        shaoguotong@yingding.org
 * Version：    V1.0
 * Title:       主界面阿里ARout路径
 */
public interface MainRouter {
    String MainActivityPath = "/main/Activity/MainActivity";//首页
    String SplashActivityPath = "/main/Activity/SplashActivityPath";//启动页
    String MainHomeFragmentPath = "/home/fragment/Home";//首页
    String MainLiveFragmentPath = "/live/fragment/Live";//直播
    String MainFansFragmentPath = "/fans/fragment/fans";//宠粉
    String MainMineFragmentPath = "/mine/fragment/Mine";//我的


    String LoginActivityPath = "/user/Activity/login";//登录
    String BindPhoneActivityPath = "/user/Activity/bind_phone";//绑定手机
    String ServiceUserManager = "/service/user/manager";//用户服务
     String ShareManagerProvider = "/share/provider";//分享服务


    String RechargeHome = "/mine/recharge/home";//充值首页
    String AudioPlayHomePath = "/live/audio/audioPlay";//音频播放界面
    String AllRadioStationPath = "/live/audio/allRadioStation";//全部电台
    String ShortVideoPlayPath = "/live/video/shortVideo";//小视频播放界面
    String VideoPlayPath = "/live/video/videoPlay";//视频播放界面
    String VideoPlayPath2 = "/live/video/videoPlay2";//视频播放界面
    String LivePlayPath = "/live/video/livePlay";//直播间播放界面
    String LivePlayPath2 = "/live/video/livePlay2";//直播间播放界面
    String VideoPreviewPath = "/live/video/preview";//视频播放预览


    //--商城
    String GoodDetailPath = "/petfan/good/detail";//商品详情
    String MineCardList = "/mine/card/list";//优惠券列表
    String CollectCouponPath = "/petfan/coupon/collect";//领取优惠券
    String WEBVIEW_ACTIVITY = "/petfan/webview/activity";//webview
    String OrderWriteOff = "/petfan/order/writeOff";//订单核销
    String OrderAfterSaleApply = "/petfan/order/AfterSaleApply";//申请售后
    String OrderAfterSaleList = "/petfan/order/AfterSaleList";//售后列表
    String OrderAfterSaleDetail = "/petfan/order/AfterSaleDetail";//售后详情

    String MineOrderPath = "/petfan/order/MineOrderActivity";//我的订单
    String OrderDetail = "/petfan/order/OrderDetail";//订单详情

    String AddressManagerPath = "/mine/setUserInfo/AddressManager";//地址管理页面

    String MineZanPath = "/mine/mineInfo/MineZanActivity";//我的点赞
    String PayActivity = "/mine/pay/payActivity";//支付
    String MineWorksPath = "/mine/mineInfo/MineWorksActivity";//我的铁粉生活
    String MineComplaintHomePath = "/mine/mineInfo/MineComplaintActivity";//我的投诉主页
    String MineAccountAndSafeActivity = "/mine/mineInfo/AccountAndSafeActivity";//微信绑定


    String InfoDetailPlatformPath = "/home/info/InfoDetailPlatformActivity";//专辑中的图文
    String InfoDetailUserPath = "/home/info/InfoDetailUserActivity";//铁粉生活中的图文
    String AlbumPath = "/home/info/AlbumActivity";//专辑
    String TopicPath = "/home/topic/TopicActivity";//话题页
    String ReleasePath = "/app/release/PostContentActivity";//发布铁粉生活页

    String QuestionAnswerPath = "/home/info/QuestionAnswerActivity";//问答
    String ExpertHomepagePath = "/home/info/ExpertHomepage";//问答-专家首页
    String HotlineHomePath = "/home/info/HotlineHomeActivity";//为民热线
    String ComplaintPath = "/home/info/ComplaintActivity";//我要投诉
    String ComplaintDetailPath = "/home/info/ComplaintDetailActivity";//投诉详情
    String MicroHomepagePath = "/home/info/MicroHomepageActivity";//微主页

    String QuestionAskPath = "/home/qa/QuestionAskActivity";//提问
    String ExpertAskPath = "/home/qa/ExpertAnswerActivity";//专家回复
    String QuestionDetailPath = "/home/qa/QuestionDetailActivity";//问题详情
    String ExpertQuestionDetailPath = "/home/qa/ExpertQuestionDetailActivity";//专家问题详情
    String AnchorHomepagePath = "/home/homepage/HomepageActivity";//主播主页

    String SignUpHomePath = "/home/signup/SignUpHome";//报名首页
    String MineSignUpListPath = "/home/signup/MySignUpList";//我的报名

    String MineNewsPath = "/mine/mineInfo/MineNewsActivity";//我的爆料
    String MineVotePath = "/mine/mineInfo/MineVoteActivity";//我的投票

    String VoteActivityPath = "/home/signup/VoteActivity";//投票



    String MINEVOTEDETAIL = "/mine/vote/detail";//投票详情
    String MINEQA = "/mine/go/qa";          //我的问答
    String ExpertQA = "/mine/go/ExpertQA";          //专家问答

    String MINEGONEWS = "/mine/go/news";//我要爆料

    String MINEGOSIGNIN_OLD = "/mine/go/signIn";//我要签到
    String MINEGOSIGNIN = "/mine/go/dailySign";//签到新

    String MineAwardList = "/mine/award/awardList";//我的奖品列表
    String MineAwardDetails = "/mine/award/awardDetails";//我的奖品详情
    String MineLiveAwardDetails = "/mine/award/liveAwardDetails";//我的直播奖品详情
    String MineInteractiveAwardDetails = "/mine/award/InteractiveAwardDetails";//互动奖品详情
    String MineWriteOff = "/mine/award/writeOff";//核销

    String MineRedEnvelopeDetails = "/mine/award/RedEnvelopeDetails";//红包详情

    String SignupWriteOff = "/home/signup/signupWriteOff";//报名核销

    String AddInvitationCode = "/mine/invitation/AddInvitationCode";//填写邀请码
    String MyInvitationList = "/mine/invitation/MyInvitationList";//邀请列表



    //-------------------------------------------预约点歌---------------------------------------
    /**预约点歌入口*/
    String PreOrderSongEntry = "/orderSong/ac/OrderSongEntry";
    /**直播频道列表*/
    String PreOrderSongLiveRoomList = "/orderSong/ac/LiveRoomList";
    /**频道下歌曲搜索*/
    String PreOrderSongList = "/orderSong/ac/LiveRoomSongList";
    /**点歌记录*/
    String PreOrderSongRecord = "/orderSong/ac/MyOrderSongRecord";
    /**点歌详情*/
    String PreOrderSongDetail = "/orderSong/ac/MyOrderSongDetail";
    /**申请海报记录*/
    String PosterRequestRecord = "/orderSong/ac/PosterRequestRecord";
    /**海报详情页*/
    String PosterDetail = "/orderSong/ac/PosterDetail";
    /**预约成功歌单列表 dialog*/
    String PreOrderSongSuccessList = "/orderSong/ac/OrderSongList";

    /**实人认证*/
    String RealName = "/mine/realName/RealNameActivity";
}
