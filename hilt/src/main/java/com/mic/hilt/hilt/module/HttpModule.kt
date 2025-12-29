package com.mic.hilt.hilt.module

import com.mic.hilt.hilt.`object`.HttpObject
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@InstallIn(ActivityComponent::class)
@Module
class HttpModule {

    @Provides
    @ActivityScoped
    fun getHttpObject(): HttpObject {
        return  HttpObject()
    }

}