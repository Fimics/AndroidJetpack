package com.hnradio.common.http.bean

import android.text.TextUtils
import java.lang.NumberFormatException

/**
 * 铁粉生活详情
 */
data class IronFansLifeBean(
    val id: Int,
    val createTime: Long,
    val title: String,
    val imageUrl: String,
    val type: Int,
    val textContent: String,
    val topicId: Int,
    val topicName: String?,
    val images: String,
    val videoUrl: String,
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
    val linkUrl: String?,
    val linkTitle: String?,
    val isPass: Int,
    val shareDescrib: String,
    val shareImageUrl: String,
    val shareTitle: String,
    val shareUrl: String,
    val userHeadImageUrl: String,
    val userNickName: String,
    var isPraises: Boolean,
    /**审核拒绝理由*/
    val unPassReason : String?,
    /**不上热门理由*/
    val unHotReason : String?,
    val levelName : String,
    val levelImageUrl : String,
){
    var praiseNum: Int=0
    var commentNum: Int=0
    var isFans: Boolean=false

    //获取统一格式的点赞，评论，分享数
    fun getStringByNumber(numberStr: String, oldStr: String?): String? {
        var number = 0
        if (!TextUtils.isEmpty(numberStr)) number = try {
            numberStr.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return numberStr
        }
        if (number <= 0) return oldStr else if (number > 9999) {
            val yu = number / 1000
            val str = yu.toString()
            return str.substring(0, str.length - 1) + "." + str.substring(str.length - 1) + "w"
        }
        return number.toString()
    }

    fun isPassed() : Boolean{
        return 1 == isPass
    }

    fun isHoted() : Boolean{
        return 1 == isHot
    }

    fun unpassReason() : String{
        return unPassReason?:""
    }

    fun unhotReason() : String{
        return unHotReason?:""
    }
}
