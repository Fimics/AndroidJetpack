package com.hnradio.common.http.bean

import androidx.annotation.Keep

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-23 15:58
 * @Version: 1.0
 */
@Keep
data class UserInfo(
    val appleId: String,
    val city: String,
    val country: String,
    val province: String,
    val createTime: Long,
    val headImageUrl: String,
    val id: Int,
    val userSn: Int,
    val isBlack: Int,
    val isDelete: Int,
    val memo: String,
    val modifyTime: Long,
    val modifyUserId: String,
    val nickName: String,
    val phone: String,
    val sex: String,
    val tag: String,
    val token: String,
    val unionId: String,
    val userName: String,
    val weiboId: String,
    val weixinId: String,

//更新个人信息的字段
    val fansNum: Int,
    val flowNum: Int,
    val invitationCode: Int,
    val invitationNum: Int,
    val isBindWeibo: Int,
    val isBindWx: Int,
    val praiseNum: Int,
    val weiboNickName: String,
    val weixinNickName: String,
    val professorId:Int,
    val roleType:String,   //0和1都是专家

    val invitationUserCode:String?,   //邀请人的邀请码

    //新加
    /**车牌*/
    val carNum : String?,
    /**车号*/
    val carType : String?,

    /**是否实名0未认证 1认证*/
    val isVerified:String?,

    val idCardNo:String,
    /**头衔，后续添加*/
    val levelName:String,
    /**头衔图片，后续添加*/
    val levelImageUrl:String,
    /**需要核销但是没有核销的中奖记录数，我的奖品数*/
    val prizeNum : Int,
    /**专家问答数:用户新提问或追加提问而专家未回复的问答数，专家问答数*/
    val qaNumNeedReply : Int,
    /**专家已回复而用户未查看的问答数，我的问答数*/
    val qaNum : Int,
){
    fun getAllMsgCount() : Int{
        return prizeNum + qaNum + qaNumNeedReply
    }
}
