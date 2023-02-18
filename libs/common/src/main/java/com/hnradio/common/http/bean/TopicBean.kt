package com.hnradio.common.http.bean

/**
 *  话题
 * created by qiaoyan on 2021/8/13
 */
data class TopicBean(
    var id: Int,
    var topicName: String,
    var createTime: String,
    var pv: Int
)
