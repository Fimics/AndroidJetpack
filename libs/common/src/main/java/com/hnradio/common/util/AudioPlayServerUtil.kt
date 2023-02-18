package com.hnradio.common.util

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.hnradio.common.constant.CommonBusEvent
import com.hnradio.common.service.AudioPlayService
import com.hwangjr.rxbus.RxBus

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: AudioPlayServerUtil
 * @Description: 音频后台播放辅助类
 * @Author: shaoguotong
 * @CreateDate: 2021/9/27 7:53 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/9/27 7:53 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class AudioPlayServerUtil {
    companion object {
        var playService: AudioPlayService? = null
        private var callBack: ((AudioPlayService) -> Unit)? = null

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                val playBinder = binder as AudioPlayService.AudioPlayBinder
                playService = playBinder.getService() as AudioPlayService
                callBack?.invoke(playService!!)
            }

            override fun onServiceDisconnected(p0: ComponentName?) {

            }
        }

        //开启服务
        fun startServer(onServiceConnected: (AudioPlayService) -> Unit) {
            this.callBack = onServiceConnected
            if (playService == null) {
                val intent = Intent(Global.application, AudioPlayService::class.java)
                Global.application.bindService(
                    intent,
                    serviceConnection,
                    AppCompatActivity.BIND_AUTO_CREATE
                )
            } else {
                callBack?.invoke(playService!!)
            }
        }

        //关闭服务
        fun closeServer() {
            if(playService!=null) {
                playService?.pause()
                playService?.cancelNotification()
                Global.application.unbindService(serviceConnection)
                playService = null
                RxBus.get().post(CommonBusEvent.RX_BUS_UPDATE_AUDIO_CONTROL, "hide")
            }
        }
    }
}