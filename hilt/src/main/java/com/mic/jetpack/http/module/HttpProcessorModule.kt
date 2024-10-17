package com.mic.jetpack.http.module

import com.mic.jetpack.http.annoation.BindOkhttp
import com.mic.jetpack.http.annoation.BindVolley
import com.mic.jetpack.http.annoation.BindXUtils
import com.mic.jetpack.http.client.IHttpProcessor
import com.mic.jetpack.http.client.OkHttpProcessor
import com.mic.jetpack.http.client.VolleyProcessor
import com.mic.jetpack.http.client.XUtilsProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
abstract class HttpProcessorModule {

    @BindOkhttp
    @Binds
//    @Singleton
    abstract fun bindOkhttp(okHttpProcessor: OkHttpProcessor?): IHttpProcessor?

    @BindVolley
    @Binds
//    @Singleton
    abstract fun bindVolley(volleyProcessor: VolleyProcessor?): IHttpProcessor?

    @BindXUtils
    @Binds
//    @Singleton
    abstract fun bindXUtils(xUtilsProcessor: XUtilsProcessor?): IHttpProcessor?
}