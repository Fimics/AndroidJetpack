package com.mic.jetpack.databinding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.mic.BR


class User(name: String, pwd: String) : BaseObservable() {

    @get:Bindable
    var name: String = name
        set(value) {
            field = value
            notifyChange()  // 通知所有属性变化
        }

    @get:Bindable
    var pwd: String = pwd
        set(value) {
            field = value
            notifyChange()  // 通知所有属性变化
        }
}