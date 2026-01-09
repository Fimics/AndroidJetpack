package com.mic.hilt.demo2;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.components.SingletonComponent;

@InstallIn(ActivityComponent.class)
@Module
public class AppModule {
    @ActivityScoped
    @Provides
    Student provideStudent(){
        return new Student();
    }


    //hilt 提供的限定符 @ApplicationContext  @ActivityContext
    @Provides
    ViewModel provideViewModel(Student student, Application application, Activity activity,@ApplicationContext Context context){
        return new ViewModel(student,application,activity,context);
    }
}
