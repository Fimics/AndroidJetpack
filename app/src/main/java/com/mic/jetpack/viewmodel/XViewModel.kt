package com.mic.jetpack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class XViewModel:ViewModel(){

    private  var number:MutableLiveData<String>?=null

    private  var user:MutableLiveData<User>?=null

    fun getNumber():MutableLiveData<String>{
        if (null==number){
            number= MutableLiveData("hello")
        }
        return number!!
    }

    fun getUser():MutableLiveData<User>{
        if (user==null){
            user=MutableLiveData()
        }
        return user!!
    }

    fun getUserFromNet(){
        //获取数据
    }
}