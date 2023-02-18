package com.hnradio.common.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.aliyun.player.alivcplayerexpand.util.ThreadUtils.runOnUiThread
import com.google.gson.Gson
import com.hnradio.common.http.bean.PushExtrasBean
import com.hnradio.common.manager.UserManager
import com.hnradio.common.router.MainRouter
import com.hnradio.common.router.NoticeIntentUtil
import com.hnradio.common.router.RouterUtil
import com.hnradio.common.router.ShareManagerService
import com.hnradio.common.util.bdaudio.JsMp3Recorder
import com.hnradio.common.util.bdaudio.RecorderCallback
import com.just.agentweb.AgentWeb
import com.orhanobut.logger.Logger
import com.permissionx.guolindev.PermissionX
import com.yingding.lib_net.bean.JsResRecorderBean

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-30 11:38
 * @Version: 1.0
 */
class MyJsInterfaceHolder(val context: Context, val mAgentWeb: AgentWeb? = null) {

    @JavascriptInterface
    fun getUserToken() {
        if (!UserManager.checkIsGotoLogin()) {
            mAgentWeb?.let {
                it.jsAccessEntrace?.quickCallJs(
                    "setUserToken",
                    UserManager.getToken()

                )
            }
        }
    }

    @JavascriptInterface
    fun getUserInfo(): String? {
        return Gson().toJson(UserManager.getLoginUser())
    }

    @JavascriptInterface
    fun finish() {
        if (context is Activity) {
            context.finish()
        }
    }

    @JavascriptInterface
    fun getAppVersionCode(): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionCode
    }

    @JavascriptInterface
    fun getAppVersionName(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }


    /**
     * 奖品详情
     */
    @JavascriptInterface
    fun gotoAwardDetails(id: Int) {
        RouterUtil.gotoAwardDetailsActivity(id)
    }

    /**
     * 奖品列表
     */
    @JavascriptInterface
    fun gotoAwardList() {
        RouterUtil.gotoAwardListActivity()
    }

    /**
     * 通知跳转
     */
    @JavascriptInterface
    fun gotoCustomPage(params: String) {
        val extras = Gson().fromJson(params, PushExtrasBean::class.java)
        NoticeIntentUtil.startView(context, extras)
    }

    /**
     * 充值首页
     */
    @JavascriptInterface
    fun gotoRecharge() {
        if (!UserManager.checkIsGotoLogin()) {
            ARouter.getInstance().build(MainRouter.RechargeHome).navigation()
        }
    }

    /**
     * 商品详情
     */
    @JavascriptInterface
    fun gotoGoodsDetails(id: Int) {
        RouterUtil.gotoGoodsDetailActivity(id)
    }

    /**
     * 分享链接
     * @param shareTitle String 标题
     * @param shareDescribe String 描述
     * @param shareUrl String 链接
     * @param shareImageUrl String 图标
     */
    @JavascriptInterface
    fun shareUrl(
        shareTitle: String,
        shareDescribe: String,
        shareUrl: String,
        shareImageUrl: String
    ) {
        val shareService: ShareManagerService = ARouter.getInstance()
            .build(MainRouter.ShareManagerProvider)
            .navigation() as ShareManagerService
        shareService.shareUrl(context, shareTitle, shareDescribe, shareUrl, shareImageUrl)

    }

    /**
     * 分享图片
     * @param shareImageUrl String 图片地址
     */
    @JavascriptInterface
    fun shareImage(
        shareImageUrl: String
    ) {
        val shareService: ShareManagerService = ARouter.getInstance()
            .build(MainRouter.ShareManagerProvider)
            .navigation() as ShareManagerService
        shareService.shareImage(context, shareImageUrl)

    }

    /**
     * 分享音乐
     * @param shareTitle String 标题
     * @param shareDescribe String 描述
     * @param shareMusicFileUrl String? 音频mp3文件
     * @param shareMusicUrl String? 音频播放页面
     * @param shareImageUrl String? 图标
     */
    @JavascriptInterface
    fun shareMusic(
        shareTitle: String,
        shareDescribe: String,
        shareMusicFileUrl: String?,
        shareMusicUrl: String?,
        shareImageUrl: String?,
    ) {
        val shareService: ShareManagerService = ARouter.getInstance()
            .build(MainRouter.ShareManagerProvider)
            .navigation() as ShareManagerService
        shareService.shareMusic(
            context,
            shareTitle,
            shareDescribe,
            shareMusicUrl,
            shareMusicFileUrl,
            shareImageUrl
        )

    }

    /**
     * 分享视频
     * @param shareTitle String 标题
     * @param shareDescribe String 描述
     * @param shareVideoUrl String 视频地址
     * @param shareImageUrl String? 图标
     */
    @JavascriptInterface
    fun shareVideo(
        shareTitle: String,
        shareDescribe: String,
        shareVideoUrl: String,
        shareImageUrl: String?,
    ) {
        val shareService: ShareManagerService = ARouter.getInstance()
            .build(MainRouter.ShareManagerProvider)
            .navigation() as ShareManagerService
        shareService.shareVideo(context, shareTitle, shareDescribe, shareVideoUrl, shareImageUrl)

    }

    /**
     * 路由跳转
     */
    @JavascriptInterface
    fun gotoMinePage(route: String) {
        ARouter.getInstance().build(route).navigation()
    }

    /**
     * 铁粉生活详情
     * type: 0： 短视频 ，1：图文
     */
    @JavascriptInterface
    fun gotoLifeDetail(type: Int, id: Int, tagId: Int) {
        if (type == 0) {
            RouterUtil.gotoShortVideoPlay(id, tagId, false)
        } else {
            RouterUtil.gotoInfoDetailUserActivity(id)
        }
    }

    /**
     * 话题
     */
    @JavascriptInterface
    fun gotoTopicDetail(id: Int) {
        RouterUtil.gotoTopicActivity(id)
    }

    /**
     * 专家主页
     */
    @JavascriptInterface
    fun gotoExpertHome(id: Int) {
        RouterUtil.gotoExpertHome(id)
    }


    /**
     * 领红包
     */
    @JavascriptInterface
    fun openRedEnvelope(activityType: Int, id: Int) {
        RouterUtil.gotoRedEnvelopeActivity(activityType, id)
    }

    /**
     * 实名
     */
    @JavascriptInterface
    fun gotoRealName() {
        if (context is Activity) {
            RouterUtil.gotoRealName(
                context,
                100
            )
        }

    }

    /**
     * 账户安全
     */
    @JavascriptInterface
    fun gotoAccountAndSafe() {
        RouterUtil.gotoAccountAndSafeActivity()
    }

    @JavascriptInterface
    fun configAudio() {
        runOnUiThread {
            PermissionX.init(context as? FragmentActivity)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request { allGranted, grantedList, deniedList ->
                    JsMp3Recorder.isPermission = allGranted
                    JsMp3Recorder.configAudio(object : RecorderCallback {
                        override fun onError(code: Int, msg: String) {
                            mAgentWeb?.let {
                                var method =
                                    if (code == -4) "transCallback" else "recorderCallback"
                                it.jsAccessEntrace?.quickCallJs(
                                    method,
                                    Gson().toJson(JsResRecorderBean(code, msg))
                                )
                            }
                        }

                        override fun onRecordSuccess(code: Int, filePath: String) {
                            mAgentWeb?.let {
                                it.jsAccessEntrace?.quickCallJs(
                                    "recorderCallback",
                                    Gson().toJson(
                                        JsResRecorderBean(
                                            code,
                                            filePath = filePath,
                                        )
                                    )
                                )
                            }
                        }

                        override fun onTransSuccess(code: Int, translateStr: String) {
                            mAgentWeb?.let {
                                it.jsAccessEntrace?.quickCallJs(
                                    "transCallback",
                                    Gson().toJson(
                                        JsResRecorderBean(
                                            code,
                                            translateStr = translateStr,
                                        )
                                    )
                                )
                            }
                        }
                    })
                }

        }
    }

    @JavascriptInterface
    fun reqPermission(vararg permissions: String) {
        PermissionX.init(context as? FragmentActivity)
            .permissions(listOf(*permissions))
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {

                }
            }
    }

    @JavascriptInterface
    fun startRecorder() {
        Logger.d("开始录音")
        runOnUiThread {
            PermissionX.init(context as? FragmentActivity)
                .permissions(Manifest.permission.RECORD_AUDIO)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        JsMp3Recorder.startRecorder()
                    } else {
                        mAgentWeb?.let {
                            it.jsAccessEntrace?.quickCallJs(
                                "transCallback",
                                Gson().toJson(
                                    JsResRecorderBean(
                                        -5,
                                        "未授权",
                                    )
                                )
                            )
                        }
                    }
                }
        }
    }

    @JavascriptInterface
    fun endRecorder() {
        Logger.d("结束录音")
        JsMp3Recorder.endRecorder()
    }

    @JavascriptInterface
    fun audioTrans(str: String) {
        Logger.d("开始翻译")
        runOnUiThread {
            JsMp3Recorder.audioTrans(str)
        }
    }

}