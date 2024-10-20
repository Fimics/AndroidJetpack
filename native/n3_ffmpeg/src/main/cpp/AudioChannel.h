#ifndef FFMPEG_AUDIOCHANNEL_H
#define FFMPEG_AUDIOCHANNEL_H

#include <SLES/OpenSLES_Android.h>
#include "BaseChannel.h"

extern "C" {
#include <libswresample/swresample.h>
}

class AudioChannel : public BaseChannel {

public:
    AudioChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                 AVRational time_base);

    void start();// 开启解码和播放线程

    void stop();

    void initOpenSL();// 初始化OpenSL

    unsigned int getPCM();// PCM重采样

    uint8_t *out_buffer;// 输出缓冲区 TODO 记得释放 av_freep(&out_buffer);

private:
    pthread_t pid_audio_decode;// 解码线程
    pthread_t pid_audio_play;// OpenSL ES初始化线程
    SwrContext *swr_ctx;// 重采样上下文 TODO 记得释放 swr_free(&swr_ctx);
    int out_channels;// 输出通道数，例如双通道
    int out_sample_size;// 输出采样位数，例如16bit
    int out_sample_rate;// 输出采样率，例如44100Hz，一秒内对声音信号采样44100次

    // 音频引擎
    SLEngineItf engineInterface = nullptr;
    // 音频对象
    SLObjectItf engineObject = nullptr;
    // 混音器
    SLObjectItf outputMixObject = nullptr;

    // 播放器
    SLObjectItf bqPlayerObject = nullptr;
    // Player接口
    SLPlayItf bqPlayerInterface = nullptr;
    // 缓冲队列
    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue = nullptr;
};

#endif //FFMPEG_AUDIOCHANNEL_H
