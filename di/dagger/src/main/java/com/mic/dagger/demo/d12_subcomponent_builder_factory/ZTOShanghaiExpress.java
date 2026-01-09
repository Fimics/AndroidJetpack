package com.mic.dagger.demo.d12_subcomponent_builder_factory;

import dagger.Subcomponent;

@Subcomponent
public interface ZTOShanghaiExpress {
    void inject(Person person);

    @Subcomponent.Factory
    interface Factory {
        ZTOShanghaiExpress create();
    }
}