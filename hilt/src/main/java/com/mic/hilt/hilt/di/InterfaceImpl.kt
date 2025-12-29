package com.mic.hilt.hilt.di

import com.mic.hilt.KLog
import javax.inject.Inject

class InterfaceImpl @Inject constructor(): IInterface {

    override fun method() {
        KLog.d("hilt","注入成功")
    }
}