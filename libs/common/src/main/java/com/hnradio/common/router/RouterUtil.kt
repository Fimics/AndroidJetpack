package com.hnradio.common.router

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.hnradio.common.R
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.http.bean.PushExtrasBean
import com.hnradio.common.util.AssetsUtil
import com.hnradio.common.util.GlideEngine
import com.hnradio.common.util.Global
import com.hnradio.common.util.ToastUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.permissionx.guolindev.PermissionX
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONArray

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.router
 * @ClassName: RouterUtil
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/8/15 12:15 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/15 12:15 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
object RouterUtil {
    /**跳转到主页（推送）*/
    fun gotoSplashFromPush(extras: PushExtrasBean, context: Activity) {
        /*   ARouter.getInstance().build(MainRouter.SplashActivityPath)
               .withParcelable("PushExtrasBean", extras)
               .navigation()*/
        val intent = Intent()
        intent.component = ComponentName(
            "com.hnradio.fans",
            "com.hnradio.fans.SplashActivity"
        )
        intent.putExtra("PushExtrasBean", extras)
        context.startActivity(intent)
    }

    /**跳转到主页（推送）*/
    fun gotoMainFromPush(extras: PushExtrasBean, context: Activity) {
        /*  ARouter.getInstance().build(MainRouter.MainActivityPath)
              .withParcelable("PushExtrasBean", extras)
              .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
              .navigation()*/
        val intent = Intent();
        intent.component = ComponentName(
            "com.hnradio.fans",
            "com.hnradio.fans.MainActivity"
        )
        intent.putExtra("PushExtrasBean", extras)
        context.startActivity(intent)


    }

    /**跳转到短视频*/
    fun gotoShortVideoPlay(id: Int, tagId: Int = -1, isOnlyPlayOne: Boolean = true) {
        ARouter.getInstance().build(MainRouter.ShortVideoPlayPath)
            .withInt("id", id)
            .withInt("tagId", tagId)
            .withBoolean("isOnlyPlayOne", isOnlyPlayOne)
            .navigation()
    }

    /**跳转到直播间*/
    fun gotoLivePlay(id: Int) {
//        if(BuildConfig.DEBUG){
        ARouter.getInstance().build(MainRouter.LivePlayPath2)
            .withInt("id", id)
            .navigation()
//        }else{
//            ARouter.getInstance().build(MainRouter.LivePlayPath)
//                .withInt("id", id)
//                .navigation()
//        }
    }

    /**跳转到视频播放界面*/
    fun gotoVideoPlay(id: Int) {
        ARouter.getInstance().build(MainRouter.VideoPlayPath2)
            .withInt("id", id)
            .navigation()
//        ARouter.getInstance().build(MainRouter.VideoPlayPath)
//            .withInt("id", id)
//            .navigation()
    }

    /**跳转到视频预览界面*/
    fun gotoVideoPreview(url: String) {
        ARouter.getInstance().build(MainRouter.VideoPreviewPath)
            .withString("url", url)
            .navigation()
    }

    /**跳转到图片预览界面*/
    fun gotoImagePreview(position: Int, paths: List<String>) {
        val result = ArrayList<LocalMedia>()
        paths.forEach {
            val bean = LocalMedia()
            bean.path = it
            bean.mimeType
            result.add(bean)
        }
        PictureSelector.create(Global.getTopActivity())
            .themeStyle(R.style.picture_default_style)
            .isNotPreviewDownload(true)
            .imageEngine(GlideEngine.createGlideEngine())
            .openExternalPreview(position, result)
    }

    /**跳转到我的优惠券*/
    fun gotoMineCardActivity() {
        ARouter.getInstance().build(MainRouter.MineCardList)
            .navigation()
    }

    /**跳转到填写邀请码*/
    fun gotoAddInvitationActivity() {
        ARouter.getInstance().build(MainRouter.AddInvitationCode)
            .navigation()
    }
    /**跳转到我的邀请列表*/
    fun gotoMyInvitationListActivity() {
        ARouter.getInstance().build(MainRouter.MyInvitationList)
            .navigation()
    }

    /**跳转到商品详情*/
    fun gotoGoodsDetailActivity(id: Int) {
        ARouter.getInstance().build(MainRouter.GoodDetailPath)
            .withInt("id", id)
            .navigation()
    }

    /**跳转音频播放*/
    fun gotoAudioPlayActivity(albumDetailId: Int) {
        ARouter.getInstance().build(MainRouter.AudioPlayHomePath)
            .withInt("albumDetailId", albumDetailId)
            .navigation()
    }

    /**跳转全部电台*/
    fun gotoAllRadioStationActivity() {
        ARouter.getInstance().build(MainRouter.AllRadioStationPath)
            .navigation()
    }

    /**跳转专辑中图文*/
    fun gotoInfoDetailPlatformActivity(albumDetailId: Int) {
        ARouter.getInstance().build(MainRouter.InfoDetailPlatformPath)
            .withInt("albumDetailId", albumDetailId)
            .navigation()
    }

    /**跳转专辑*/
    fun gotoAlbumActivity(albumInfoId: Int) {
        ARouter.getInstance().build(MainRouter.AlbumPath)
            .withInt("albumInfoId", albumInfoId)
            .navigation()
    }

    /**跳转铁粉生活中图文*/
    fun gotoInfoDetailUserActivity(lifeId: Int) {
        ARouter.getInstance().build(MainRouter.InfoDetailUserPath)
            .withInt("lifeId", lifeId)
            .navigation()
    }

    /**跳转问题详情*/
    fun gotoQuestionDetailActivity(qaId: Int) {
        ARouter.getInstance().build(MainRouter.QuestionDetailPath)
            .withInt("qa_id", qaId)
            .navigation()
    }

    /**跳转专家问题详情*/
    fun gotoExpertQuestionDetailActivity(qaId: Int) {
        ARouter.getInstance().build(MainRouter.ExpertQuestionDetailPath)
            .withInt("qa_id", qaId)
            .navigation()
    }

    /**跳转提问*/
    fun gotoQuestionAskActivity() {
        ARouter.getInstance().build(MainRouter.QuestionAskPath)
            .navigation()
    }

    /**跳转专家回复*/
    fun gotoExpertAnswerActivity() {
        ARouter.getInstance().build(MainRouter.ExpertAskPath)
            .navigation()
    }

    /**跳转到报名*/
    fun gotoSignUp(id: Int) {
        ARouter.getInstance().build(MainRouter.SignUpHomePath)
            .withInt("id", id)
            .navigation()
    }

    /**跳转到我的爆料*/
    fun gotoMineNews() {
        ARouter.getInstance().build(MainRouter.MineNewsPath)
            .navigation()
    }

    /**跳转到我的投票*/
    fun gotoMineVote() {
        ARouter.getInstance().build(MainRouter.MineVotePath)
            .navigation()
    }

    /**跳转到我的报名*/
    fun gotoMineSignUp() {
        ARouter.getInstance().build(MainRouter.MineSignUpListPath)
            .navigation()
    }

    /**跳转到我的钱包*/
    fun gotoRechargeHome() {
        ARouter.getInstance().build(MainRouter.RechargeHome).navigation()
    }


    /**跳转到主播主页*/
    fun gotoAnchorHomepage(userId: Int) {
        ARouter.getInstance().build(MainRouter.AnchorHomepagePath)
            .withInt("userId", userId)
            .navigation()
    }

    /**跳转到领取优惠券*/
    fun gotoCollectCouponActivity(couponId: Int) {
        ARouter.getInstance().build(MainRouter.CollectCouponPath)
            .withInt("couponId", couponId)
            .navigation()
    }


    /**跳转到webview*/
    fun gotoWebViewActivity(title: String, url: String) {
        ARouter.getInstance().build(MainRouter.WEBVIEW_ACTIVITY)
            .withString("title", title)
            .withString("url", url)
            .navigation()
    }

    /**跳转到支付*/
    fun gotoPayActivity(orderId: String, amount: Long) {
        ARouter.getInstance().build(MainRouter.PayActivity)
            .withString("orderId", orderId)
            .withLong("Amount", amount)
            .navigation()
    }

    /**跳转到问答*/
    fun gotoQuestionAnswerHome() {
        ARouter.getInstance().build(MainRouter.QuestionAnswerPath)
            .navigation()
    }

    /**跳转到为民热线*/
    fun gotoHotlineHome() {
        ARouter.getInstance().build(MainRouter.HotlineHomePath)
            .navigation()
    }

    /**跳转到我的投诉*/
    fun gotoMineComplaintHome() {
        ARouter.getInstance().build(MainRouter.MineComplaintHomePath)
            .navigation()
    }

    /**跳转到我的铁粉生活*/
    fun gotoMineWorks() {
        ARouter.getInstance().build(MainRouter.MineWorksPath)
            .navigation()
    }

    /**跳转微主页*/
    fun gotoMicroHomepage(albumDetailId: Int) {
        ARouter.getInstance().build(MainRouter.MicroHomepagePath)
            .withInt("albumDetailId", albumDetailId)
            .navigation()
    }


    /**投票详情*/
    fun gotoVoteDetail(setupDetailId: Int) {
        ARouter.getInstance().build(MainRouter.MINEVOTEDETAIL)
            .withInt("setupDetailId", setupDetailId)
            .navigation()
    }

    /**跳转话题*/
    fun gotoTopicActivity(topicId: Int) {
        ARouter.getInstance().build(MainRouter.TopicPath)
            .withInt("topicId", topicId)
            .navigation()
    }

    /**跳转发布铁粉生活*/
    fun gotoReleaseActivity(
        contentType: Int,
        contentPath: String,
        contentPathList: ArrayList<String>
    ) {
        ARouter.getInstance().build(MainRouter.ReleasePath)
            .withInt("content_type", contentType)
            .withString("content_path", contentPath)
            .withStringArrayList("content_path_list", contentPathList)
            .navigation()
    }

    /**
     * 投票活动界面 列表
     */
    fun gotoVoteList(voteSetupId: Int) {
        ARouter.getInstance().build(MainRouter.VoteActivityPath)
            .withInt("voteSetupId", voteSetupId)
            .navigation()
    }

    //跳转到相册选择界面
    fun gotoPhotoViewSelect(
        activity: FragmentActivity = Global.getTopActivity() as FragmentActivity,
        type: Int = PictureMimeType.ofAll(),
        maxSelectNum: Int = 9,
        minSelectNum: Int = 1,
        imageSpanCount: Int = 3,
        videoDurationLimit: Int = 60,
        callBack: (MutableList<LocalMedia>?) -> Unit
    ) {
        PermissionX.init(activity)
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    PictureSelector.create(Global.getTopActivity())
                        .openGallery(type)
                        .imageEngine(GlideEngine.createGlideEngine())
                        .isWeChatStyle(true)
                        .showCropGrid(true)
                        .isEditorImage(true)
                        .videoMaxSecond(videoDurationLimit)
                        .isMaxSelectEnabledMask(true)
                        .isCompress(true)
                        .maxSelectNum(maxSelectNum)// 最大图片选择数量
                        .minSelectNum(minSelectNum)// 最小选择数量
                        .imageSpanCount(imageSpanCount)// 每行显示个数
                        .setPictureWindowAnimationStyle(
                            PictureWindowAnimationStyle.ofCustomWindowAnimationStyle(
                                R.anim.picture_anim_up_in,
                                R.anim.picture_anim_down_out
                            )
                        )// 自定义相册启动退出动画
                        .forResult(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: MutableList<LocalMedia>?) {
                                callBack(result)
                            }

                            override fun onCancel() {
                            }
                        })
                } else {
                    ToastUtils.show("未授权")
                }
            }
    }

    //跳转到我要投诉
    fun gotoComplaintActivity() {
        ARouter.getInstance().build(MainRouter.ComplaintPath)
            .navigation()
    }

    //跳转到投诉详情
    fun gotoComplaintDetailActivity(id: String) {
        ARouter.getInstance().build(MainRouter.ComplaintDetailPath)
            .withString("id", id)
            .navigation()
    }


    //跳转到我要爆料
    fun gotoGoNewsActivity() {
        ARouter.getInstance().build(MainRouter.MINEGONEWS)
            .navigation()
    }


    //跳转到我的问答
    fun gotoGoMineQAActivity() {
        ARouter.getInstance().build(MainRouter.MINEQA)
            .navigation()
    }


    //跳转到我要签到
    fun gotoGoMineSignInActivity() {
        ARouter.getInstance().build(MainRouter.MINEGOSIGNIN)
            .navigation()
    }

    //跳转到我的奖品
    fun gotoAwardListActivity() {
        ARouter.getInstance().build(MainRouter.MineAwardList)
            .navigation()
    }

    //跳转到我的奖品详情
    fun gotoAwardDetailsActivity(id: Int) {
        ARouter.getInstance().build(MainRouter.MineAwardDetails)
            .withInt("id", id)
            .navigation()
    }

    //跳转到我的直播奖品详情
    fun gotoLiveAwardDetailsActivity(id: Int) {
        ARouter.getInstance().build(MainRouter.MineLiveAwardDetails)
            .withInt("id", id)
            .navigation()
    }

    //跳转到我的互动奖品详情
    fun gotoInteractiveAwardDetailsActivity(id: Int) {
        ARouter.getInstance().build(MainRouter.MineInteractiveAwardDetails)
            .withInt("id", id)
            .navigation()
    }

    //跳转到核销
    fun gotoWriteOffActivity(sn: String) {
        ARouter.getInstance().build(MainRouter.MineWriteOff)
            .withString("sn", sn)
            .navigation()
    }

    //跳转到报名核销
    fun gotoSignupWriteOffActivity(sn: String) {
        ARouter.getInstance().build(MainRouter.SignupWriteOff)
            .withString("sn", sn)
            .navigation()
    }

    //跳转到订单核销
    fun gotoOrderWriteOffActivity(sn: String) {
        ARouter.getInstance().build(MainRouter.OrderWriteOff)
            .withString("sn", sn)
            .navigation()
    }

    //跳转到专家首页
    fun gotoExpertHome(id: Int) {
        ARouter.getInstance().build(MainRouter.ExpertHomepagePath)
            .withInt("id", id)
            .navigation()
    }

    //跳转到订单列表
    fun gotoOrderListActivity() {
        ARouter.getInstance().build(MainRouter.MineOrderPath)
            .navigation()
    }

    //跳转到订单列表
    fun gotoOrderDetailActivity(id: Int) {
        ARouter.getInstance().build(MainRouter.OrderDetail)
            .withInt("id", id)
            .navigation()
    }

    //展示地区选择
    fun showSelectAddressDialog(
        cancelListener: View.OnClickListener? = null,
        cancelStr: String = "取消",
        callBack: (String, String, String) -> Unit,
    ) {

        val context = Global.getTopActivity()!!

        AssetsUtil.getFileString(context, "province.json") { assetStr ->
            val jsonArray = JSONArray(assetStr)
            val options1Items = mutableListOf<String>()
            val options2Items = mutableListOf<List<String>>()
            val options3Items = mutableListOf<List<List<String>>>()
            val length = jsonArray.length()
            for (i in 0 until length) {
                val jsonObject = jsonArray.getJSONObject(i)
                val list1 = mutableListOf<String>()
                val list2 = mutableListOf<List<String>>()
                options1Items.add(jsonObject.getString("name"))
                val cityArray = jsonObject.getJSONArray("city")
                val cityLength = cityArray.length()
                for (j in 0 until cityLength) {
                    val cityObj = cityArray.getJSONObject(j)
                    list1.add(cityObj.getString("name"))
                    val list3 = mutableListOf<String>()
                    val areaArray = cityObj.getJSONArray("area")
                    val areaLength = areaArray.length()
                    for (k in 0 until areaLength) {
                        list3.add(areaArray.getString(k))
                    }
                    list2.add(list3)
                }
                options2Items.add(list1)
                options3Items.add(list2)
            }
            val pvOptions: OptionsPickerView<String> =
                OptionsPickerBuilder(context) { options1, options2, options3, _ -> //返回的分别是三个级别的选中位置
                    callBack(
                        options1Items[options1],
                        options2Items[options1][options2],
                        options3Items[options1][options2][options3]
                    )
                }
                    .setTitleText("请选择所在区域")
                    .addOnCancelClickListener(cancelListener)
                    .setCancelText(cancelStr)
                    .setTitleBgColor(context.resources.getColor(R.color.white)) //设置标题的背景颜色
                    .setDividerColor(context.resources.getColor(R.color.hui9)) //设置分割线的颜色
                    .setTextColorCenter(context.resources.getColor(R.color.hui3)) //设置选中项文字颜色
                    .setSubmitColor(context.resources.getColor(R.color.color_000000)) //确定按钮文字颜色
                    .setCancelColor(context.resources.getColor(R.color.color_000000)) //取消按钮文字颜色
                    .setContentTextSize(18)
                    .build()
            pvOptions.setPicker(
                options1Items, options2Items, options3Items
            ) //三级选择器
            pvOptions.show()
        }
    }

    private fun OpenMiniProgram(
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


    /**-------------------------------------------预约点歌-------------------------------------------*/

    object OrderSong {
        /**跳转到预约点歌*/
        fun gotoSongOrderPage() {
            ARouter.getInstance().build(MainRouter.PreOrderSongEntry)
                .navigation()
        }

        /**跳转直播频道选择*/
        fun gotoLiveRoomListPage() {
            ARouter.getInstance().build(MainRouter.PreOrderSongLiveRoomList)
                .navigation()
        }

        /**跳转点歌详情*/
        fun gotoOrderSongDetailPage(recordId: Int) {
            ARouter.getInstance().build(MainRouter.PreOrderSongDetail)
                .withInt("recordId", recordId)
                .navigation()
        }

    }

    /**跳转到实人认证*/
    fun gotoRealName(activity: Activity, requestCode: Int) {
        ARouter.getInstance().build(MainRouter.RealName)
            .navigation(activity, requestCode)
    }

    //跳转开红包
    fun gotoRedEnvelopeActivity(activityType: Int, id: Int) {
        ARouter.getInstance().build(MainRouter.MineRedEnvelopeDetails)
            .withInt("activityType", activityType)
            .withInt("id", id)
            .navigation()
    }

    //账户安全（绑定微信）
    fun gotoAccountAndSafeActivity() {
        ARouter.getInstance().build(MainRouter.MineAccountAndSafeActivity)
            .navigation()
    }
}