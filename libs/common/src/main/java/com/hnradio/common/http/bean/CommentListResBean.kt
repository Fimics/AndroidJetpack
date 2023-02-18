package com.hnradio.common.http.bean

/**
 *  评论列表
 * created by qiaoyan on 2021/7/29
 */
data class CommentListResBean(
    var records: ArrayList<CommentBean>,
    var total: Int,
    var size: Int,
    var current: Int,
    var orders: ArrayList<String>,
    var searchCount: Boolean,
    var pages: Int
)
