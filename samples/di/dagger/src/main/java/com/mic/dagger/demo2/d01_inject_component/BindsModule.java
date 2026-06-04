package com.mic.dagger.demo2.d01_inject_component;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class BindsModule {

    @Binds
    abstract IBinds bindImplA(BindsImplA bindsImplA);

    abstract IBinds bindImplB(BindImplB bindsImplB);

    @Provides
    static BindImplB provideIBindsB(){
        return new BindImplB();
    }

    @Provides
    static BindsImplA provideIBindsA(){
        return new BindsImplA();
    }
}
