package com.mic.hilt.demo.http.module

import com.mic.hilt.demo.http.annoation.BindOkhttp
import com.mic.hilt.demo.http.annoation.BindVolley
import com.mic.hilt.demo.http.annoation.BindXUtils
import com.mic.hilt.demo.http.client.IHttpProcessor
import com.mic.hilt.demo.http.client.OkHttpProcessor
import com.mic.hilt.demo.http.client.VolleyProcessor
import com.mic.hilt.demo.http.client.XUtilsProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

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