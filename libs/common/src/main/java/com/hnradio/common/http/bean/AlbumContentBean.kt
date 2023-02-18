package com.hnradio.common.http.bean

/**
 * 专辑内容
 * created by qiaoyan on 2021/7/29
 */
data class AlbumContentBean(
    var id: Int,
    var albumInfoId: Int,
    var mediaType: Int,
    var name: String,
    var subName: String,
    var imageUrl: String,
    var url: String,
    var descirb: String?,
    var pv: Int,//浏览数
    var mediaLength: Int,
    var forwardNum: Int,
    var commentNum: Int,
    var praiseNum: Int,
    var tag: String,
    var createTime: String,
    var shareDescrib: String,
    var shareImageUrl: String,
    var shareTitle: String,
    var shareUrl: String,
    var isPraises: Boolean,
    //自定义 序号
    var orderNum: Int

)
