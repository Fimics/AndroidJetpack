package com.hnradio.common.util

import com.hnradio.common.http.CommonApiUtil
import com.hnradio.common.http.bean.AnchorContentBean
import com.hnradio.common.http.bean.TagContentBean
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors

class OSSImageInfoGetter<T : OSSImageInfoGetter.IOssImageFeature> {

    //首页瀑布流 缩略图的宽 根据屏幕算出来的
    private val tagContentWidth = (ScreenUtils.getScreenWidth(Global.application) - 44.idDp) / 2

    interface Callback<T> {
        fun onResult(ret: MutableList<T>)
    }

    var mCallback : Callback<T>? = null

    private var currentReqCounts = 0
    fun getImageSize(list : List<T>){
        finalOssInfoList.clear()
        if(list.isNotEmpty()){
            currentReqCounts = list.size
            val obs = mutableListOf<Observable<T>>()
            for (element in list) {
                obs.add(getOssInfoObservable(element))
            }
            mergeDatas(obs)
        }else{
            mCallback?.onResult(finalOssInfoList)
        }
    }

    private val finalOssInfoList = mutableListOf<T>()
    private fun mergeDatas(obs: MutableList<Observable<T>>) {
        Observable.merge(obs)
            .subscribeOn(Schedulers.from(executor))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                finalOssInfoList.add(it)
                if(finalOssInfoList.size == currentReqCounts){
                    mCallback?.onResult(finalOssInfoList)
                }
            }
    }

    //    var tagContentWidth = 500
    private val executor = Executors.newFixedThreadPool(10)

    private fun getOssInfoObservable(bean : T) : Observable<T> {
        return Observable.create(object : ObservableOnSubscribe<T> {
            override fun subscribe(emitter: ObservableEmitter<T>) {
                CommonApiUtil.getOSSImageInfo(bean.imageUrl,{ info ->
                    //设置缩略图的宽高
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90"
                    bean.imageWidth = tagContentWidth
                    bean.imageHeight = (tagContentWidth * (info.ImageHeight.value.toDouble() / info.ImageWidth.value.toDouble())).toInt()
                    emitter.onNext(bean)
                    emitter.onComplete()
                },{
                    bean.imageUrl += "?x-oss-process=image/resize,m_lfit,w_$tagContentWidth/quality,q_90}"
                    bean.imageWidth = 0
                    bean.imageHeight = 0
                    emitter.onNext(bean)
                    emitter.onComplete()
                })
            }
        })
    }

    open class IOssImageFeature {
        var imageUrl: String = ""
        var imageWidth: Int = 0
        var imageHeight: Int = 0
    }
}