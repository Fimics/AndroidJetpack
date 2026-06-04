package com.mic.dagger.demo2.d01_inject_component;

import dagger.Module;
import dagger.Provides;

@Module
public class UserModule {

    @UserScope
    @Provides
    User provideUser(){
        return new User();
    }
}
