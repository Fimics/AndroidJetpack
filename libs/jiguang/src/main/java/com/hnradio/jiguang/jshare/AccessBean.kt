package com.hnradio.jiguang.jshare

/**
 *
 * @Description: 认证数据
 * @Author: huqiang
 * @CreateDate: 2021-07-14 16:29
 * @Version: 1.0
 */
data class AccessBean(
    var token: String? = null,
    var refeshToken: String? = null,
    var openid: String? = null,
    var expiresIn: Number? = null,
    val nickName: String? = null,
    val imageUrl: String? = null,
    val gender: Int? = null,
    val errorCode:  Int? = 0,
    val errorMsg: String? = null

) {
    override fun toString(): String {
        return "AccessBean(token=$token, refeshToken=$refeshToken, openid=$openid, expiresIn=$expiresIn, nickName=$nickName, imageUrl=$imageUrl, gender=$gender, errorCode=$errorCode, errorMsg=$errorMsg)"
    }
}
