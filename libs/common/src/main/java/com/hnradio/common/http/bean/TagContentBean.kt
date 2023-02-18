package com.hnradio.common.http.bean

import com.hnradio.common.util.OSSImageInfoGetter


/**
 * 频道标签内容
 * created by qiaoyan on 2021/7/22
 */
data class TagContentBean(
    val id: Int,
    val title: String,
//    var imageUrl: String,
    val praiseNum: Int,//赞
    val headImageUrl: String,
    val nickName: String,//主播名称
    val type: Int,
    val levelName : String,
    val levelImageUrl : String,
    //自己加的
//    var imageWidth: Int,
//    var imageHeight: Int
) : OSSImageInfoGetter.IOssImageFeature()