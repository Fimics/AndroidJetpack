package com.hnradio.common.http.bean

/**
 *  关注
 * created by qiaoyan on 2021/8/14
 */
data class FollowBean(
    var userId: Int,
    val fansId: Int,//铁粉生活的
    var isFans: Boolean
)