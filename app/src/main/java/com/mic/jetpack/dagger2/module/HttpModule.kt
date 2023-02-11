package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.HttpObject
import dagger.Module
import dagger.Provides

@Module
class HttpModule {

    @Provides
    fun providerHttpObject():HttpObject{
        return HttpObject()
    }
}