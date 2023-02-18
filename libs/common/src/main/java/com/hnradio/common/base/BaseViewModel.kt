package com.hnradio.common.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.Disposable

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.base
 * @ClassName: BaseViewModel
 * @Description: 基础viewmodel
 * @Author: shaoguotong
 * @CreateDate: 2021/6/21 10:14 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/6/21 10:14 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
open class BaseViewModel :ViewModel(){
    //自定义是为了防止某些页面会频繁请求无法释放清理
    private val mDisList = ArrayList<Disposable>() //网络Disposable集合

    /**
     * cleared生命周期
     */
    override fun onCleared() {
        super.onCleared()
        clearDisposed();
        mDisList.clear()
    }

    /**
     * 校验删除已经执行完的请求
     */
    private fun checkDisposed() {
        var y: Int? = 0
        for (i in mDisList.indices) {
            val disposable = mDisList[i - y!!]
            if (disposable.isDisposed) {
                mDisList.removeAt(i - y)
                y++
            }
        }
    }

    /**
     * 清除所有请求, 正在执行的会中断
     */
    private fun clearDisposed() {
        for (disposable in mDisList) {
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
    }

    /**
     * 添加到中断管理
     */
    fun Disposable.add() {
        mDisList.add(this)
        checkDisposed()
    }

    fun addDisposed(disposable: Disposable){
        mDisList.add(disposable)
        checkDisposed()
    }
}