package com.mic.netconfig;

import android.app.Application;

import com.mic.dagger.demo2.ApplicationComponent;
import com.mic.dagger.demo2.DaggerApplicationComponent;


public class ConfigApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }
    static ApplicationComponent applicationComponent = DaggerApplicationComponent.create();

    public static ApplicationComponent getApplicationComponent(){
        return  applicationComponent;
    }
}
