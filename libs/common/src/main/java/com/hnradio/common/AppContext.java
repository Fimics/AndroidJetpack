package com.hnradio.common;

import android.app.Application;

/**
 * Created by yutf on 2017/8/17 0017.
 */

public class AppContext extends Application
{
    protected static AppContext context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;
    }

    public void onExceptionShutdown(){

    }

    public static AppContext getContext()
    {
        return context;
    }
}
