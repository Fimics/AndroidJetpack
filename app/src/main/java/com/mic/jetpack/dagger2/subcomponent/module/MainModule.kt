package com.mic.jetpack.dagger2.subcomponent.module

import com.mic.jetpack.dagger2.subcomponent.`object`.MainObject
import com.mic.jetpack.dagger2.subcomponent.`object`.XOkhttp
import com.mic.jetpack.dagger2.subcomponent.`object`.XRetrofit
import com.mic.jetpack.dagger2.subcomponent.`object`.XUser
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class MainModule constructor() {

    @Provides
    fun getMainObject(): MainObject {
        return MainObject()
    }

    var name: String? = null
    var pwd: String? = null

    constructor(name: String, pwd: String) : this() {
        this.name = name
        this.pwd = pwd
    }

    @Named("key1")
    @Provides
    fun getUser1():XUser{
        return XUser(this.name!!,this.pwd!!)
    }

    @Named("key2")
    @Provides
    fun getUser2():XUser{
        return XUser(this.name!!,this.pwd!!)
    }

    @Provides
    fun getXOkhttp():XOkhttp{
        return XOkhttp()
    }

    @Provides
    fun getXRetrofit(xOkhttp: XOkhttp):XRetrofit{
        return XRetrofit(xOkhttp)
    }
}