package com.hnradio.common.util

import com.hnradio.common.http.CommonApiUtil
import com.hnradio.common.http.bean.AnchorContentBean
import com.hnradio.common.http.bean.TagContentBean

class OSSImageInfoUtil<T>(private val onGetImageInfoCallback: OnGetImageInfoCallback<T>) {

    private var position = 0

    //首页瀑布流 缩略图的宽 根据屏幕算出来的
    private val tagContentWidth = (ScreenUtils.getScreenWidth(Global.application) - 44.idDp) / 2

    /**
     * 获取阿里OSS服务的图片宽高
     */
    fun getOSSImageInfo(list: ArrayList<T>){
//        Logger.d("获取图片宽高  开始请求")
        if (list.size == 0){
            onGetImageInfoCallback.onGetImageInfoSuccess(list)
            return
        }
        val bean = list[position]
        when(bean){
            is TagContentBean -> {
                CommonApiUtil.getOSSImageInfo(bean.imageUrl,{
                    //设置缩略图的宽高
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90"
                    bean.imageWidth = tagContentWidth
                    bean.imageHeight =
                        (tagContentWidth * (it.ImageHeight.value.toDouble() / it.ImageWidth.value.toDouble())).toInt()
                    if (++position < list.size){
                        getOSSImageInfo(list)
                    }else{
                        onGetImageInfoCallback.onGetImageInfoSuccess(list)
                    }
                },{
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90}"
                    bean.imageWidth = 0
                    bean.imageHeight = 0
                    if (++position < list.size){
                        getOSSImageInfo(list)
                    }else{
                        onGetImageInfoCallback.onGetImageInfoSuccess(list)
                    }
                })
            }
            is AnchorContentBean -> {
                CommonApiUtil.getOSSImageInfo(bean.imageUrl,{
                    //设置缩略图的宽高
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90"
                    bean.imageWidth = tagContentWidth
                    bean.imageHeight =
                        (tagContentWidth * (it.ImageHeight.value.toDouble() / it.ImageWidth.value.toDouble())).toInt()
                    if (++position < list.size){
                        getOSSImageInfo(list)
                    }else{
                        onGetImageInfoCallback.onGetImageInfoSuccess(list)
                    }
                },{
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90}"
                    bean.imageWidth = 0
                    bean.imageHeight = 0
                    if (++position < list.size){
                        getOSSImageInfo(list)
                    }else{
                        onGetImageInfoCallback.onGetImageInfoSuccess(list)
                    }
                })
            }
        }
    }

    interface OnGetImageInfoCallback<T> {

        fun onGetImageInfoSuccess(urlList: ArrayList<T>)

    }
}