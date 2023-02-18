package com.hnradio.jiguang.jshare

/**
 *
 * @Description: 分享渠道
 * @Author: huqiang
 * @CreateDate: 2021-07-14 16:29
 * @Version: 1.0
 */
data class ShareBean(var id:Int,var text:String,var drawableResId:Int,var alias:String){
    override fun toString(): String {
        return "ShareBean(id=$id, text='$text', drawableResId=$drawableResId, alias=$alias)"
    }
}
