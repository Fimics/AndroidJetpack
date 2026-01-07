package com.mic.hilt.demo2;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModel;
import com.mic.hilt.KLog;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private static final String TAG = "ViewModel";

    private final Student student;
    private final Application application;
    private final Context context;

    @Inject
    public MainViewModel(Student student, Application application, @ApplicationContext Context context) {
        this.student = student;
        this.application = application;
        this.context = context;
    }

    public void test() {
        KLog.d(TAG, "ViewModel test student -> " + student);
        KLog.d(TAG, "ViewModel test application -> " + application);
        KLog.d(TAG, "ViewModel test context -> " + context);
    }
}