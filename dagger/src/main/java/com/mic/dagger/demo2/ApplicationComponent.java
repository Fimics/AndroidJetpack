package com.mic.dagger.demo2;


import com.mic.dagger.demo2.d01_inject_component.InjectFragment;
import com.mic.dagger.demo2.d01_inject_component.InjectFragmentTest;
import com.mic.dagger.demo2.d01_inject_component.NetModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = NetModule.class)
public interface ApplicationComponent {

    //注入到目标类
    void inject(InjectFragment injectFragment);

    void inject(InjectFragmentTest injectFragmentTest);
}
