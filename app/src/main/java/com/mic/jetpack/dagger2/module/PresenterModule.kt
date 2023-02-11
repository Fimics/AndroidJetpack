package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.PresenterObject
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {

    @Provides
    fun providePresenter():PresenterObject{
        return  PresenterObject()
    }
}