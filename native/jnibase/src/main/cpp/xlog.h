//
// Created by mac on 2023/4/10.
//

#ifndef ANDROIDJETPACK_XLOG_H
#define ANDROIDJETPACK_XLOG_H

#endif //ANDROIDJETPACK_XLOG_H

// NDK工具链里面的 log 库 引入过来
#include <android/log.h>

#define TAG "steven"
// ... 我都不知道传入什么  借助JNI里面的宏来自动帮我填充
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)