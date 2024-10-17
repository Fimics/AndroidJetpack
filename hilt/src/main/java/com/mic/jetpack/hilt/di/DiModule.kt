package com.mic.jetpack.hilt.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class DiModule {

    @Binds //绑定接口实现类
    abstract fun bindClass(interfaceImpl: InterfaceImpl):IInterface
}