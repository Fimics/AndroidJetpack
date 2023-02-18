package com.hnradio.common.util

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: AssentUtil
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/8/28 4:04 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/8/28 4:04 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
object AssetsUtil {

    //获取assets 文件字符串
    fun getFileString(context: Context, fileName: String, callBack: Consumer<String>): Disposable {
        return Observable.just(fileName)
            .map {
                val stringBuilder = StringBuilder()
                val assetManager = context.assets
                val bf = BufferedReader(
                    InputStreamReader(
                        assetManager.open(it)
                    )
                )
                var line: String?
                while (bf.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                stringBuilder.toString()
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(callBack, {
                it.printStackTrace()
            })
    }

}