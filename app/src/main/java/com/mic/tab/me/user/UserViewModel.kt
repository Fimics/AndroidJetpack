package com.mic.tab.me.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel:ViewModel() {

    private var userData :MutableLiveData<User>?=null

    fun getUserData():LiveData<User>{
        var user= User("android","15313195276",30,"北京市朝阳区","男","https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fup.enterdesk.com%2Fedpic%2Fa7%2F69%2Fe1%2Fa769e103cb378d2ea4c89ef24617104b.jpg&refer=http%3A%2F%2Fup.enterdesk.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1651160320&t=e5781b174e47ed3c55cd3bd425f0be31",
        "jetpack","developer")
        userData = MutableLiveData(user)
        return userData!!
    }
}