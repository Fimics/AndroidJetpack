package com.mic.dagger.demo2.d01_inject_component;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class StudentModule {

//    @Named("stu1")
    @Stu1Qualifier
    @Provides
    Student provideStudent(){
        return new Student();
    }

//    @Named("stu2")
    @Stu2Qualifier
    @Provides
    Student provideStudent2(){
        return new Student("new name");
    }
}
