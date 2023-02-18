package com.hnradio.common.widget.bottomDialog

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gyf.immersionbar.ktx.isSupportNavigationIconDark
import com.hnradio.common.R
import com.hnradio.common.http.bean.AlbumContentBean
import com.hnradio.common.http.bean.ExpertBean
import com.hnradio.common.util.FormatUtil

/**
 *
 * created by qiaoyan on 2021/8/11
 */
class BottomDialogMultiItemAdapter<T> :
    BaseMultiItemQuickAdapter<BottomDialogMultiItem<T>, BaseViewHolder> {

    private var onAdapterItemClickListener: OnAdapterItemClickListener<T>? = null
    private var selectText: String? = null
    private var selectId: Int? = -1

    constructor(data: ArrayList<BottomDialogMultiItem<T>>) : super(data)

    constructor(data: ArrayList<BottomDialogMultiItem<T>>, selectText: String) : super(data) {
        this.selectText = selectText
    }

    constructor(data: ArrayList<BottomDialogMultiItem<T>>, selectId: Int) : super(data) {
        this.selectId = selectId
    }

    init {
        addItemType(BottomDialogMultiItem.ITEM_TYPE_TEXT, R.layout.item_bottom_text)
        addItemType(BottomDialogMultiItem.ITEM_TYPE_IMAGE_TEXT, R.layout.item_bottom_image_text)
        addItemType(BottomDialogMultiItem.ITEM_TYPE_TEXT_RADIO, R.layout.item_bottom_text_radio)
        addItemType(BottomDialogMultiItem.ITEM_TYPE_AUDIO, R.layout.item_bottom_audio)
        addItemType(BottomDialogMultiItem.ITEM_TYPE_EXPERT, R.layout.item_bottom_expert)
        addItemType(BottomDialogMultiItem.ITEM_TYPE_REWARD, R.layout.item_bottom_reward)
    }

    override fun convert(holder: BaseViewHolder, item: BottomDialogMultiItem<T>) {
        when (item.data) {
            is String -> {
                holder.setText(R.id.tv_text, item.data)
            }
            is BottomImageTextBean -> {
                Glide.with(context).load(item.data.imageResource).into(holder.getView(R.id.ivImage))
                holder.setText(R.id.tv_text, item.data.text)
            }
            is BottomTextRadioBean -> {//文本 radio(倍速)
                holder.setText(R.id.tv_text, item.data.text)
                holder.setImageResource(
                    R.id.iv_radio,
                    if (item.data.text == selectText) R.drawable.icon_circle_orange else R.drawable.icon_circle_white
                )
            }
            is AlbumContentBean -> {//音频
                holder.setText(R.id.tv_order_num, item.data.orderNum.toString())
                holder.setText(R.id.tv_name, item.data.name)
                holder.setText(
                    R.id.tv_time,
                    FormatUtil.formatProgramDuration(item.data.mediaLength)
                )
                holder.setText(R.id.tv_play_count, FormatUtil.formatPlayAmount(item.data.pv))
            }
            is ExpertBean -> {//专家
                Glide.with(context).load(item.data.imageUrl).into(holder.getView(R.id.riv_portrait))
                holder.setText(R.id.tv_name, item.data.name)
                holder.setText(R.id.tv_tag_name, item.data.tagName)
                holder.setText(R.id.tv_describe, item.data.describ)
                holder.setImageResource(
                    R.id.iv_radio,
                    if (item.data.id == selectId) R.drawable.icon_circle_orange else R.drawable.icon_circle_white
                )
            }
            is Int -> {//打赏
                holder.setText(R.id.tv_num, item.data.toString())
                holder.setTextColor(
                    R.id.tv_num,
                    if (selectId == item.data)
                        context.resources.getColor(R.color.white)
                    else
                        context.resources.getColor(R.color.hui3)
                )
                holder.setTextColor(
                    R.id.tv_hint,
                    if (selectId == item.data)
                        context.resources.getColor(R.color.white)
                    else
                        context.resources.getColor(R.color.hui3)
                )
                holder.setBackgroundResource(
                    R.id.cl_reward,
                    if (selectId == item.data)
                        R.drawable.shape_solid_orange_radius_10
                    else
                        R.drawable.shape_solid_grayee_radius_10
                )
            }
        }
        holder.itemView.setOnClickListener {
            when (item.data) {
                is BottomTextRadioBean -> {
                    selectText = item.data.text
                    notifyDataSetChanged()
                }
                is Int -> {
                    selectId = item.data
                    notifyDataSetChanged()
                }
            }
            onAdapterItemClickListener?.onItemClick(item.data)
        }
    }

    fun setOnAdapterItemClickListener(onAdapterItemClickListener: OnAdapterItemClickListener<T>) {
        this.onAdapterItemClickListener = onAdapterItemClickListener
    }

    interface OnAdapterItemClickListener<T> {
        fun onItemClick(data: T)
    }

}