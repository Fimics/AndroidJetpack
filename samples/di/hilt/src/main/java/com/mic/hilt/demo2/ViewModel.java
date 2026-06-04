package com.mic.hilt.demo2;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.mic.hilt.KLog;

import javax.inject.Inject;

public class ViewModel {
    private static final String TAG = "ViewModel";
    // Student 是我们自己定义的module提供创建
    Student student;
    //它是由 hilt 预定义的，由hilt创建的
    Application application;

    Activity activity;
    Context context;

//    @Inject
    public ViewModel(Student student, Application application,Activity activity,Context context) {
        this.student = student;
        this.application = application;
        this.activity = activity;
        this.context = context;
    }

    public void test(){
        KLog.d(TAG,"ViewModel test student ->"+student);
        KLog.d(TAG,"ViewModel test application ->"+application);
        KLog.d(TAG,"ViewModel test activity ->"+activity);
        KLog.d(TAG,"ViewModel test context ->"+context);
    }
}
