package com.hnradio.common.http.bean

import com.hnradio.common.util.OSSImageInfoGetter

/**
 * 主播 发布内容
 * created by qiaoyan on 2021/8/3
 */
data class AnchorContentBean(
    val id: Int,
    val createTime: Long,
    val title: String,
//    var imageUrl: String,
    val type: Int,
    val textContent: String,
    val topicId: Int,
    val topicName: String,
    val images: String,
    val videoUrl: String,
    val praiseNum: Int,
    val commentNum: Int,
    val forwardNum: Int,
    val countNum: Int,
    val isHot: Int,
    val location: String,
    val longitude: Double,
    val latitude: Double,
    val userId: Int,
    val tagId: Int,
    val linkType: Int,
    val linkId: Int,
    val linkUrl: String,
    val linkTitle: String,
    val userHeadImageUrl: String,
    val userNickName: String,
    val isPraises: Boolean,
    val isFans: Boolean,
    val levelName : String,
    val levelImageUrl : String,
    //自己加的
//    var imageWidth: Int,
//    var imageHeight: Int
): OSSImageInfoGetter.IOssImageFeature()

