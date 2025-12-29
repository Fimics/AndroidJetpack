package com.mic.dagger.module

import com.mic.dagger.`object`.HttpObject
import com.mic.dagger.scope.AppScope
import dagger.Module
import dagger.Provides

@Module
class HttpModule {

    @AppScope
    @Provides
    fun providerHttpObject(): HttpObject {
        return HttpObject()
    }
}