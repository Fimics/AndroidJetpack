package com.hnradio.common.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 发布图片 MultiItem
 * created by 乔岩 on 2021/7/27
 */
class PostPhotoMultiItem(val data: String) : MultiItemEntity {

    companion object {
        const val ITEM_TYPE_PHOTO = 0 //照片
        const val ITEM_TYPE_ADD = 1  //添加
    }

    fun isAddItem() : Boolean{
        return itemType == ITEM_TYPE_ADD
    }

    override val itemType: Int
        get() = if(data.contains("/")) ITEM_TYPE_PHOTO else ITEM_TYPE_ADD
}