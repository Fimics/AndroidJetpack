//
// Created by mac on 2023/4/12.
//
#include <jni.h>
#include <string>
#include "com_mic_jnibase_DParcel.h"
#include "xlog.h"
#include "DParcel.h"

// 全局的  OpenCV  Parcel 都是这样干的 ....
static DParcel * dParcel = 0;
// 早期：DerryPlayer * py
// 如果在native中定义成全局变量的话，java层没办法控制了  是对的，  我还是能共享起来

// 【获取C++对象指针的值，作为共享】

extern "C"
JNIEXPORT jlong JNICALL
Java_com_mic_jnibase_DParcel_nativeCreate(JNIEnv *env, jobject thiz) {
    DParcel * dParcel = new DParcel();
    return reinterpret_cast<jlong>(dParcel);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_DParcel_nativeWriteInt(JNIEnv *env, jobject thiz, jlong m_native_ptr,
                                            jint val) {

    // C的命名标准：   AVFormatContext    fmt_cxt        VIM 没有提示 全部手敲
    DParcel * dParcel = reinterpret_cast<DParcel *>(m_native_ptr);
    dParcel->writeInt(val);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_DParcel_nativeSetDataPosition(JNIEnv *env, jobject thiz, jlong m_native_ptr,
                                                   jint pos) {
    DParcel * dParcel = reinterpret_cast<DParcel *>(m_native_ptr);
    dParcel->setDataPosition(pos);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_mic_jnibase_DParcel_nativeReadInt(JNIEnv *env, jobject thiz, jlong m_native_ptr) {
    /    DParcel * dParcel = reinterpret_cast<DParcel *>(m_native_ptr);
    return dParcel->readInt();
}