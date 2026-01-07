package com.mic.hilt.demo2;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
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
}
