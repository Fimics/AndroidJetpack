package com.mic.dagger.demo2.d01_inject_component;


import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class NetModule {

    private Application application;

    public NetModule(Application application) {
        this.application = application;
    }

    @Provides
    Context getApplication(){
        return application;
    }

    // 第二种方式: 告诉dagger,可以通过调用该方法来获取到要注入对象的实例, retrofit 没能办法通过
    //会从 providerOkhttpClient 拿到okHttpClient实例 传给 providerRetrofit

    @MyScope
//    @Singleton
    @Provides
    public Retrofit providerRetrofit(OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl("https://www.google.com")
                .build();
    }

    // 方法参数：Retrofit retrofit，dagger 会在当前module 查找retrofit实例 然后传到 providerApiService

    /**
     * @Singleton 是Dagger提供的一种作用域实现，作用域就是来管理Component获取对象的生命周期的
     */
//    @Singleton
    @MyScope
    @Provides
    public ApiService providerApiService(Retrofit retrofit){
        return retrofit.create(ApiService.class);
    }

//    @Singleton
    @MyScope
    @Provides
    public OkHttpClient providerOkhttpClient(){
        return new OkHttpClient.Builder().build();
    }
}
