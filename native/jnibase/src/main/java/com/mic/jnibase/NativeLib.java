package com.mic.jnibase;

public class NativeLib {

    // Used to load the 'jnibase' library on application startup.
    static {
        System.loadLibrary("jnibase");
    }

    /**
     * A native method that is implemented by the 'jnibase' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}