package com.hnradio.common.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.source.UrlSource
import com.hnradio.common.R
import com.hnradio.common.constant.CommonBusEvent
import com.hnradio.common.receiver.NotificationBroadcastReceiver
import com.hnradio.common.util.Global
import com.hnradio.common.util.ToastUtils
import com.hnradio.common.util.mqtt.MqttEngine
import com.hnradio.common.widget.player.AliyunRenderView
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.orhanobut.logger.Logger

/**
 * 音频后台播放服务
 * created by 乔岩 on 2021/8/24
 */
class AudioPlayService : Service() {

    private var currentTime = 0L //当前时间 毫秒
    var totalTime = 0L //总时间
    var currentStatus = 0 //当前播放状态

    var viewType: Int? = -1 // 0:AudioPlayActivity 1:AllRadioStationActivity
    var id: Int? = -1
    var url: String? = null
    var imageUrl: String? = null //图片
    var name: String? = null //名称
    var subName: String? = null

    private var aliRenderView: AliyunRenderView? = null
    private var onAudioPlayListener: OnAudioPlayListener? = null

    private val channelId = "channelId"
    private val channelName = "channelName"
    private lateinit var notificationManager: NotificationManager
    private var isCancelNotification = false //当关闭通知后 会暂停音乐，当再次开始时  要新建通知

    override fun onBind(intent: Intent): IBinder {
        return AudioPlayBinder()
    }

    override fun onCreate() {
        super.onCreate()
        //创建播放器
        Logger.d("create audio background service")
        RxBus.get().register(this)
        aliRenderView = AliyunRenderView(this)
        aliRenderView?.apply {
            setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {
                override fun onLoadingBegin() {
//                    Logger.d("开始加载")
                }

                override fun onLoadingProgress(p0: Int, p1: Float) {
//                    Logger.d("加载进度 p0 = $p0 , p1 = $p1")
                }

                override fun onLoadingEnd() {
//                    Logger.d("结束加载")
                }

            })
            setOnPreparedListener {
                Logger.d("准备完成")
                totalTime = duration
                onAudioPlayListener?.onAudioPrepared(totalTime)
            }
            setOnStateChangedListener {
                Logger.d("当前状态 = $it")
                currentStatus = it
                if (currentStatus == 3) {//正在播放
                    onAudioPlayListener?.onAudioPlay()
                } else if (currentStatus == 2 || currentStatus == 4 || currentStatus == 6) {//2:  4:暂停  6播放完成
                    onAudioPlayListener?.onAudioPause()
                }

            }
            setOnInfoListener {
//                Logger.d("当前info = ${it.extraValue} ==========  ${it.code} ======== ${it.extraMsg}")
                when (it.code) {
                    InfoCode.CurrentPosition -> {
                        currentTime = it.extraValue
                        onAudioPlayListener?.onAudioProgressChanged(currentTime)
                    }
                    else -> {
                    }
                }
            }
            setOnCompletionListener {
                aliRenderView?.seekTo(0, IPlayer.SeekMode.Inaccurate)
                aliRenderView?.start()
            }
            setOnErrorListener {
                ToastUtils.show("播放出错  ${it.code}")
            }
            //初始化通知
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.d("onUnbind audio background service")
        aliRenderView?.stop()
        aliRenderView?.release()
        cancelNotification()
        if (viewType == 2) {
            //直播间播放退出直播间
//            Global.mqttUtil.publishMessage("", id ?: 0, 3, 301)
            MqttEngine.instance.publishMessage("", id?:0, 3, 301)
        }
        id = -1
        url = ""
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
        Logger.d("destroy audio background service")
    }

    @Subscribe(tags = [Tag(CommonBusEvent.RX_BUS_STOP_AUDIO)])
    fun onReceiveStopAudio(tag: String) {
        pause()
        isCancelNotification = true
    }

    /**
     * 设置播放源
     */
    fun setDataSource(
        viewType: Int,
        id: Int,
        url: String,
        imageUrl: String,
        name: String,
        subName: String
    ) {
        currentTime = 0
        this.viewType = viewType
        this.id = id
        this.url = url
        this.imageUrl = imageUrl
        this.name = name
        this.subName = subName
        //设置音频
        val urlSource = UrlSource()
        urlSource.uri = url
        aliRenderView?.setDataSource(urlSource)
        aliRenderView?.prepare()
        aliRenderView?.start()
        //显示通知
        startForeground(1, getNotification())
        isCancelNotification = false
    }

    /**
     * 播放或暂停
     */
    fun playOrPause() {
        when (currentStatus) {
            3 -> {//正在播放
                aliRenderView?.pause()
            }
            2, 4 -> {//暂停
                aliRenderView?.start()
            }
        }
        if (isCancelNotification) {
            startForeground(1, getNotification())
            isCancelNotification = false
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        aliRenderView?.pause()
    }

    //开始播放
    fun start() {
        when (currentStatus) {
            2, 4 -> {//暂停
                aliRenderView?.start()
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        aliRenderView?.stop()
        aliRenderView?.release()
    }

    /**
     * 设置倍速
     */
    fun setSpeed(speed: Float) {
        aliRenderView?.setSpeed(speed)
    }

    /**
     * 快进15面
     */
    fun fastForward15() {
        aliRenderView?.seekTo(
            if (currentTime + 15000L < totalTime) currentTime + 15000L else totalTime,
            IPlayer.SeekMode.Inaccurate
        )
    }

    /**
     * 快退15秒
     */
    fun remind15() {
        aliRenderView?.seekTo(
            if (currentTime - 15000L > 0) currentTime - 15000L else 0,
            IPlayer.SeekMode.Inaccurate
        )
    }

    /**
     * 跳到
     */
    fun seekTo(position: Long) {
        aliRenderView?.seekTo(
            position,
            IPlayer.SeekMode.Inaccurate
        )
        if (currentStatus != 3) {
            aliRenderView?.start()
        }
    }

    /**
     * 关闭通知
     */
    fun cancelNotification() {
        stopForeground(true)
    }

    /**
     * 设置监听
     */
    fun setOnAudioPlayListener(onAudioPlayListener: OnAudioPlayListener) {
        this.onAudioPlayListener = onAudioPlayListener
    }


    private fun getNotification(): Notification {
        val builder = Notification.Builder(this)
        builder.apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(name)
            setContentText(subName)
            //设置跳转 因为用了路由 所以通过广播跳转
            val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
            intent.action = "notification_click"
            intent.putExtra("viewType", viewType)
            intent.putExtra("id", id)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val pendingIntent =
                PendingIntent.getBroadcast(
                    baseContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            setContentIntent(pendingIntent)
            //关闭通知后  结束
            val deleteIntent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
            deleteIntent.action = "notification_cancel"
            deleteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val deletePendingIntent =
                PendingIntent.getBroadcast(
                    baseContext,
                    1,
                    deleteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            setDeleteIntent(deletePendingIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId)
        }
        return builder.build()
    }

    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    private fun initCallingStateListener() {

        phoneStateListener = object : PhoneStateListener() {
            @SuppressLint("MissingPermission")
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                super.onCallStateChanged(state, incomingNumber)

                when (state) {
                    TelephonyManager.CALL_STATE_IDLE // 待机，即无电话时，挂断时触发
                    -> {
//                        Log.i(TAG, "callingState：挂断，对方手机号$incomingNumber")

                    }

                    TelephonyManager.CALL_STATE_RINGING // 响铃，来电时触发
                    -> {
//                        Log.i(TAG, "callingState：来电，对方手机号$incomingNumber")
                    }

                    TelephonyManager.CALL_STATE_OFFHOOK // 摘机，接听或打电话时触发
                    -> {
//                        Log.i(TAG, "callingState：拨打电话，对方手机号$incomingNumber")

                    }

                    else -> {
//                        Log.i(TAG, "callingState：其他")
                    }
                }
            }
        }

        // 设置来电监听器
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager?
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    /**
     * 音频播放监听
     */
    interface OnAudioPlayListener {

        fun onAudioPrepared(totalTime: Long)

        fun onAudioProgressChanged(currentTime: Long)

        fun onAudioPlay()

        fun onAudioPause()
    }

    inner class AudioPlayBinder : Binder() {

        fun getService(): Service {
            return this@AudioPlayService
        }

    }
}