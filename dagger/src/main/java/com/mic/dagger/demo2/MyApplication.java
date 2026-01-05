package com.mic.dagger.demo2;

import android.app.Application;

import com.mic.dagger.demo2.d01_inject_component.ApplicationComponent;
import com.mic.dagger.demo2.d01_inject_component.DaggerApplicationComponent;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }
    static ApplicationComponent applicationComponent = DaggerApplicationComponent.create();

    public static ApplicationComponent getApplicationComponent(){
        return  applicationComponent;
    }
}
