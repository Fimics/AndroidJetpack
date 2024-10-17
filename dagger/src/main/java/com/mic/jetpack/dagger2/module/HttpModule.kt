package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.HttpObject
import com.mic.jetpack.dagger2.scope.AppScope
import dagger.Module
import dagger.Provides

@Module
class HttpModule {

    @AppScope
    @Provides
    fun providerHttpObject():HttpObject{
        return HttpObject()
    }
}