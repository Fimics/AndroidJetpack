package com.hnradio.common.util.upgrade

import android.app.Activity
import com.hnradio.common.http.CommonApiUtil

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-23 10:30
 * @Version: 1.0
 */
object UpdateManager {

    fun checkNewVersion(activity: Activity, callBack: CheckCallBack) {
        CommonApiUtil.checkAppVersion(
            {
                it?.data?.let { it1 ->
                    callBack.onCheckResult(it1)
                }
            }, {

            }
        )
    }

}