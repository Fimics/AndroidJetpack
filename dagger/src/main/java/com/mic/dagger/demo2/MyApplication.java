package com.mic.dagger.demo2;

import android.app.Application;


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
