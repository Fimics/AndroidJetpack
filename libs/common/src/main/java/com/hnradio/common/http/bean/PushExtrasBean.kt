package com.hnradio.common.http.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-26 09:57
 * @Version: 1.0
 */
@Parcelize
data class PushExtrasBean(
    val messageType: Int,//0:系统消息 1：用户消息, 12
    val linkType: Int,
    val linkId: Int?,
    val linkUrl: String?,
    val linkAppId: String?
) : Parcelable{

    fun isSystemMsg() : Boolean{
        return 0 == messageType
    }

    fun isUserMsg() : Boolean{
        return 1 == messageType
    }

    /**7-取消热门提醒 8-下架提醒 9-上架失败审核通知，都跳转到铁粉生活对应的作品主页*/
    fun isLifeTypeMsg() : Boolean{
        return 7 == messageType || 8 == messageType || 9 == messageType
    }

    /**是用户提问或是追加提问的时候通知专家的消息*/
    fun isExpertAnswerMsg() : Boolean{
        return 12 == messageType
    }
}
