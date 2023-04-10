package com.mic.jnibase;

public class NativeLib {

    static {
        System.loadLibrary("jnibase");
    }

    private static NativeLib instance = null;
    public static NativeLib getInstance(){
        if (instance==null){
            instance=new NativeLib();
        }
        return instance;
    }

    public static final int A=100;
    public String name="android";
    public native void changeName();
    public static native void changeAge();
    public native void callAddMethod();

    // 专门写一个函数，给native成调用
    public int add(int number1, int number2) {
        return number1 + number2 + 8;
    }

}