package com.hnradio.common.model

import androidx.lifecycle.MutableLiveData
import com.hnradio.common.base.BaseViewModel
import com.hnradio.common.constant.CommonBusEvent
import com.hnradio.common.http.IronFansLifeApiUtil
import com.hnradio.common.http.ProgramApiUtil
import com.hnradio.common.http.bean.*
import com.hnradio.common.manager.UserManager
import com.hwangjr.rxbus.RxBus

/**
 *  铁粉生活发布内容   用户图文  小视频
 * created by qiaoyan on 2021/8/10
 */
open class IronFansLifeModel : BaseViewModel() {

    val lifeDetailData: MutableLiveData<IronFansLifeBean> = MutableLiveData()

    val changeLikeData: MutableLiveData<PraiseBean> = MutableLiveData()

    val changeFollowData: MutableLiveData<FollowBean> = MutableLiveData()

    val postCommentData: MutableLiveData<String> = MutableLiveData()

    val commentListData: MutableLiveData<CommentListResBean> = MutableLiveData()

    val subCommentListData: MutableLiveData<CommentListResBean> = MutableLiveData()


    /**
     * 获取节目详情
     */
    fun getLiftDetail(lifeId: Int) {
        IronFansLifeApiUtil.getLiftDetail(
            lifeId, {
                lifeDetailData.postValue(it.data)
            }, {

            })?.add()
    }


    /**
     * 增加浏览数
     */
    fun addBrowseNumLife(lifeId: Int) {
        IronFansLifeApiUtil.addBrowseNumLife(
            lifeId, {

            }, {

            })?.add()
    }

    /**
     * 增加转发数
     */
    fun addForwardNumLife(lifeId: Int) {
        IronFansLifeApiUtil.addForwardNumLife(
            lifeId, {
//                ToastUtils.show("增加转发数")
            }, {

            })?.add()
    }

    /**
     * 点赞或取消点赞
     */
    fun changeLikeLife(json: String) {
        if(!UserManager.checkIsGotoLogin()) {
            IronFansLifeApiUtil.changeLikeLife(
                json, {
                    changeLikeData.postValue(it.data)
                    RxBus.get().post(CommonBusEvent.RX_BUS_ANCHOR_INFO_CHANGED, "")
                }, {

                })?.add()
        }
    }

    /**
     * 关注或取消关注
     */
    fun changeFollowLife(json: String) {
        if(!UserManager.checkIsGotoLogin()) {
            IronFansLifeApiUtil.changeFollowLife(
                json, {
                    changeFollowData.postValue(it.data)
                    RxBus.get().post(CommonBusEvent.RX_BUS_ANCHOR_INFO_CHANGED, "")
                }, {

                })?.add()
        }
    }

    val delResp: MutableLiveData<Boolean> = MutableLiveData()
    /**删除评论*/
    fun delComment(id : Int){
        ProgramApiUtil.delCommentLife(
            id, {
                delResp.postValue(true)
            }, {

            })?.add()
    }

    /**
     * 发布评论
     */
    fun postCommentLife(json: String) {
        if(!UserManager.checkIsGotoLogin()) {
            IronFansLifeApiUtil.postCommentLife(
                json, {
                    postCommentData.postValue(it.msg)
                }, {

                })?.add()
        }
    }

    /**
     * 分页查询评论列表
     */
    fun getCommentListLife(json: String) {
        IronFansLifeApiUtil.getCommentListLife(
            json, {
                commentListData.postValue(it.data)
            }, {

            })?.add()
    }

    /**
     * 分页查询二级评论列表
     */
    fun getSubCommentListLife(json: String) {
        IronFansLifeApiUtil.getCommentListLife(
            json, {
                subCommentListData.postValue(it.data)
            }, {

            })?.add()
    }

}