package com.hnradio.jiguang.jverification

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.jiguang.verifysdk.api.*
import com.alibaba.android.arouter.launcher.ARouter
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.http.bean.UserInfo
import com.hnradio.common.manager.ACTION_LOGIN_SUCCESS
import com.hnradio.common.manager.UserManager
import com.hnradio.common.router.MainRouter
import com.hnradio.common.util.FileUtils
import com.hnradio.common.util.ScreenUtils
import com.hnradio.common.util.ToastUtils
import com.hnradio.jiguang.CODE_LOGIN_CANCELD
import com.hnradio.jiguang.CODE_LOGIN_SUCCESS
import com.hnradio.jiguang.R
import com.hnradio.jiguang.http.JiGuangApiUtil
import com.hnradio.jiguang.http.bean.JGLoginBean
import com.hnradio.jiguang.http.bean.ReqJGLoginBean
import com.hnradio.jiguang.jshare.AccessBean
import com.hwangjr.rxbus.RxBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @Description: 极光认证
 * @Author: huqiang
 * @CreateDate: 2021-07-07 17:01
 * @Version: 1.0
 */


class JgAuthLogin(private val context: Context) {
    val list = arrayListOf(
        PrivacyBean("《铁粉生活用户协议》", UrlConstant.USER_AGREEMENT, " 和", "、"),
        PrivacyBean("《铁粉生活用户隐私条款》", UrlConstant.USER_SECRETA_GREEMENT, "", "")
    )

    companion object {
        var jgAuthIsActive = false
    }

    /**
     * 判断网络环境是否支持
     */
    fun checkVerifyEnable(): Boolean {
        return JVerificationInterface.checkVerifyEnable(context)
    }

    /**
     * 预取号
     */
    suspend fun preLogin(): Boolean {
       return suspendCoroutine { continuation ->
            JVerificationInterface.preLogin(
                context, 5000
            ) { code, _ ->
                continuation.resume(code == 7000)
            }

        }
    }


    fun loginAuth() {
        jgAuthIsActive = true
        JVerificationInterface.setCustomUIWithConfig(getFullScreenPortraitConfig())
        JVerificationInterface.loginAuth(context, false, VerifyListener { code, token, operator ->
            jgAuthIsActive = false
            when (code) {
                CODE_LOGIN_SUCCESS -> {
                    login(ReqJGLoginBean(2, token, UserManager.getInvitationCode()))
                }
                CODE_LOGIN_CANCELD -> {
                    ToastUtils.show("授权取消")
                }
                else -> {
                    ToastUtils.show("授权失败")
                    toPhoneLong()
                }
            }
        })
    }

    private fun getFullScreenPortraitConfig(): JVerifyUIConfig? {
        val uiConfigBuilder = JVerifyUIConfig.Builder()
        val height = ScreenUtils.px2dip(context, ScreenUtils.getScreenHeight(context).toFloat())

        //导航栏
        uiConfigBuilder.setNavReturnImgPath("icon_arrows_black")
        uiConfigBuilder.setNavColor(-0x1)
        uiConfigBuilder.setStatusBarHidden(true)


        uiConfigBuilder.setSloganTextColor(-0x54fd)
        uiConfigBuilder.setSloganTextSize(14)
        uiConfigBuilder.setSloganOffsetY(270)

        //logo
        uiConfigBuilder.setLogoImgPath("icon_login_logo")
        uiConfigBuilder.setLogoOffsetY(160)
        uiConfigBuilder.setLogoWidth(37)
        uiConfigBuilder.setLogoHeight(47)


        uiConfigBuilder.setNumFieldOffsetY(230)
        uiConfigBuilder.setNumberColor(-0xcccccd)
        uiConfigBuilder.setNumberSize(24)

        uiConfigBuilder.setLogBtnImgPath("jiguang_login_selector_button")
        uiConfigBuilder.setLogBtnTextColor(-0x1)
        uiConfigBuilder.setLogBtnText("一键登录")
        uiConfigBuilder.setLogBtnOffsetY(340)
        uiConfigBuilder.setLogBtnWidth(300)
        uiConfigBuilder.setLogBtnHeight(44)

        //设置隐私条款名称颜色
        uiConfigBuilder.setPrivacyNameAndUrlBeanList(list)
        uiConfigBuilder.setPrivacyWithBookTitleMark(true)
        uiConfigBuilder.setAppPrivacyColor(-0x4b4949, -0xb58db)
        uiConfigBuilder.setUncheckedImgPath("icon_common_select_normal")
        uiConfigBuilder.setCheckedImgPath("icon_common_select_press")
        uiConfigBuilder.setPrivacyCheckboxSize(20)
        uiConfigBuilder.setPrivacyState(false)
        uiConfigBuilder.setPrivacyTextCenterGravity(true)
        uiConfigBuilder.setPrivacyTextSize(12)
        uiConfigBuilder.setPrivacyOffsetY(height - 500)
        uiConfigBuilder.setPrivacyOffsetX(30)
        uiConfigBuilder.setPrivacyVirtualButtonTransparent(true)
        uiConfigBuilder.setPrivacyVirtualButtonColor(Color.YELLOW)
        uiConfigBuilder.enableHintToast(
            true,
            Toast.makeText(context, "请同意勾选用户协议", Toast.LENGTH_SHORT)
        )

        // 自定义欢迎语
        val layoutWelcome = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutWelcome.setMargins(ScreenUtils.dip2px(context, 32f), 140, 0, 0)
        layoutWelcome.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        val tvWelcome = TextView(context)
        tvWelcome.text = context.getString(R.string.jiguang_login_welcome_tips)
        tvWelcome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        tvWelcome.setTextColor(ContextCompat.getColor(context, R.color.hui3))
        tvWelcome.layoutParams = layoutWelcome
        uiConfigBuilder.addCustomView(tvWelcome, false) { context, view -> }

        // 手机登录按钮
        val layoutParamPhoneLogin = RelativeLayout.LayoutParams(
            ScreenUtils.dip2px(context, 300f),
            ScreenUtils.dip2px(context, 44f)
        )
        layoutParamPhoneLogin.setMargins(0, 0, 0, ScreenUtils.dip2px(context, 60f))
        layoutParamPhoneLogin.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParamPhoneLogin.addRule(RelativeLayout.CENTER_IN_PARENT)
        val tvPhoneLogin = TextView(context)
        tvPhoneLogin.text = "使用其他方式登录"
        tvPhoneLogin.gravity = Gravity.CENTER
        tvPhoneLogin.textSize = 16F
        tvPhoneLogin.setTextColor(Color.parseColor("#575757"))
        tvPhoneLogin.setBackgroundResource(R.drawable.jiguang_login_other_btn)
        tvPhoneLogin.layoutParams = layoutParamPhoneLogin
        uiConfigBuilder.addCustomView(
            tvPhoneLogin, false
        ) { context, view -> toPhoneLong() }
        return uiConfigBuilder.build()
    }

    private fun toPhoneLong() {
        ARouter.getInstance().build(MainRouter.LoginActivityPath).navigation()
        JVerificationInterface.dismissLoginAuthActivity()
    }

    fun login(bean: ReqJGLoginBean) {
        JiGuangApiUtil.login(bean) {
            val model: JGLoginBean? = it?.data
            model?.let { it1 ->
                if (!it1.needBind) {
                    UserManager.saveUserToken(model.token)
                    ToastUtils.show("登录成功")
                    RxBus.get().post(ACTION_LOGIN_SUCCESS, model.token)
                    JiGuangApiUtil.getUserInfo { it2 ->
                        val user: UserInfo? = it2?.data
                        user?.let { it3 ->
                            UserManager.saveLoginUser(it3)
                        }
                    }
                    JVerificationInterface.dismissLoginAuthActivity()
                }
            }

        }
    }
}