package com.hnradio.common.http

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.http
 * @ClassName: CommonUrl
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/7/20 6:06 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/20 6:06 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
//获取ali oss 配置
const val url_ali_oss_config : String="wfadmin/oss/getConfig"
//获取MQTT config
const val urlMqttConfig : String="wfadmin/mqtt/getConfig"
//发送MQTT消息
const val urlMqttSendMessage : String="wfadmin/mqtt/sendMsg"

//获取节目详情
const val url_get_album_detail : String="wfadmin/albumDetail/open/getAlbumDetail"

//增加浏览数
const val url_add_browse_num : String="wfadmin/albumDetail/open/addPvNum"

//增加转发数
const val url_add_forward_num : String="wfadmin/albumDetail/open/addForwardNum"

//点赞或取消点赞
const val url_change_like : String="wfadmin/albumDetail/changePraises"

//发布评论
const val url_post_comment : String="wfadmin/albumDetail/comment/postComments"

//分页查询评论列表
const val url_get_comment_list : String="wfadmin/albumDetail/comment/open/getCommentList"

//获取播放列表
const val url_get_play_list : String="wfadmin/albumDetail/open/getPlayList"

//获取铁粉生活详情
const val url_get_life_detail : String="wfadmin/appTflifenInfo/open/getLifeDetail"

//增加转发数 铁粉生活
const val url_add_forward_num_life : String="wfadmin/appTflifenInfo/open/addForwardNum"

//增加浏览数 铁粉生活
const val url_add_count_num_life : String="wfadmin/appTflifenInfo/open/addCountNum"

//点赞 取消点赞 铁粉生活
const val url_change_like_life : String="wfadmin/appTflifenInfo/changePraises"

//关注 取消关注 铁粉生活
const val url_change_follow_life : String="wfadmin/appTflifenInfo/changeFans"

//获取评论列表 铁粉生活
const val url_get_comment_list_life : String="wfadmin/comment/open/getLifeComment"

//发布评论 铁粉生活
const val url_post_comment_post : String="wfadmin/comment/postComments"

//检查版本
const val url_upgrade_check_version : String="wfadmin/appUpdate/open/getLastOne"

//支付
const val url_base_pay: String = "wfadmin/pay"

//获取核销信息
const val url_getWriteOffInfo: String = "wfadmin/lottery/records/getRecordBySn"
//核销
const val url_WriteOff: String = "wfadmin/lottery/records/passPrizeRecord"
/**删除铁粉生活评论*/
const val url_del_life_comment = "wfadmin/comment/deleteLifeComment"
/**删除专辑评论*/
const val url_del_album_comment = "wfadmin/albumDetail/comment/deleteAlbumComment"



