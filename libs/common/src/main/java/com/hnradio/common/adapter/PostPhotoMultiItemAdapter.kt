package com.hnradio.common.adapter


import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hnradio.common.R
import com.hnradio.common.bean.PostPhotoMultiItem
import com.hnradio.common.util.L


/**
 * 铁粉生活发布 适配器
 * created by 乔岩 on 2021/7/27
 */
class PostPhotoMultiItemAdapter(data: MutableList<PostPhotoMultiItem>) :
    BaseMultiItemQuickAdapter<PostPhotoMultiItem, BaseViewHolder>(data) {

    init {
        addItemType(PostPhotoMultiItem.ITEM_TYPE_PHOTO, R.layout.item_photo)
        addItemType(PostPhotoMultiItem.ITEM_TYPE_ADD, R.layout.item_add_photo)
    }

    override fun convert(holder: BaseViewHolder, item: PostPhotoMultiItem) {
        when (holder.itemViewType) {
            PostPhotoMultiItem.ITEM_TYPE_PHOTO -> {
                Glide.with(context).load(item.data).into(holder.getView(R.id.iv_photo))
                var ivDelete = holder.getView<ImageView>(R.id.deleteImg)
                ivDelete.setOnClickListener {
                    mOnCloseListener?.onCloseClick(holder.position)
                }
            }
            PostPhotoMultiItem.ITEM_TYPE_ADD -> {
                holder.setText(R.id.tv_text, item.data)
                holder.itemView.setOnClickListener {
                    onPhotoItemClickListener?.onAddClick()
                }
            }
        }
    }

    private var onPhotoItemClickListener: OnPhotoItemClickListener? = null

    fun setOnPhotoItemClickListener(onPhotoItemClickListener: OnPhotoItemClickListener) {
        this.onPhotoItemClickListener = onPhotoItemClickListener
    }

    interface OnPhotoItemClickListener {

        fun onAddClick()
    }


    private var mOnCloseListener: OnCloseListener? = null
    fun setOnCloseListener(onCloseListener: OnCloseListener) {
        this.mOnCloseListener = onCloseListener
    }

    interface OnCloseListener {
        fun onCloseClick(position:Int)
    }

}