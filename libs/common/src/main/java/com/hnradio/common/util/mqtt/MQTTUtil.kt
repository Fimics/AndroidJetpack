package com.hnradio.common.util.mqtt

import com.google.gson.JsonObject
import com.hnradio.common.http.CommonApiUtil
import com.hnradio.common.http.bean.MqttConfigBean
import com.hnradio.common.manager.UserManager.getLoginUser
import com.hnradio.common.manager.UserManager.isLogin
import com.hnradio.common.util.Global
import com.orhanobut.logger.Logger
import com.yingding.lib_net.bean.base.BaseResBean
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject


/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: MQTTUtil
 * @Description: MQTT辅助类
 * @Author: shaoguotong
 * @CreateDate: 2021/8/9 11:44 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/9 11:44 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class MQTTUtil {
    val chatChannel: String = "/message/"
    val systemChannel: String = "/system/"
    val notificationChannel: String = "/notification/"

    private var mqttClient: MqttAndroidClient? = null

    private var mqttConfigBean: MqttConfigBean? = null

    private var clientId: String? = null

    //初始化mqtt config
    fun initConfig() {
        if (mqttClient != null)
            mqttClient?.unregisterResources()
        if (isLogin())
            CommonApiUtil.getMqttConfig({
                mqttConfigBean = it.data
                clientId =
                    "${mqttConfigBean?.groupId}@@@${System.currentTimeMillis()}${getLoginUser()?.unionId}"
                mqttClient =
                    MqttAndroidClient(
                        Global.application,
                        "tcp://${mqttConfigBean?.endPoint}:1883",
                        clientId
                    )
                setCallBack()
                connect()
            }, {

            })
    }

    //设置MQTT回调
    private fun setCallBack() {
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                cause?.printStackTrace()
                Logger.e("${cause?.message}")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Logger.d("接收到消息：topic: $topic \n message: ${String(message.payload)}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Logger.d("消息发送成功：${String(token.message.payload)}")
            }

        })
    }

    //开始连接
    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.connectionTimeout = 3000
        mqttConnectOptions.keepAliveInterval = 60
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = true
        try {
            // 参考 https://help.aliyun.com/document_detail/54225.html
            // Signature 方式
            mqttConnectOptions.userName =
                "Signature|" + mqttConfigBean?.accessKey.toString() + "|" + mqttConfigBean?.instanceId
            mqttConnectOptions.password =
                macSignature(clientId ?: "", mqttConfigBean?.secretKey ?: "").toCharArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mqttClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Logger.w("mqtt连接成功：${asyncActionToken}")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    exception.printStackTrace()
                    Logger.w("mqtt连接失败：${exception.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    var subscriptCallBack: ((String, MqttMessage) -> Unit)? = null

    //被订阅直播间idlist
    private val liveRoomIdList = mutableListOf<Int>()

    //mqtt 设置订阅
    fun setSubscript(liveRoomId: Int, callBack: (topic: String, message: MqttMessage) -> Unit) {
        if (!isLogin())
            return
        liveRoomIdList.add(liveRoomId)
        try {
            subscriptCallBack = callBack
            val topicFilter = arrayOf(
                "${mqttConfigBean?.parentTopic}$chatChannel$liveRoomId",
                "${mqttConfigBean?.parentTopic}$systemChannel$liveRoomId",
                "${mqttConfigBean?.parentTopic}$notificationChannel$liveRoomId"
            )
            val qos = intArrayOf(1, 1, 1)
            val messageListeners = arrayOf(
                iMqttMessageListener,
                iMqttMessageListener,
                iMqttMessageListener
            )
            mqttClient?.subscribe(topicFilter, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Logger.w("mqtt连接成功：${asyncActionToken}")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable?) {
                    exception?.printStackTrace()
                    Logger.w("mqtt连接失败：${exception?.message}")
                }
            }, messageListeners)
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private val iMqttMessageListener =
        IMqttMessageListener { topic, message ->
            Logger.e("MQTT 接收消息topic：$topic： ${String(message.payload)}")
            subscriptCallBack?.let {
                it(topic, message)
            }
        }

    //取消所有订阅
    fun unSubscriptAll() {
        liveRoomIdList.forEach {
            unSubscript(it)
        }
    }

    //取消订阅
    fun unSubscript(liveRoomId: Int) {
        if (!isLogin())
            return
        try {
            val topicFilter = arrayOf(
                "${mqttConfigBean?.parentTopic}$chatChannel$liveRoomId",
                "${mqttConfigBean?.parentTopic}$systemChannel$liveRoomId",
                "${mqttConfigBean?.parentTopic}$notificationChannel$liveRoomId"
            )
            mqttClient?.unsubscribe(topicFilter, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
//                    Logger.e("MQTT 取消订阅 成功： $asyncActionToken")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
//                    Logger.e("MQTT 取消订阅 失败： ${exception.message}")
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
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
        if (!isLogin())
            return

        val param = JsonObject()
        param.addProperty("channelType", channelType)
        param.addProperty("roomId", liveRoomId)

        val messageParam = JsonObject()

        val from = JsonObject()
        from.addProperty("userId", getLoginUser()?.id)
        from.addProperty("userName", getLoginUser()?.nickName ?: "")
        from.addProperty("userAvatar", getLoginUser()?.headImageUrl ?: "")


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
}