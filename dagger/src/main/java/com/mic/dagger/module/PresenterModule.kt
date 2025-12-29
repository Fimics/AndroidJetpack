package com.mic.dagger.module

import com.mic.dagger.`object`.PresenterObject
import com.mic.dagger.scope.UserScope
import dagger.Module
import dagger.Provides

@Module
class PresenterModule {

    @UserScope
    @Provides
    fun providePresenter(): PresenterObject {
        return  PresenterObject()
    }
}