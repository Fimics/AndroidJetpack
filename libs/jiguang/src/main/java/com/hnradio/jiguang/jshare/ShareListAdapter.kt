package com.hnradio.jiguang.jshare

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hnradio.jiguang.R

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-14 16:35
 * @Version: 1.0
 */
class ShareListAdapter(data: MutableList<ShareBean>?) :
    BaseQuickAdapter<ShareBean, BaseViewHolder>(R.layout.item_share_new, data) {
    override fun convert(holder: BaseViewHolder, item: ShareBean) {
        holder.setText(R.id.tv_share_text, item.text)
        holder.setImageResource(R.id.iv_share_icon, item.drawableResId)
    }
}