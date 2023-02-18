package com.hnradio.common.http.bean

import com.hnradio.common.util.TimeUtils

/**
 *  评论
 * created by qiaoyan on 2021/8/12
 */
data class CommentBean(
    var id: Int,
    var userId: Int,
    var albumDetailId: Int,//专辑的
    var tfLifeId: Int,//铁粉生活的
    var parentId: Int,
    var text: String,
    var nickName: String,
    var headImageUrl: String,
    var createTime: Long,
    var replyNum: Int,
    var children: ArrayList<CommentBean>,
    var levelName : String,
    var levelImageUrl : String,
    var targetUserNickName : String?,
    var targetUserHeadImageUrl : String?,
)
