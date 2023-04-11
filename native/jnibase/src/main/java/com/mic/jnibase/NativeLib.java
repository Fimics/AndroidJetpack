package com.mic.jnibase;

public class NativeLib {

    static {
        // System.load(D:/xxx/xxxx/xxx/native-lib); 这种是可以绝对路径的加载动态链接库文件
        // 这种是从库目录遍历层级目录，去自动的寻找   apk里面的lib/libnative-lib.so
        System.loadLibrary("jnibase");
    }

    private static NativeLib instance = null;

    public static NativeLib getInstance() {
        if (instance == null) {
            instance = new NativeLib();
        }
        return instance;
    }

    //11111111111111111111111111111
    public static final int A = 100;
    public String name = "android";

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

    //3.fmod 变声
    //Android NDK 导入 C库，开发流程，一劳永逸，任何C库 都是一样的

    interface FmodCallback {
        void onPlalyerEnd(String msg);
    }

    FmodCallback fmodCallback;

    public void setFmodCallback(FmodCallback fmodCallback) {
        this.fmodCallback = fmodCallback;
    }

    public static final int MODE_NORMAL = 0; // 正常
    public static final int MODE_LUOLI = 1; //
    public static final int MODE_DASHU = 2; //
    public static final int MODE_JINGSONG = 3; //
    public static final int MODE_GAOGUAI = 4; //
    public static final int MODE_KONGLING = 5; //

    public native void voiceChangeNative(int modeNormal, String path);

    private void playerEnd(String msg) {
        if (fmodCallback != null) {
            fmodCallback.onPlalyerEnd(msg);
        }
    }

}