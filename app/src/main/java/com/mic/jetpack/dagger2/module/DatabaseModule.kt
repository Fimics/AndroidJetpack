package com.mic.jetpack.dagger2.module

import com.mic.jetpack.dagger2.`object`.DatabaseObject
import dagger.Module

/**
 * 用来提供对象
 */
@Module
class DatabaseModule {

    fun providerDatabaseObject():DatabaseObject{
        return DatabaseObject()
    }
}