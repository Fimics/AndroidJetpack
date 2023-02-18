package com.hnradio.common.constant

/**
 *
 * created by qiaoyan on 2021/8/24
 */
object CommonBusEvent {

    //显示音频播放控制器
    const val RX_BUS_UPDATE_AUDIO_CONTROL = "rx_bus_update_audio_control"

    /**未读消息数*/
    const val RX_BUS_UPDATE_UnREAD_MSG = "rx_bus_update_unread_msg"

    //停止后台播放音频
    const val RX_BUS_STOP_PLAY_SERVICE_AUDIO = "rx_bus_stop_play_service_audio"

    //提问成功
    const val RX_BUS_ASK_SUCCESS = "rx_bus_ask_success"

    //专家回复成功
    const val RX_BUS_REPLY_SUCCESS = "rx_bus_reply_success"

    //主播主页信息变化 点赞 关注等
    const val RX_BUS_ANCHOR_INFO_CHANGED = "rx_bus_anchor_info_changed"

    //投票成功
    const val RX_BUS_VOTE_SUCCESS = "rx_bus_vote_success"

    //刷新用户信息
    const val RX_BUS_UPDATE_USER_INFO = "rx_bus_update_user_info"

    //更新用户登录状态  登入 登出
    const val RX_BUS_UPDATE_LOGIN_STATUS = "rx_bus_update_login_status"

    //暂停音乐
    const val RX_BUS_STOP_AUDIO = "RX_BUS_STOP_AUDIO"
}