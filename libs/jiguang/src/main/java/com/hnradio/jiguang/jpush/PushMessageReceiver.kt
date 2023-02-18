package com.hnradio.jiguang.jpush

import android.app.Activity
import android.content.Context
import android.content.Intent
import cn.jpush.android.api.*
import cn.jpush.android.service.JPushMessageReceiver
import com.google.gson.Gson
import com.hnradio.common.http.bean.PushExtrasBean
import com.hnradio.common.router.NoticeIntentUtil
import com.hnradio.common.router.RouterUtil
import com.hnradio.common.util.Global
import com.orhanobut.logger.Logger

/**
 *
 * @Description: 极光推送接收
 * @Author: huqiang
 * @CreateDate: 2021-07-12 15:54
 * @Version: 1.0
 */
class PushMessageReceiver : JPushMessageReceiver() {

    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Logger.d("[onMessage] $customMessage")
    }

    override fun onNotifyMessageOpened(context: Context, message: NotificationMessage) {
        val str = message.notificationExtras
        if (!str.isNullOrBlank()) {
            val extras = Gson().fromJson(str, PushExtrasBean::class.java)
            if (Global.mainIsActive) {
                NoticeIntentUtil.startView(context, extras)
            } else {
                RouterUtil.gotoMainFromPush(
                    extras,
                    context as Activity
                )
            }
        }

    }

    override fun onMultiActionClicked(context: Context?, intent: Intent) {
        val nActionExtra = intent.extras!!.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
        Logger.d("[onMultiActionClicked] $nActionExtra")
    }

    override fun onNotifyMessageArrived(context: Context?, message: NotificationMessage) {
        Logger.d("[onNotifyMessageArrived] $message")
    }

    override fun onNotifyMessageDismiss(context: Context?, message: NotificationMessage) {

    }

    override fun onRegister(context: Context, registrationId: String) {
        Logger.d("[onRegister] $registrationId")
    }

    override fun onConnected(context: Context?, isConnected: Boolean) {
        Logger.d("[onConnected] $isConnected")
    }

    override fun onCommandResult(context: Context?, cmdMessage: CmdMessage) {
        Logger.d("[onCommandResult] $cmdMessage")
    }

    override fun onTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onTagOperatorResult(context, jPushMessage)
    }

    override fun onCheckTagOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onCheckTagOperatorResult(context, jPushMessage)
    }

    override fun onAliasOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        if (jPushMessage!!.errorCode == 0) {
            Logger.d("onAliasOperatorResult success${jPushMessage.toString()}")
        } else {
            Logger.d("Failed to modify alias, errorCode:${jPushMessage!!.errorCode}")
        }
    }

    override fun onMobileNumberOperatorResult(context: Context?, jPushMessage: JPushMessage?) {
        super.onMobileNumberOperatorResult(context, jPushMessage)
    }

    override fun onNotificationSettingsCheck(context: Context?, isOn: Boolean, source: Int) {
        super.onNotificationSettingsCheck(context, isOn, source)
        Logger.d("[onNotificationSettingsCheck] isOn:$isOn,source:$source")
    }

}