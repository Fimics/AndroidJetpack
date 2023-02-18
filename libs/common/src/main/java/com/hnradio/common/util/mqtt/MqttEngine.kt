package com.hnradio.common.util.mqtt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.google.gson.JsonObject
import com.hnradio.common.AppContext
import com.hnradio.common.http.CommonApiUtil
import com.hnradio.common.http.bean.MqttConfigBean
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.Global
import com.hnradio.common.util.L
import com.orhanobut.logger.Logger
import com.yingding.lib_net.bean.base.BaseResBean
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.properties.Delegates

/**
 * @author ytf
 * Created by on 2021/11/11 15:20
 */
class MqttEngine private constructor(){

    companion object{
        const val ChatChannel = "/message/"
        const val SystemChannel = "/system/"
        const val NotificationChannel = "/notification/"

        val instance : MqttEngine by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ MqttEngine() }
    }

    private var td : HandlerThread = HandlerThread("t")
    private var H : Handler by Delegates.notNull()
    private var mContext : Context by Delegates.notNull()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(context : Context) {
        mContext = context
        td.start()
        H = Handler(td.looper)
        mContext.registerReceiver(LoginStateChangedReceiver(),
            IntentFilter(mContext.packageName + ".ACTION_LOGIN_STATE_CHANGED"))
    }

    private var mClient: MqttAndroidClient? = null
    private var mConfig: MqttConfigBean? = null

    private var mClientId: String? = null
    private var mEndPoint = ""

    private var currentRoomId = 0
    private var hasSubscribed = false

    fun start(liveRoomId : Int){
        currentRoomId = liveRoomId
        if(isAlive.get()){
            msgCallback?.onConnectState(true, "")
            if(!hasSubscribed){
                subscript(currentRoomId)
            }
        }else{
            getConfigAndConnect()
        }
    }

    fun stop(liveRoomId : Int){
        unSubscript(liveRoomId)
        currentRoomId = 0
    }

    fun notifyConfigChanged(){
        if (UserManager.isLogin()){
            if(!isAlive.get()){
                getConfigAndConnect()
            }
        }else{
            disconnect()
        }
    }

    private fun getConfigAndConnect(){
        CommonApiUtil.getMqttConfig({
            mConfig = it.data
            mConfig?.let { config->
                mClientId = "${config.groupId}@@@${System.currentTimeMillis()}${UserManager.getLoginUser()?.unionId}"
                mEndPoint = config.endPoint
                dontRetry = false
                connect()
            }
        }, {
            H.postDelayed({getConfigAndConnect()}, 2000)
        })
    }

    val isAlive = AtomicBoolean(false)
    private var isConnecting = false

    private val mLock = ReentrantLock(true)

    private fun connect(){
        if(isAlive.get() || isConnecting || mClientId == null){
            L.e("isAlive=${isAlive}, isConnecting = ${isConnecting} mClientId= ${mClientId}")
            return
        }

        isConnecting = true

        try {
            val mqttConnectOptions = MqttConnectOptions().also {
                it.connectionTimeout = 3000
                it.keepAliveInterval = 60
                it.isAutomaticReconnect = false
                it.isCleanSession = true
                it.userName = "Signature|" + mConfig?.accessKey.toString() + "|" + mConfig?.instanceId
                it.password = macSignature(mClientId ?: "", mConfig?.secretKey ?: "").toCharArray()
            }
            L.e("Mqtt开始连接")
            mClient = MqttAndroidClient(Global.application, "tcp://${mEndPoint}:1883", mClientId)
            mClient?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    L.e("${cause?.message}")
                    isAlive.set(false)
                    hasSubscribed = false
                    isConnecting = false
                    L.e("Mqtt连接断开==>${cause?.stackTraceToString()}")
                    if(!dontRetry){
                        H.postDelayed({ connect() }, 3000)
                    }
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    L.e("接收到消息：topic: $topic \n message: ${String(message.payload)}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    L.e("消息发送成功：${String(token.message.payload)}")
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                }
            })

            mClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    L.e("mqtt连接成功：${asyncActionToken.isComplete}")
                    isAlive.set(true)
                    isConnecting = false
                    //连接成功了就马上订阅
                    if(!hasSubscribed){
                        H.postDelayed({subscript(currentRoomId)}, 300)
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    L.e("mqtt连接失败：${exception.stackTraceToString()}")
                    isAlive.set(false)
                    isConnecting = false
                    hasSubscribed = false
                    if(!dontRetry){
                        H.postDelayed({ connect() }, 1500)
                    }
                }
            })
        } catch (e: Exception) {
            isAlive.set(false)
            isConnecting = false
            hasSubscribed = false
            L.e("mqtt连接异常：${e.stackTraceToString()}")
        }
    }

    private var dontRetry = false
    private fun disconnect(){
        if(!isAlive.get()) return

        mClientId = null
        mEndPoint = ""
        isConnecting = false
        hasSubscribed = false
        dontRetry = true
        try {
            mLock.lock()
            isAlive.set(false)
            mClient?.unregisterResources()
            mClient?.close()
            mClient = null
            L.e("关闭mqtt连接")
            mLock.unlock()
        }catch (e : Exception){
            isAlive.set(false)
        }
    }

    //mqtt 设置订阅
    private fun subscript(liveRoomId: Int) {

        if(!isAlive.get()){
            L.e("Mqtt未启动 subscript")
            return
        }
        if(liveRoomId == 0) return

        try {
            val topicFilter = arrayOf(
                "${mConfig?.parentTopic}$ChatChannel$liveRoomId",
                "${mConfig?.parentTopic}$SystemChannel$liveRoomId",
                "${mConfig?.parentTopic}$NotificationChannel$liveRoomId"
            )
            val qos = intArrayOf(1, 1, 1)
            val messageListeners = arrayOf(mqttMessageListener, mqttMessageListener, mqttMessageListener)
            mClient?.subscribe(topicFilter, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    L.e("mqtt订阅成功：${asyncActionToken}")
                    hasSubscribed = true
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    L.e("mqtt订阅失败：${exception?.stackTraceToString()}")
                }
            }, messageListeners)
        } catch (ex: MqttException) {
            ex.printStackTrace()
            L.e("mqtt订阅失败：${ex.stackTraceToString()}")
        }
    }

    private val mqttMessageListener = IMqttMessageListener { topic, message ->
        L.e("MQTT 接收消息topic：$topic： ${String(message.payload)}")

        if(topic != null && message != null){
            when{
                topic.contains(ChatChannel) -> {
                    mainHandler.post {
                        msgCallback?.onChatChannelMsg(message)
                    }
                }
                topic.contains(SystemChannel) ->{
                    mainHandler.post {
                        msgCallback?.onSystemChannelMsg(message)
                    }
                }
                topic.contains(NotificationChannel) ->{
                    mainHandler.post {
                        msgCallback?.onNotiChannelMsg(message)
                    }
                }
            }
        }
    }

    //取消订阅
    private fun unSubscript(liveRoomId: Int) {

        if(!isAlive.get()){
            L.e("Mqtt未启动, unSubscript")
            return
        }
        try {
            val topicFilter = arrayOf(
                "${mConfig?.parentTopic}$ChatChannel$liveRoomId",
                "${mConfig?.parentTopic}$SystemChannel$liveRoomId",
                "${mConfig?.parentTopic}$NotificationChannel$liveRoomId"
            )
            mClient?.unsubscribe(topicFilter, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    L.e("取消订阅成功")
                    hasSubscribed = false
                    currentRoomId = 0
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    L.e("取消订阅失败 ==> ${exception?.stackTraceToString()}")
                    currentRoomId = 0
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun registerCallback(msgCallback : MsgCallback){
        this.msgCallback = msgCallback
    }

    fun unregisterCallback(){
        msgCallback = null
    }

    /**
     *发布消息
     * @param messageStr 消息体
     * @param liveRoomId 直播间id
     * @param msgType 消息类型 (1 文本 2 图片 3 语音 4 视频 100 自定义)
     * @param channelType (1：聊天消息(Message) 2：系统消息(System) 3: 通知消息(Notification)
     * */
    fun publishMessage(
        messageStr: String,
        liveRoomId: Int,
        channelType: Int = 1,
        msgType: Int = 1,
        attach: JsonObject? = JsonObject(),
        contentHandler : ((content : JsonObject)->Unit)? = null,
        success: ((BaseResBean<*>?) -> Unit)? = null
    ) {
        if (!UserManager.isLogin())
            return

        val param = JsonObject()
        param.addProperty("channelType", channelType)
        param.addProperty("roomId", liveRoomId)

        val messageParam = JsonObject()

        val from = JsonObject()
        from.addProperty("userId", UserManager.getLoginUser()?.id)
        from.addProperty("userName", UserManager.getLoginUser()?.nickName ?: "")
        from.addProperty("userAvatar", UserManager.getLoginUser()?.headImageUrl ?: "")


        val content = JsonObject()
        content.addProperty("msgType", msgType)
        content.addProperty("channelType", channelType)
        content.addProperty("roomId", liveRoomId)
        content.addProperty("text", messageStr)
        attach?.let {
            content.add("attach", it)
        }

        contentHandler?.invoke(content)

        messageParam.add("from", from)
        messageParam.add("content", content)



        param.addProperty("message", messageParam.toString())

        CommonApiUtil.mqttSendMessage(param = param.toString()) {
            success?.invoke(it)
        }
    }


    private var msgCallback : MsgCallback? = null

    interface MsgCallback{

        fun onConnectState(ok : Boolean, reasonIfErr : String)

        fun onSubUnSubState(isSubscribe : Boolean, isSuccess : Boolean)

        fun onChatChannelMsg(msg : MqttMessage)

        fun onSystemChannelMsg(msg : MqttMessage)

        fun onNotiChannelMsg(msg : MqttMessage)
    }
}