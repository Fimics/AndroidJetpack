package com.hnradio.common.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hnradio.common.R
import com.hnradio.common.http.bean.CommentBean
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.TimeUtils
import com.hnradio.common.widget.roundedimageview.TitleAvatarView

/**
 *  评论适配器
 * created by qiaoyan on 2021/8/12
 */
class CommentItemChildAdapter(data: MutableList<CommentBean>?) :
    BaseQuickAdapter<CommentBean, BaseViewHolder>(R.layout.item_comment_child, data) {

    private var userId = -1
    init {
        userId = UserManager.getLoginUser()?.id?:-1
    }

    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        val avatarView = holder.getView<TitleAvatarView>(R.id.riv_portrait)
        avatarView.setImages(item.headImageUrl, item.levelImageUrl)
        holder.setText(R.id.tv_name, item.nickName)
        holder.setText(R.id.tv_date, TimeUtils.millis2String(item.createTime))
        holder.setText(R.id.tv_comment, item.text)
        if(item.userId == userId){
            holder.setVisible(R.id.tv_del_comment, true)
        }else{
            holder.setGone(R.id.tv_del_comment, true)
        }
    }
}