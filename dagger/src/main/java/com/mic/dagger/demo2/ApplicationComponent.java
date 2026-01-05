package com.mic.dagger.demo2;


import android.content.Context;

import com.mic.dagger.demo2.d01_inject_component.ApiService;
import com.mic.dagger.demo2.d01_inject_component.BindsModule;
import com.mic.dagger.demo2.d01_inject_component.InjectFragment;
import com.mic.dagger.demo2.d01_inject_component.MyScope;
import com.mic.dagger.demo2.d01_inject_component.NetModule;
import com.mic.dagger.demo2.d01_inject_component.StudentComponent;
import com.mic.dagger.demo2.d01_inject_component.SubComponentModule;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;

@MyScope
//@Singleton
@Component(modules = {NetModule.class, SubComponentModule.class, BindsModule.class})
public interface ApplicationComponent {

    //注入到目标类
//    void inject(InjectFragment injectFragment);

     // 如果使用组件 依赖需要声明下面方法，如果使用子组件则不需要
//    Retrofit retrofit();
//    ApiService apiService();
//    Context context();

    StudentComponent.Factory studentComponentFactory();

}
