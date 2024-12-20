package com.mic.jnibase;

import android.util.Log;

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

    //----------------------------------------4444444444444-----------------------------------------
    // 第一部分 动态注册 动态注册报红没关系，不用生成接口 区域 =======================================================
    public native void staticRegister(); // 静态注册 - 偷懒
    public native void dynamicJavaMethod01(); // 动态注册1
    public native int dynamicJavaMethod02(String valueStr); // 动态注册2

    // 第二部分 JNI线程 区域 =====================================================
    public native void nativeThread(); // Java层 调用 Native层 的函数，完成JNI线程
    public native void closeThread(); // 释放全局引用

    // 第三部分 纠结纠结细节 区域 ==================================================
    public native void nativeFun1();
    public native void nativeFun2(); // 2
    public static native void staticFun3(); // 3
    public static native void staticFun4();

    interface UpdateUiCallback{
        void onUpdateUi();
    }
    UpdateUiCallback updateUiCallback;

    public void setUpdateUiCallback(UpdateUiCallback updateUiCallback) {
        this.updateUiCallback = updateUiCallback;
    }

    public void updateActivityUI(){
        if (updateUiCallback!=null){
            updateUiCallback.onUpdateUi();
        }
    }

    //----------------------------------------555555555555555-----------------------------------------
    public native  void sort(int [] array);
    public static native void localCache(String name); // 普通的局部缓存，弊端演示
    // 下面 静态缓存
    public static native void initStaticCache(); // 初始化静态缓存
    public static native void staticCache(String name);
    public static native void clearStaticCache(); // 清除化静态缓存
    // 下面是异常处理
    public static native void exceptionNative();
    public static native void exception2Native2() throws NoSuchFieldException; // NoSuchFieldException接收C++层抛上来的异常
    public static native void exceptionJava();
    public static native String thisAction();
    // 假设这里定义了一堆变量
    static String name1  ="T1";
    static String name2  ="T2";
    static String name3  ="T3";
    static String name4  ="T4";
    static String name5  ="T5";
    static String name6  ="T6";
    // 专门给 C++（native层） 层调用的 函数
    public static void show() throws Exception {
        Log.d("jnibase", "show: 1111");
        Log.d("jnibase", "show: 1111");
        Log.d("jnibase", "show: 1111");
        throw new NullPointerException("我是Java中抛出的异常，我的show方法里面发送了Java逻辑错误");
    }

}