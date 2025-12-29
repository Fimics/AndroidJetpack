package com.mic.dagger.subcomponent.module

import com.mic.dagger.subcomponent.`object`.SubObject
import dagger.Module
import dagger.Provides

@Module
class SubModule {

    @Provides
    fun getSubObject(): SubObject {
        return SubObject()
    }
}