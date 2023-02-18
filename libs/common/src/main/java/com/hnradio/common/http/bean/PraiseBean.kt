package com.hnradio.common.http.bean

/**
 *  点赞
 * created by qiaoyan on 2021/8/12
 */
data class PraiseBean(
    var userId: Int,
    var albumDetailId: Int,//专辑的
    var lifeId: Int,//铁粉生活的
    var isPraises: Boolean
)