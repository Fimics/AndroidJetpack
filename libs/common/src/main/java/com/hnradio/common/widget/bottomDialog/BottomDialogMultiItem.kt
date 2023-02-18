package com.hnradio.common.widget.bottomDialog

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.hnradio.common.http.bean.AlbumContentBean
import com.hnradio.common.http.bean.ExpertBean

/**
 * 底部弹窗 MultiItem
 * created by 乔岩 on 2021/8/11
 */
class BottomDialogMultiItem<T>(val data: T) : MultiItemEntity {

    companion object {
        const val ITEM_TYPE_TEXT = 0 //单行字符串
        const val ITEM_TYPE_IMAGE_TEXT = 1 //图片文字
        const val ITEM_TYPE_TEXT_RADIO = 2 //文字 单选框
        const val ITEM_TYPE_AUDIO = 3 //音频信息
        const val ITEM_TYPE_EXPERT = 4 //专家信息
        const val ITEM_TYPE_REWARD = 5 //打赏
    }

    override val itemType: Int
        get() = when(data){
            is String -> ITEM_TYPE_TEXT
            is BottomImageTextBean -> ITEM_TYPE_IMAGE_TEXT
            is BottomTextRadioBean -> ITEM_TYPE_TEXT_RADIO
            is AlbumContentBean -> ITEM_TYPE_AUDIO
            is ExpertBean -> ITEM_TYPE_EXPERT
            is Int -> ITEM_TYPE_REWARD
            else -> -1
        }
}