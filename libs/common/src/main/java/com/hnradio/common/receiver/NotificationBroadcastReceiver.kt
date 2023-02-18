package com.hnradio.common.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hnradio.common.constant.CommonBusEvent
import com.hnradio.common.router.RouterUtil
import com.hwangjr.rxbus.RxBus
import com.orhanobut.logger.Logger

/**
 *  音频播放的通知 跳转界面
 * created by qiaoyan on 2021/9/5
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("接收到的 = ${intent.action}")
        when (intent.action) {
            "notification_click" -> {//点击通知
                when (intent.getIntExtra("viewType", -1)) {
                    0 -> {//音频播放
                        RouterUtil.gotoAudioPlayActivity(intent.getIntExtra("id", -1))
                    }
                    1 -> {//全部电台
                        RouterUtil.gotoAllRadioStationActivity()
                    }
                    2 -> {//直播间
                        RouterUtil.gotoLivePlay(intent.getIntExtra("id", -1))
                    }
                }
            }
            "notification_cancel" -> {//删除通知
                RxBus.get().post(CommonBusEvent.RX_BUS_STOP_AUDIO,"")
            }
        }
    }
}