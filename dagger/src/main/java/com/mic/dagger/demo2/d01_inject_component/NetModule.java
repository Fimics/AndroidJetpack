package com.mic.dagger.demo2.d01_inject_component;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class NetModule {

    // 第二种方式: 告诉dagger,可以通过调用该方法来获取到要注入对象的实例, retrofit 没能办法通过
    //会从 providerOkhttpClient 拿到okHttpClient实例 传给 providerRetrofit

    @Singleton
    @Provides
    public Retrofit providerRetrofit(OkHttpClient okHttpClient){
        return new Retrofit.Builder()
                .baseUrl("https://www.google.com")
                .build();
    }

    // 方法参数：Retrofit retrofit，dagger 会在当前module 查找retrofit实例 然后传到 providerApiService
    @Singleton
    @Provides
    public ApiService providerApiService(Retrofit retrofit){
        return retrofit.create(ApiService.class);
    }

    @Singleton
    @Provides
    public OkHttpClient providerOkhttpClient(){
        return new OkHttpClient.Builder().build();
    }
}
