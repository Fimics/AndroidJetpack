package com.hnradio.common.model

import androidx.lifecycle.MutableLiveData
import com.hnradio.common.base.BaseViewModel
import com.hnradio.common.http.IronFansLifeApiUtil
import com.hnradio.common.http.ProgramApiUtil
import com.hnradio.common.http.bean.AlbumContentBean
import com.hnradio.common.http.bean.CommentListResBean
import com.hnradio.common.http.bean.PraiseBean
import com.hnradio.common.manager.UserManager
import com.yingding.lib_net.bean.base.BaseResBean
import com.yingding.lib_net.http.RetroFitResultFailListener
import com.yingding.lib_net.http.RetrofitRequest
import com.yingding.lib_net.http.RetrofitResultListener
import io.reactivex.disposables.Disposable

/**
 *  节目相关  音频  视频  平台图文  详情
 * created by qiaoyan on 2021/8/10
 */
class ProgramModel : BaseViewModel() {

    val albumDetailData: MutableLiveData<AlbumContentBean> = MutableLiveData()

    val changeLikeData: MutableLiveData<PraiseBean> = MutableLiveData()

    val postCommentData: MutableLiveData<String> = MutableLiveData()

    val commentListData: MutableLiveData<CommentListResBean> = MutableLiveData()

    val subCommentListData: MutableLiveData<CommentListResBean> = MutableLiveData()

    val playListData: MutableLiveData<ArrayList<AlbumContentBean>> = MutableLiveData()

    val delResp: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 获取节目详情
     */
    fun getAlbumDetail(albumDetailId: Int) {
        ProgramApiUtil.getAlbumProgramDetail(
            albumDetailId, {
                albumDetailData.postValue(it.data)
            }, {

            })?.add()
    }

    fun delLifeComment(id : Int){
        ProgramApiUtil.delCommentLife(
            id, {
                delResp.postValue(true)
            }, {

            })?.add()
    }

    fun delAlbumComment(id : Int){
        ProgramApiUtil.delCommentAlbum(
            id, {
                delResp.postValue(true)
            }, {

            })?.add()
    }

    /**
     * 增加浏览数
     */
    fun addBrowseNum(albumDetailId: Int) {
        ProgramApiUtil.addBrowseNum(
            albumDetailId, {

            }, {

            })?.add()
    }

    /**
     * 增加转发数
     */
    fun addForwardNum(albumDetailId: Int) {
        ProgramApiUtil.addForwardNum(
            albumDetailId, {
//                ToastUtils.show("增加转发数")
            }, {

            })?.add()
    }

    /**
     * 点赞或取消点赞
     */
    fun changeLike(json: String) {
        if(!UserManager.checkIsGotoLogin()) {
            ProgramApiUtil.changeLike(
                json, {
                    changeLikeData.postValue(it.data)
                }, {

                })?.add()
        }
    }

    /**
     * 发布评论
     */
    fun postComment(json: String) {
        if(!UserManager.checkIsGotoLogin()) {
            ProgramApiUtil.postComment(
                json, {
                    postCommentData.postValue(it.msg)
                }, {

                })?.add()
        }
    }

    /**
     * 分页查询评论列表
     */
    fun getCommentList(json: String) {
        ProgramApiUtil.getCommentList(
            json, {
                commentListData.postValue(it.data)
            }, {

            })?.add()
    }

    val commentRefreshListData: MutableLiveData<CommentListResBean> = MutableLiveData()

    //需特殊处理的，这块现在这么写有问题
    fun getCommentRefreshList(json: String) {
        ProgramApiUtil.getCommentList(
            json, {
                commentRefreshListData.postValue(it.data)
            }, {

            })?.add()
    }

    /**
     * 分页查询二级评论列表
     */
    fun getSubCommentList(json: String) {
        ProgramApiUtil.getCommentList(
            json, {
                subCommentListData.postValue(it.data)
            }, {

            })?.add()
    }

    /**
     * 获取节目列表
     * mediaType  0:音频  1:视频  2.图文  3:H5
     */
    fun getPlayList(albumId: Int, mediaType: Int) {
        ProgramApiUtil.getPlayList(
            albumId, mediaType, {
                playListData.postValue(it.data)
            }, {

            })?.add()
    }
}