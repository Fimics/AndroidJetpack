package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.HttpObject
import dagger.Module

@Module
class HttpModule {

    fun providerHttpObject():HttpObject{
        return HttpObject()
    }
}