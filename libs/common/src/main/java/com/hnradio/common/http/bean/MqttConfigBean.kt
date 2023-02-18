package com.hnradio.common.http.bean

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.http.bean
 * @ClassName: MqttConfigBean
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/8/8 10:03 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/8 10:03 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */data class MqttConfigBean(
    val accessKey: String,
    val endPoint: String,
    val groupId: String,
    val instanceId: String,
    val parentTopic: String,
    val port: String,
    val secretKey: String
)