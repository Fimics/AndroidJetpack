#include <jni.h>
#include <string>

// 日志输出
#include <android/log.h>

#define TAG "Derry"
// __VA_ARGS__ 代表 ...的可变参数
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG,  __VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG,  __VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG,  __VA_ARGS__);

// 我先声明 int get()，运行后，你自己去找 实现（libgetndk.a库里面的实现），如果找不到报错
extern "C" { // 由于库 是C编写的，必须采用C的编译方式
    extern int get();
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_mic_jnibase_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";

    LOGE("get:%d\n", get());
    return env->NewStringUTF(hello.c_str());
}