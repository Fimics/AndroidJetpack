package com.mic.jetpack.hilt.module

import com.mic.jetpack.hilt.`object`.HttpObject
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
    fun getHttpObject():HttpObject{
        return  HttpObject()
    }

}