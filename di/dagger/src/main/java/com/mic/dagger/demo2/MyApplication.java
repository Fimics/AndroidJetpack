package com.mic.dagger.demo2;

import android.app.Application;

import com.mic.dagger.demo2.d01_inject_component.NetModule;


public class MyApplication extends Application {

//    static ApplicationComponent applicationComponent = DaggerApplicationComponent.create();
static ApplicationComponent applicationComponent ;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder().netModule(new NetModule(this)).build();
    }


    public static ApplicationComponent getApplicationComponent(){
        return  applicationComponent;
    }
}
