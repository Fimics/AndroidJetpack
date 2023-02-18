package com.hnradio.common.manager

import android.content.Intent
import com.alibaba.android.arouter.launcher.ARouter
import com.hnradio.common.AppContext
import com.hnradio.common.http.bean.UserInfo
import com.hnradio.common.router.MainRouter
import com.hnradio.common.router.UserManagerService
import com.hnradio.common.util.Global
import com.hnradio.common.util.MMKVUtils
import com.hnradio.common.util.mqtt.MqttEngine
import com.hwangjr.rxbus.RxBus
import com.orhanobut.hawk.Hawk
import com.yingding.lib_net.http.RetrofitUtil

/**
 *
 * @Description: 用户信息
 * @Author: huqiang
 * @CreateDate: 2021-08-04 17:27
 * @Version: 1.0
 */
object UserManager {
    private val USER_TOKEN = "USER_TOKEN"
    private val INVITATION_CODE = "INVITATION_CODE"
    private var userInfo: UserInfo? = null
    private var userToken: String? = null
    private var invitationCode: String? = null

    fun initUser() {
        getLoginUser()
        getToken()
        if (!userToken.isNullOrEmpty()) {
            if (RetrofitUtil.instance.headerMap == null) {
                val map = HashMap<String, String>()
                map["Authorization"] = userToken!!
                RetrofitUtil.instance.headerMap = map
            } else {
                RetrofitUtil.instance.headerMap?.put("Authorization", userToken!!)
            }
        }
    }

    fun clearUser() {
        Hawk.delete("user_info")
        MMKVUtils.removeKey(USER_TOKEN)
        userInfo = null
        userToken = null
        RetrofitUtil.instance.headerMap = null
        AppContext.getContext().sendBroadcast(Intent(AppContext.getContext().packageName + ".ACTION_LOGIN_STATE_CHANGED"))
//        MqttEngine.instance.notifyConfigChanged()
    }

    fun getLoginUser(): UserInfo? {
        userInfo?.let { return it }
        userInfo = Hawk.get("user_info")
        return userInfo
    }

    fun saveLoginUser(userInfo: UserInfo,isEvent:Boolean = true) {
        Hawk.put("user_info", userInfo)
        this.userInfo = userInfo
        if (isEvent){
            RxBus.get().post(ACTION_GET_LOGIN_USER_SUCCESS, userInfo)
        }
    }

    fun getToken(): String? {
        if (!userToken.isNullOrEmpty()) {
            return userToken
        }
        userToken = MMKVUtils.decodeString(USER_TOKEN)
        return userToken
    }

    fun saveUserToken(token: String?) {
        MMKVUtils.encode(USER_TOKEN, token)
        userToken = token
        if (!token.isNullOrEmpty()) {
            if (RetrofitUtil.instance.headerMap == null) {
                val map = HashMap<String, String>()
                map["Authorization"] = token
                RetrofitUtil.instance.headerMap = map
            } else {
                RetrofitUtil.instance.headerMap?.put("Authorization", token)
            }
//            Global.mqttUtil.initConfig()
            //极光推送进程不同，这里不能通过直接使用mqtt，会有跨进程访问无法绑定mqttclient service问题，改为静态广播发送
            AppContext.getContext().sendBroadcast(Intent(AppContext.getContext().packageName + ".ACTION_LOGIN_STATE_CHANGED"))
//            MqttEngine.instance.notifyConfigChanged()
        }
    }

    fun isLogin(): Boolean {
        return if (!userToken.isNullOrEmpty()) {
            true
        } else {
            val token = MMKVUtils.decodeString(USER_TOKEN)
            userToken = token
            token!!.isNotEmpty()
        }
    }

    fun checkIsGotoLogin(): Boolean {
        return if (isLogin()) {
            false
        } else {
            val userService: UserManagerService = ARouter.getInstance()
                .build(MainRouter.ServiceUserManager)
                .navigation() as UserManagerService;
            userService.startLogin()
            true
        }
    }

    fun getInvitationCode(): String? {
        if (!invitationCode.isNullOrEmpty()) {
            return invitationCode
        }
        invitationCode = MMKVUtils.decodeString(INVITATION_CODE)
        return invitationCode
    }

    fun saveInvitationCode(code: String?) {
        if (!code.isNullOrBlank()){
            MMKVUtils.encode(INVITATION_CODE, code)
        }
    }

}