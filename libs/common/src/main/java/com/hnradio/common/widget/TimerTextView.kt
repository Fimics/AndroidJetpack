package com.hnradio.common.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatTextView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.widget
 * @ClassName: TimerTextView
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/9/2 11:49 上午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/9/2 11:49 上午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class TimerTextView(@NonNull context: Context, @Nullable attrs: AttributeSet) :
    AppCompatTextView(context, attrs) {


    var disposable: Disposable? = null

    //开始计时
    fun startTimer() {
        endTimer()
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                timerCallBack?.let { it1 -> it1() }
            }
    }

    //计时回调
    var timerCallBack: (() -> Unit)? = null

    fun setCallBack(call:(() -> Unit)?){
        timerCallBack=call
    }

    //结束计时
    fun endTimer() {
        disposable?.dispose()
    }

    //从屏幕移除
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timerCallBack = null
        endTimer()
    }
}