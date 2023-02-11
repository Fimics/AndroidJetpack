package com.mic.jetpack.dagger2.subcomponent.module

import com.mic.jetpack.dagger2.subcomponent.`object`.SubObject
import dagger.Module
import dagger.Provides

@Module
class SubModule {

    @Provides
    fun getSubObject(): SubObject {
        return SubObject()
    }
}