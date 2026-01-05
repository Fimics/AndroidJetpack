package com.mic.dagger.demo2;


import android.content.Context;

import com.mic.dagger.demo2.d01_inject_component.ApiService;
import com.mic.dagger.demo2.d01_inject_component.InjectFragment;
import com.mic.dagger.demo2.d01_inject_component.MyScope;
import com.mic.dagger.demo2.d01_inject_component.NetModule;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;

@MyScope
//@Singleton
@Component(modules = NetModule.class)
public interface ApplicationComponent {

    //注入到目标类
//    void inject(InjectFragment injectFragment);

    Retrofit retrofit();
    ApiService apiService();
    Context context();

}
