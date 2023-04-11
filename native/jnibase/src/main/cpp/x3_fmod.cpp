//
// Created by mac on 2023/4/10.
//
#include <jni.h>
#include <string>
#include <unistd.h>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"
#include "fmod.hpp"
using namespace FMOD;

extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_voiceChangeNative(JNIEnv *env, jobject thiz, jint mode,
                                                 jstring path) {
    char * content_ = "默认：播放完毕";
    // C认识 char *
    const char * path_ = env->GetStringUTFChars(path, NULL);
    // Java 万物皆对象
    // C 万物皆指针
    // Linux 万物皆文件

    System * system = 0; // fmod 音效引擎系统
    Sound * sound = 0;   // fmod 声音
    Channel * channel = 0; // 通道 音轨
    DSP * dsp = 0; // digital signal process  == 数字信号处理

    // Java思想初始化
    // system = xxxx();
    // C的思想初始化
    // xxxxx(system);

    // TODO 第一步 创建系统
    System_Create(&system); // & 取出该指针所对应的地址
    // TODO 第二步：系统的初始化
    system->init(32, FMOD_INIT_NORMAL, 0);
    // TODO 第三步：创建声音
    system->createSound(path_, FMOD_DEFAULT, 0, &sound);
    // TODO 第四步：播放声音
    system->playSound(sound, 0, false, &channel);
    // 判断六个宏
    // TODO 给播放的声音 加特性
    switch (mode) {
        case com_mic_jnibase_NativeLib_MODE_NORMAL:
            content_ = "原生：播放完毕";
            break;
        case com_mic_jnibase_NativeLib_MODE_LUOLI:
            content_ = "萝莉：播放完毕";
            // 音调高 萝莉
            // 1.创建DSP类型是Pitch 音调调节 默认正常：1.0    0.5 ~ 2.0
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            // 2.设置Pitch音调调节为：2.0，音调很高就是萝莉了
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 2.0f);
            // 3.添加音效进去 参数一：0 是因为只有一个dsp
            channel->addDSP(0, dsp);
            break;

        case com_mic_jnibase_NativeLib_MODE_DASHU:
            content_ = "大叔：播放完毕";

            // 音调低 大叔

            // 1.创建DSP类型是Pitch 音调调节 默认正常：1.0  0.5 ~ 2.0
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);

            // 2.设置Pitch音调调节为：2.0，音调很高就是萝莉了
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);

            // 3.添加音效进去 参数一：0 是因为只有一个dsp
            channel->addDSP(0, dsp);
            break;

        case com_mic_jnibase_NativeLib_MODE_GAOGUAI:
            content_ = "搞怪 小黄人：播放完毕"; // 频率快

            // 从通道里面拿频率， 原始频率
            float frequency;
            channel->getFrequency(&frequency);

            // 在原来的频率上 修改频率
            channel->setFrequency(frequency * 1.5f);

            break;

        case com_mic_jnibase_NativeLib_MODE_JINGSONG:
            content_ = "惊悚音 播放完毕";

            // 惊悚音效：特点 很多声音的组合拼接  就会惊悚

            // TODO 音调很低，
            // 大叔音效：特点：音调低
            // 大叔的声音： 特点：音调低 低沉
            system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
            dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.7f);
            channel->addDSP(0, dsp);

            //  TODO 搞点回声
            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 400); // 延时的回音
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 40); // 默认：50  0完全衰减了
            channel->addDSP(1, dsp);

            // TODO 颤抖 Tremolo
            system->createDSPByType(FMOD_DSP_TYPE_TREMOLO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_FREQUENCY, 0.8f);
            dsp->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, 0.8f);
            channel->addDSP(2, dsp);

            // TODO 作业 调节更恐怖的音效 .....

            break;

        case com_mic_jnibase_NativeLib_MODE_KONGLING:
            content_ = "空灵：播放完毕";

            // 回音 ECHO

            system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
            dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 200); // 延时的回音
            dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10); // 默认：50  0完全衰减了
            channel->addDSP(0, dsp);
            break;
    }

    // 死循环
    bool isPalyer = true;
    while (isPalyer) {
        channel->isPlaying(&isPalyer);
        usleep(1000 * 1000); // 每隔一秒
    }

    // 时时刻刻 记得回收
    sound->release();
    system->close();
    system->release();
    env->ReleaseStringUTFChars(path, path_);

    // 调用到上层 [注意：需要放置回收的后面]
    // C 调用到 Java
    // char* ---> jstring  ---> String(Java)
    jstring value = env->NewStringUTF(content_);
    jclass mainCls = env->GetObjectClass(thiz); // MainActivity 对象 ---> Class
    jmethodID playerEndMethod = env->GetMethodID(mainCls, "playerEnd", "(Ljava/lang/String;)V");
    env->CallVoidMethod(thiz, playerEndMethod, value); // 反射 完整     Java的发射
}