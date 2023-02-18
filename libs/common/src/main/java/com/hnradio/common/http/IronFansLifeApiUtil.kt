package com.hnradio.common.http

import com.hnradio.common.http.bean.CommentListResBean
import com.hnradio.common.http.bean.FollowBean
import com.hnradio.common.http.bean.IronFansLifeBean
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
 *  铁粉生活 相关
 * created by qiaoyan on 2021/8/12
 */
object IronFansLifeApiUtil {

    private var service =
        RetrofitUtil.instance.getInterface(BuildConfig.ApiUrl, CommonService::class.java)

    /**
     * 获取节目详情
     */
    fun getLiftDetail(
        lifeId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<IronFansLifeBean>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.getLiftDetail(lifeId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 增加浏览数
     */
    fun addBrowseNumLife(
        lifeId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.addBrowseNumLife(lifeId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 增加转发数
     */
    fun addForwardNumLife(
        lifeId: Int,
        onSuccess: RetrofitResultListener<BaseResBean<String>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        return RetrofitRequest.request(
            service.addForwardNumLife(lifeId),
            onSuccess,
            onFailure
        )
    }

    /**
     * 点赞或取消点赞
     */
    fun changeLikeLife(
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
            service.changeLikeLife(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 点赞或取消点赞
     */
    fun changeFollowLife(
        jsonObject: String,
        onSuccess: RetrofitResultListener<BaseResBean<FollowBean>>,
        onFailure: RetroFitResultFailListener
    ): Disposable? {
        val body: RequestBody =
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonObject
            )
        return RetrofitRequest.request(
            service.changeFollowLife(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 发布评论
     */
    fun postCommentLife(
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
            service.postCommentLife(body),
            onSuccess,
            onFailure
        )
    }

    /**
     * 删除评论
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
     * 分页查询评论列表
     */
    fun getCommentListLife(
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
            service.getCommentListLife(body),
            onSuccess,
            onFailure
        )
    }


}