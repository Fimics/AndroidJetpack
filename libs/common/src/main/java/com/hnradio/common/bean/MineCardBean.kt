package com.hnradio.common.bean

/**
 * 我的卡券
 * Created by liguangze on 2021/7/23.
 */
data class MineCardBean(
    val describ: String,
    val eDate: String,
    val edate: Long,
    val getDate: String,
    val goodId: Int,
    val id: Int,
    val couponId: Int,
    val isValid: Int,
    val name: String,
    val orderId: Int,
    val price: Long,
    val sDate: String,
    val sdate: Long,
    val userId: Int,
    var isCheck:Boolean
)

