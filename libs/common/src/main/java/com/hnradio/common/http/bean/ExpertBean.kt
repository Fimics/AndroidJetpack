package com.hnradio.common.http.bean
/**
 * 专家
 * created by qiaoyan on 2021/8/14
 */
data class ExpertBean(
    var id: Int,
    var name: String,
    var imageUrl: String,
    var describ: String,
    var tagId: Int,
    var tagName: String,
    var answeredNum: Int
)