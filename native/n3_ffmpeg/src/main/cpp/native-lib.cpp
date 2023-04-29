#include <jni.h>
#include <string>

// 有坑，会报错，必须混合编译
// #include <libavutil/avutil.h>

extern "C" {
#include "include/libavutil/avutil.h"
}

// 日志输出
#include <android/log.h>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_mic_jnibase_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string version = "ffmpeg version ->";
    version.append(av_version_info());
    return env->NewStringUTF(version.c_str());
}