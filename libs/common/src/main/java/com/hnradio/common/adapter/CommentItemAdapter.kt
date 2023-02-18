package com.hnradio.common.adapter

import android.graphics.Color
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hnradio.common.R
import com.hnradio.common.adapter.decoration.LinearItemDecoration
import com.hnradio.common.adapter.decoration.LinearLayoutDivider
import com.hnradio.common.http.bean.CommentBean
import com.hnradio.common.ktx.UiExtension.C
import com.hnradio.common.ktx.UiExtension.DP
import com.hnradio.common.ktx.UiExtension.HIDE
import com.hnradio.common.ktx.UiExtension.SHOW
import com.hnradio.common.ktx.setSpanColorText
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.FormatUtil
import com.hnradio.common.util.TimeUtils
import com.hnradio.common.util.ToastUtils
import com.hnradio.common.widget.bottomDialog.SubCommentDialog
import com.hnradio.common.widget.roundedimageview.TitleAvatarView

/**
 *  评论适配器
 * created by qiaoyan on 2021/8/12
 */
class CommentItemAdapter(data: MutableList<CommentBean>?) :
    BaseQuickAdapter<CommentBean, BaseViewHolder>(R.layout.item_comment, data) {

    private var onSubCommentClickListener: OnSubCommentClickListener? = null
    var onChildDelListener: OnChildDelCommentListener? = null

    /**是否作为一级评论适配器，此时需要显示出评论数，以便可以点击打开二级窗口*/
    var asRootAdapter = false

    private var userId = -1
    init {
        userId = UserManager.getLoginUser()?.id?:-1
    }

    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        val avatarView = holder.getView<TitleAvatarView>(R.id.riv_portrait)
        avatarView.setImages(item.headImageUrl, item.levelImageUrl)
        holder.setText(R.id.tv_name, item.nickName)
        holder.setText(R.id.tv_date, TimeUtils.millis2String(item.createTime))
        if(item.targetUserNickName.isNullOrEmpty()){
            holder.setText(R.id.tv_comment, item.text)
        }else{
            val str = "回复 @${item.targetUserNickName}：${item.text}"
            val tv = holder.getView<TextView>(R.id.tv_comment)
            tv.setSpanColorText(str, item.targetUserNickName?:"", R.color.huang_F29A3C.C)
        }

        //二级窗口，不再显示多少条回复，平铺展示出来
        if(asRootAdapter){
            holder.setGone(R.id.tv_reply_num, item.replyNum == 0)
            holder.setText(R.id.tv_reply_num, "${item.replyNum}条回复")

            holder.getView<TextView>(R.id.tv_reply_num).setOnClickListener {
                onSubCommentClickListener?.onSubCommentClick(item)
            }
        }
        if(item.userId == userId){
            holder.setVisible(R.id.tv_del_comment, true)
        }else{
            holder.setGone(R.id.tv_del_comment, true)
        }
        /*val rvLevel3 = holder.getView<RecyclerView>(R.id.level3_recycle)
        val child = item.children
        if(!child.isNullOrEmpty()){
            rvLevel3.SHOW()
            rvLevel3.isNestedScrollingEnabled = false
            val childAdapter = CommentItemChildAdapter(child)

            rvLevel3.adapter = CommentItemChildAdapter(child)
            val d = LinearLayoutDivider(context, LinearLayoutDivider.LINEAR_V).also { it.setDividerStyle(10.DP, Color.TRANSPARENT) }
            val dc = rvLevel3.itemDecorationCount
            if(dc > 0){
                val de = rvLevel3.getItemDecorationAt(0)
                rvLevel3.removeItemDecoration(de)
            }
            rvLevel3.addItemDecoration(d)

            childAdapter.addChildClickViewIds(R.id.tv_del_comment)
            childAdapter.setOnItemChildClickListener { _, view, position ->
                if(view.id == R.id.tv_del_comment){
                    onChildDelListener?.onDel(childAdapter.data[position])
                }
            }
        }else{
            rvLevel3.HIDE()
        }*/
    }

    fun setOnSubCommentClickListener(onSubCommentClickListener: OnSubCommentClickListener){
        this.onSubCommentClickListener = onSubCommentClickListener
    }

    /**
     * 当点击二级评论
     */
    interface OnSubCommentClickListener{

         fun onSubCommentClick(data: CommentBean)
    }

    interface OnChildDelCommentListener{
        fun onDel(data : CommentBean)
    }
}