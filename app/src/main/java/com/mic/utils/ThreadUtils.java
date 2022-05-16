package com.mic.utils;

import android.os.Looper;

public class ThreadUtils {

    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
