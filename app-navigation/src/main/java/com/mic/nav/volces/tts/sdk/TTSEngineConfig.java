package com.mic.nav.volces.tts.sdk;

import android.util.Log;

import com.bytedance.speech.speechengine.SpeechEngine;
import com.bytedance.speech.speechengine.SpeechEngineDefines;
import com.noetix.libcore.utils.CPUInfoUtils;

public class TTSEngineConfig {

    private static final String TAG = "TTEngine";
    private final int mTtsSilenceDuration = 0;
    private final Double mTtsSpeakSpeed = 1.0;
    private final Double mTtsAudioVolume = 1.0;
    private final Double mTtsAudioPitch = 1.0;

    public void configInitParams(SpeechEngine speechEngine,String debugPath) {
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_ENGINE_NAME_STRING, SpeechEngineDefines.TTS_ENGINE);
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_WORK_MODE_INT, SpeechEngineDefines.TTS_WORK_MODE_ONLINE);

        //【可选配置】Debug & Log
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DEBUG_PATH_STRING, debugPath);
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_LOG_LEVEL_STRING, SpeechEngineDefines.LOG_LEVEL_DEBUG);

        //【可选配置】User ID（用以辅助定位线上用户问题）
        String deviceId = CPUInfoUtils.getCPUSerial("configInitParams");
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_UID_STRING, deviceId);
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DEVICE_ID_STRING, deviceId);

        //【可选配置】是否将合成出的音频保存到设备上，为 true 时需要正确配置 PARAMS_KEY_TTS_AUDIO_PATH_STRING 才会生效
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_TTS_ENABLE_DUMP_BOOL, false);
        // TTS 音频文件保存目录，必须在合成之前创建好且 APP 具有访问权限，保存的音频文件名格式为 tts_{reqid}.wav, {reqid} 是本次合成的请求 id
        // PARAMS_KEY_TTS_ENABLE_DUMP_BOOL 配置为 true 的音频时为【必需配置】，否则为【可选配置】
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_AUDIO_PATH_STRING, debugPath);

        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_AUDIO_STREAM_TYPE_INT, 1);

        //【可选配置】合成出的音频的采样率，默认为 24000
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_SAMPLE_RATE_INT, 16000);
        //【可选配置】打断播放时使用多长时间淡出停止，单位：毫秒。默认值 0 表示不淡出
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_AUDIO_FADEOUT_DURATION_INT, 200);

        // ------------------------ 在线合成相关配置 -----------------------


        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_ID_STRING, TTSConfig.APPID);
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_TOKEN_STRING, TTSConfig.TOKEN);

        //【必需配置】语音合成服务域名
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_ADDRESS_STRING, TTSConfig.DEFAULT_ADDRESS);

        //【必需配置】语音合成服务Uri
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_URI_STRING, TTSConfig.TTS_DEFAULT_URI);

        //【必需配置】语音合成服务所用集群
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_CLUSTER_STRING, TTSConfig.TTS_DEFAULT_CLUSTER);

        //【可选配置】是否允许在 websocket 建连失败时自动重连
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_ENABLE_WS_RECONNECT_BOOL, true);
        //【可选配置】在线合成下发的 opus-ogg 音频的压缩倍率
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_COMPRESSION_RATE_INT, 10);
    }


    public void configStartTtsParams(SpeechEngine speechEngine) {
        //【必需配置】TTS 使用场景
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_SCENARIO_STRING, SpeechEngineDefines.TTS_SCENARIO_TYPE_NOVEL);
        //【可选配置】是否使用 SDK 内置播放器播放合成出的音频，默认为 true
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_TTS_ENABLE_PLAYER_BOOL, false);
        //【可选配置】是否令 SDK 通过回调返回合成的音频数据，默认不返回。
        // 开启后，SDK 会流式返回音频，收到 MESSAGE_TYPE_TTS_AUDIO_DATA_END 回调表示当次合成所有的音频已经全部返回
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_DATA_CALLBACK_MODE_INT, 0);
    }

    public void configSynthesisParams(SpeechEngine speechEngine,String text) {
        //【可选配置】需合成的文本的类型，支持直接传文本(TTS_TEXT_TYPE_PLAIN)和传 SSML 形式(TTS_TEXT_TYPE_SSML)的文本
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_TEXT_TYPE_STRING, SpeechEngineDefines.TTS_TEXT_TYPE_PLAIN);
        Log.e(TAG, "Synthesis Text: " + text);
        //【必需配置】需合成的文本，不可超过 80 字
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_TEXT_STRING, text);
        //【可选配置】用于控制 TTS 音频的语速，支持的配置范围参考火山官网 语音技术/语音合成/离在线语音合成SDK/参数说明 文档
        speechEngine.setOptionDouble(SpeechEngineDefines.PARAMS_KEY_TTS_SPEED_RATIO_DOUBLE, mTtsSpeakSpeed);
        //【可选配置】用于控制 TTS 音频的音量，支持的配置范围参考火山官网 语音技术/语音合成/离在线语音合成SDK/参数说明 文档
        speechEngine.setOptionDouble(SpeechEngineDefines.PARAMS_KEY_TTS_VOLUME_RATIO_DOUBLE, mTtsAudioVolume);
        //【可选配置】用于控制 TTS 音频的音高，支持的配置范围参考火山官网 语音技术/语音合成/离在线语音合成SDK/参数说明 文档
        speechEngine.setOptionDouble(SpeechEngineDefines.PARAMS_KEY_TTS_PITCH_RATIO_DOUBLE, mTtsAudioPitch);
        //【可选配置】是否在文本的每句结尾处添加静音段，单位：毫秒，默认为 0ms
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_SILENCE_DURATION_INT, mTtsSilenceDuration);

        // ------------------------ 在线合成相关配置 -----------------------

        //【必需配置】在线合成使用的发音人代号
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_ONLINE_STRING, "other");
        //【必需配置】在线合成使用的音色代号
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_VOICE_TYPE_ONLINE_STRING, "zh_female_cancan_mars_bigtts");

        //【可选配置】是否打开在线合成的服务端缓存，默认关闭
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_TTS_ENABLE_CACHE_BOOL, false);
        //【可选配置】指定在线合成的语种，默认为空，即不指定
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_LANGUAGE_ONLINE_STRING, null);
        //【可选配置】是否启用在线合成的情感预测功能
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_TTS_WITH_INTENT_BOOL, true);
        //【可选配置】需要返回详细的播放进度或需要启用断点续播功能时应配置为 1, 否则配置为 0 或不配置
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_TTS_WITH_FRONTEND_INT, 0);
        //【可选配置】需要返回字粒度的播放进度时应配置为 simple, 同时要求 PARAMS_KEY_TTS_WITH_FRONTEND_INT 也配置为 1; 默认为空
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_TTS_FRONTEND_TYPE_STRING, "simple");
    }

}
