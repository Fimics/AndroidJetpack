package com.mic.dagger.demo2.d01_inject_component;


import com.mic.dagger.demo2.Demo2Fragment;
import com.mic.dagger.demo2.Demo2FragmentTest;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = NetModule.class)
public interface ApplicationComponent {

    //注入到目标类
    void inject(Demo2Fragment demo2Fragment);

    void inject(Demo2FragmentTest demo2FragmentTest);
}
