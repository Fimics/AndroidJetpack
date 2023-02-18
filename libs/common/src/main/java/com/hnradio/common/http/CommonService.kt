package com.hnradio.common.http

import com.hnradio.common.http.bean.*
import com.hnradio.common.util.pay.PayReq
import com.yingding.lib_net.bean.base.BaseResBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.http
 * @ClassName: CommonServer
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/7/20 6:09 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/20 6:09 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
interface CommonService {

    @GET(url_ali_oss_config)
    fun getAliOSSConfig(): Observable<BaseResBean<AliOSSConfigBean>>

    @GET
    fun getMqttConfig(@Url url: String): Observable<BaseResBean<MqttConfigBean>>

    @POST
    fun mqttSendMessage(@Url url: String, @Body body: RequestBody): Observable<BaseResBean<String>>

    @GET(url_get_album_detail)
    fun getAlbumDetail(@Query("albumDetailId") albumDetailId: Int): Observable<BaseResBean<AlbumContentBean>>

    @FormUrlEncoded
    @POST(url_add_browse_num)
    fun addBrowseNum(@Field("albumDetailId") albumDetailId: Int): Observable<BaseResBean<String>>

    @FormUrlEncoded
    @POST(url_add_forward_num)
    fun addForwardNum(@Field("albumDetailId") albumDetailId: Int): Observable<BaseResBean<String>>

    @POST(url_change_like)
    fun changeLike(@Body body: RequestBody): Observable<BaseResBean<PraiseBean>>

    @POST(url_post_comment)
    fun postComment(@Body body: RequestBody): Observable<BaseResBean<String>>

    @POST(url_get_comment_list)
    fun getCommentList(@Body body: RequestBody): Observable<BaseResBean<CommentListResBean>>

    @GET(url_get_play_list)
    fun getPlayList(
        @Query("albumId") albumId: Int,
        @Query("mediaType") mediaType: Int
    ): Observable<BaseResBean<ArrayList<AlbumContentBean>>>

    @GET(url_get_life_detail)
    fun getLiftDetail(@Query("lifeId") lifeId: Int): Observable<BaseResBean<IronFansLifeBean>>

    @POST(url_get_comment_list_life)
    fun getCommentListLife(@Body body: RequestBody): Observable<BaseResBean<CommentListResBean>>

    @POST(url_post_comment_post)
    fun postCommentLife(@Body body: RequestBody): Observable<BaseResBean<String>>

    @POST(url_change_like_life)
    fun changeLikeLife(@Body body: RequestBody): Observable<BaseResBean<PraiseBean>>

    @POST(url_change_follow_life)
    fun changeFollowLife(@Body body: RequestBody): Observable<BaseResBean<FollowBean>>

    @FormUrlEncoded
    @POST(url_add_count_num_life)
    fun addBrowseNumLife(@Field("lifeId") lifeId: Int): Observable<BaseResBean<String>>

    @FormUrlEncoded
    @POST(url_add_forward_num_life)
    fun addForwardNumLife(@Field("lifeId") lifeId: Int): Observable<BaseResBean<String>>

    @GET
    fun getOSSImageInfo(@Url url: String): Observable<ImageInfoBean>

    @GET(url_upgrade_check_version)
    fun checkVersion(@Query("platform") albumDetailId: Int): Observable<BaseResBean<AppVersionBean>>

    /**
     * 订单支付
     */
    @POST(url_base_pay)
    fun orderPay(
        @Body body: ReqBasePayBean
    ): Observable<BaseResBean<PayReq>>

    /**删除铁粉生活评论*/
    @FormUrlEncoded
    @POST(url_del_life_comment)
    fun delLifeComment(@Field("commentId") commentId: Int): Observable<BaseResBean<String>>

    /**删除专辑评论*/
    @FormUrlEncoded
    @POST(url_del_album_comment)
    fun delAlbumComment(@Field("commentId") commentId: Int): Observable<BaseResBean<String>>
}