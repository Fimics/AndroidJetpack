//
// Created by mac on 2023/4/11.
//
#include <jni.h>
#include <string>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"

// 默认情况下，就是静态注册，静态注册是最简单的方式，NDK开发过程中，基本上使用静态注册
// Android 系统的C++源码：基本上都是动态注册（麻烦）

// 静态注册： 优点：开发简单
// 缺点
// 1.JNI函数名非常长
// 2.捆绑 上层 包名 + 类名
// 3.运行期 才会去 匹配JNI函数，性能上 低于 动态注册
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_staticRegister(JNIEnv *env, jobject thiz) {
    // TODO: implement staticRegister()
}

JavaVM *jVm = nullptr; // 0x003545 系统乱值，C++11后，取代NULL，作用是可以初始化指针赋值
const char *mainActivityClassName = "com/mic/jnibase/NativeLib";

// native 真正的函数
// void dynamicMethod01(JNIEnv *env, jobject thiz) { // OK的
void dynamicMethod01(JNIEnv *env, jobject thiz) { // 也OK  如果你用不到  JNIEnv jobject ，可以不用写
    LOGD("我是动态注册的函数 dynamicMethod01...");
}

int dynamicMethod02(JNIEnv *env, jobject thiz, jstring valueStr) { // 也OK
    const char *text = env->GetStringUTFChars(valueStr, nullptr);
    LOGD("我是动态注册的函数 dynamicMethod02... %s", text);
    env->ReleaseStringUTFChars(valueStr, text);
    return 200;
}

/*
     typedef struct {
        const char* name;       // 函数名
        const char* signature; // 函数的签名
        void*       fnPtr;     // 函数指针
     } JNINativeMethod;
     */
static const JNINativeMethod jniNativeMethod[] = {
        {"dynamicJavaMethod01", "()V",                   (void *) (dynamicMethod01)},
        {"dynamicJavaMethod02", "(Ljava/lang/String;)I", (int *) (dynamicMethod02)},
};

//2调用System.loadLibrary("native-lib") 时会调用JNI_OnLoad函数
// Java：像 Java的构造函数，如果你不写构造函数，默认就有构造函数，如果你写构造函数 覆写默认的构造函数
// JNI JNI_OnLoad函数，如果你不写JNI_OnLoad，默认就有JNI_OnLoad，如果你写JNI_OnLoad函数 覆写默认的JNI_OnLoad函数
extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *javaVm, void *) {
    // this.javaVm = javaVm;
    ::jVm = javaVm;
    // 做动态注册 全部做完
    JNIEnv *jniEnv = nullptr;
    int result = javaVm->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6);
    // result 等于0  就是成功    【C库 FFmpeg 成功就是0】
    if (result != JNI_OK) {
        return -1; // 会奔溃，故意奔溃
    }

    LOGE("System.loadLibrary ---》 JNI Load init");
    jclass mainActivityClass = jniEnv->FindClass(mainActivityClassName);
    // jint RegisterNatives(Class, 我们的数组==jniNativeMethod， 注册的数量 = 2)
    jniEnv->RegisterNatives(mainActivityClass,
                            jniNativeMethod,
                            sizeof(jniNativeMethod) / sizeof(JNINativeMethod));
    LOGE("动态 注册没有毛病");
    return JNI_VERSION_1_6; //  // AS的JDK在JNI默认最高1.6      存Java的JDKJNI 1.8
}
