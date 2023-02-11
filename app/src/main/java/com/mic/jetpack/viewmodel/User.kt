package com.mic.jetpack.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class User(@Bindable var name:String, @Bindable var pwd:String): BaseObservable(){

//    fun setName(name: String){
//        this.name=name
//        notifyPropertyChanged(BR.name)
//    }
//
//    fun setPwd(pwd: String){
//        this.pwd=pwd
//        notifyPropertyChanged(BR.pwd)
//    }
}