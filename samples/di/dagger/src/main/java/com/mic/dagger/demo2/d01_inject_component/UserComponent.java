package com.mic.dagger.demo2.d01_inject_component;

import com.mic.dagger.demo2.ApplicationComponent;

import dagger.Component;

//dependencies = ApplicationComponent.class  表示这个Component依赖到了ApplicationComponent组件
@UserScope
@Component(modules = UserModule.class,dependencies = ApplicationComponent.class)
public interface UserComponent {

    void inject(InjectFragment injectFragment);


}
