package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.DatabaseObject
import com.mic.jetpack.dagger2.scope.AppScope
import dagger.Module
import dagger.Provides

/**
 * 用来提供对象
 */

@Module
class DatabaseModule {

    @AppScope
    @Provides //对象提供注解
    fun providerDatabaseObject():DatabaseObject{
        return DatabaseObject()
    }
}