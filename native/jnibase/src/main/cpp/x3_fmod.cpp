#include <jni.h>
#include <string>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"

//
// Created by mac on 2023/4/10.
//

extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_voiceChangeNative(JNIEnv *env, jobject thiz, jint mode_normal,
                                                 jstring path) {
    // TODO: implement voiceChangeNative()
}