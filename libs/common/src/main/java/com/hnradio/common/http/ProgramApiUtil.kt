package com.hnradio.common.http

import com.hnradio.common.http.bean.AlbumContentBean
import com.hnradio.common.http.bean.CommentListResBean
import com.hnradio.common.http.bean.PraiseBean
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetroFitResultFailListener
import com.yingding.lib_net.http.RetrofitRequest
import com.yingding.lib_net.http.RetrofitResultListener
import com.yingding.lib_net.http.RetrofitUtil
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody

/**
 *  音频播放
 * created by qiaoyan on 2021/8/12
 */
object ProgramApiUtil {

    private var service =
        RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, CommonService::class.java)

    /**
     * 获取节目详情
     */
    fun getAlbumProgramDetail(
        albumDetailId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<AlbumContentBean>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.getAlbumDetail(albumDetailId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 删除铁粉生活评论
     */
    fun delCommentLife(
        id: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.delLifeComment(id),
            onSuccess,
            onFailure
        )
    }

    /**
     * 删除专辑评论
     */
    fun delCommentAlbum(
        id: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.delAlbumComment(id),
            onSuccess,
            onFailure
        )
    }

    /**
     * 增加浏览数
     */
    fun addBrowseNum(
        albumDetailId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.addBrowseNum(albumDetailId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 增加转发数
     */
    fun addForwardNum(
        albumDetailId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.addForwardNum(albumDetailId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 点赞或取消点赞
     */
    fun changeLike(
        jsonObject: String,
        onSuccess: RetrofitResultListener<BaseResBean<PraiseBean>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        val body: RequestBody =
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject
            )
        return RetrofitRequest.request(
            service.changeLike(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 发布评论
     */
    fun postComment(
        jsonObject: String,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        val body: RequestBody =
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject
            )
        return RetrofitRequest.request(
            service.postComment(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 分页查询评论列表
     */
    fun getCommentList(
        jsonObject: String,
        onSuccess: RetrofitResultListener<BaseResBean<CommentListResBean>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        val body: RequestBody =
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject
            )
        return RetrofitRequest.request(
            service.getCommentList(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 获取播放列表
     */
    fun getPlayList(
        albumId: Int,
        mediaType: Int,
        onSuccess: RetrofitResultListener<BaseResBean<ArrayList<AlbumContentBean>>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.getPlayList(albumId,mediaType),
            onSuccess,
            onFailure
        )
    }
}