package com.mic.jetpack.databinding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
//import com.mic.BR

class User(@Bindable var name:String,@Bindable var pwd:String): BaseObservable(){

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