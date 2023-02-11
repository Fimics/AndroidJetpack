package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.PresenterObject
import com.mic.jetpack.dagger2.scope.UserScope
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {

    @UserScope
    @Provides
    fun providePresenter():PresenterObject{
        return  PresenterObject()
    }
}