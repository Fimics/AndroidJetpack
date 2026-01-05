package com.mic.dagger.demo2.d01_inject_component;

import dagger.Module;
import dagger.Provides;

@Module
public class StudentModule {

    @Provides
    Student provideStudent(){
        return new Student();
    }
}
