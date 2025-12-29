package com.mic.dagger.module

import com.mic.dagger.`object`.DatabaseObject
import com.mic.dagger.scope.AppScope
import dagger.Module
import dagger.Provides

/**
 * 用来提供对象
 */

@Module
class DatabaseModule {

    @AppScope
    @Provides //对象提供注解
    fun providerDatabaseObject(): DatabaseObject {
        return DatabaseObject()
    }
}