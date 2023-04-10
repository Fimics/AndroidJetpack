package com.mic.jnibase;

public class NativeLib {

    static {
        // System.load(D:/xxx/xxxx/xxx/native-lib); 这种是可以绝对路径的加载动态链接库文件
        // 这种是从库目录遍历层级目录，去自动的寻找   apk里面的lib/libnative-lib.so
        System.loadLibrary("jnibase");
    }

    private static NativeLib instance = null;
    public static NativeLib getInstance(){
        if (instance==null){
            instance=new NativeLib();
        }
        return instance;
    }

    //11111111111111111111111111111
    public static final int A=100;
    public String name="android";
    public native void changeName();
    public static native void changeAge();
    public native void callAddMethod();

    // 专门写一个函数，给native成调用
    public int add(int number1, int number2) {
        return number1 + number2 + 8;
    }

    //222222222222222222222222
    public native void testArrayAction(int count, String textInfo, int[] ints, String[] strs); // String引用类型，玩数组
    public native void putObject(Student student, String str); // 传递引用类型，传递对象
    public native void insertObject(); // 凭空创建Java对象
    public native void testQuote(); // 测试引用
    public native void delQuote(); // 释放全局引用

}