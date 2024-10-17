package com.mic.jetpack.hilt.di

import com.mic.KLog
import javax.inject.Inject

class InterfaceImpl @Inject constructor():IInterface {

    override fun method() {
        KLog.d("hilt","注入成功")
    }
}