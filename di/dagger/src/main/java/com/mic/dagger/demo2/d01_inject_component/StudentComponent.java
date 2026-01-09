package com.mic.dagger.demo2.d01_inject_component;

import dagger.Subcomponent;

//StudentComponent 是ApplicationComponent 的子组件
@Subcomponent(modules = StudentModule.class)
public interface StudentComponent {

    //StudentComponent 由factory 创建
    @Subcomponent.Factory
    interface Factory{
        StudentComponent create();
    }

    void inject(SubComponentFragment subComponentFragment);
}
