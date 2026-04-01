package com.mic.nav.volces.tts.sdk;

import android.util.Log;

import com.bytedance.speech.speechengine.SpeechEngine;
import com.bytedance.speech.speechengine.SpeechEngineDefines;
import com.bytedance.speech.speechengine.SpeechEngineGenerator;
import com.noetix.libcore.utils.KLog;

public class TTSEngine {

    private static final String TAG = "TTEngine";
    private SpeechEngine mSpeechEngine = null;
    private String mDebugPath;
    private TTSEngineConfig engineConfig;
    private NoetixSpeechListener noetixSpeechListener;

    public void init() {
        engineConfig = new TTSEngineConfig();
        noetixSpeechListener = new NoetixSpeechListener();
        TTSConfig ttsConfig = new TTSConfig();
        mDebugPath = ttsConfig.doTryCreateTargetDir();
        KLog.d(TAG, "mDebugPath ->" + mDebugPath);

        int ret = SpeechEngineDefines.ERR_NO_ERROR;
        if (mSpeechEngine == null) {
            KLog.i(TAG, "创建引擎.");
            mSpeechEngine = SpeechEngineGenerator.getInstance();
            mSpeechEngine.createEngine();
        }
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            KLog.d("","Create engine failed: " + ret);
            return;
        }
        KLog.d(TAG, "SDK 版本号: " + mSpeechEngine.getVersion());

        KLog.i(TAG, "配置初始化参数.");
        engineConfig.configInitParams(mSpeechEngine,mDebugPath);

        KLog.i(TAG, "引擎初始化.");
        ret = mSpeechEngine.initEngine();
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            String errMessage = "初始化失败，返回值: " + ret;
            KLog.e(TAG, errMessage);
            return;
        }
        KLog.i(TAG, "设置消息监听");
        mSpeechEngine.setListener(noetixSpeechListener);
        noetixSpeechListener.setSpeechEngine(mSpeechEngine);
        KLog.i(TAG, "引擎初始化成功!");
    }

    public void unInit() {
        if (noetixSpeechListener!=null){
            noetixSpeechListener=null;
        }
        if (mSpeechEngine != null) {
            KLog.i(TAG, "引擎析构.");
            mSpeechEngine.destroyEngine();
            mSpeechEngine = null;
            KLog.i(TAG, "引擎析构完成!");
        }
    }

    public void startEngine() {
        // Directive：启动引擎前调用SYNC_STOP指令，保证前一次请求结束。
        Log.i(TAG, "关闭引擎（同步）");
        Log.i(TAG, "Directive: DIRECTIVE_SYNC_STOP_ENGINE");
        int ret = mSpeechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "");
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            Log.e(TAG, "send directive syncstop failed, " + ret);
        } else {
            engineConfig.configStartTtsParams(mSpeechEngine);
            Log.i(TAG, "启动引擎");
            Log.i(TAG, "Directive: DIRECTIVE_START_ENGINE");
            ret = mSpeechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, "");
            if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
                String message = "发送启动引擎指令失败, " + ret;
            }
        }
    }

    public void stopEngine() {
        Log.i(TAG, "关闭引擎（异步）");
        Log.i(TAG, "Directive: DIRECTIVE_STOP_ENGINE");
        mSpeechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "");
    }


    public void triggerSynthesis(int seq,int status,String text) {
        KLog.d(TAG,"triggerSynthesis seq "+seq+"  status "+status+"  text   "+text);
        engineConfig.configSynthesisParams(mSpeechEngine,text);
        // DIRECTIVE_SYNTHESIS 是连续合成必需的一个指令，在成功调用 DIRECTIVE_START_ENGINE 之后，每次合成新的文本需要再调用 DIRECTIVE_SYNTHESIS 指令
        // DIRECTIVE_SYNTHESIS 需要在当前没有正在合成的文本时才可以成功调用，否则就会报错 -901，可以在收到 MESSAGE_TYPE_TTS_SYNTHESIS_END 之后调用
        // 当使用 SDK 内置的播放器时，为了避免缓存过多的音频导致内存占用过高，SDK 内部限制缓存的音频数量不超过 5 次合成的结果，
        // 如果 DIRECTIVE_SYNTHESIS 后返回 -902, 就需要在下一次收到 MESSAGE_TYPE_TTS_FINISH_PLAYING 再去调用 MESSAGE_TYPE_TTS_FINISH_PLAYING
        KLog.i(TAG, "触发合成");
        KLog.i(TAG, "Directive: DIRECTIVE_SYNTHESIS");
        int ret = mSpeechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNTHESIS, "");
        if (ret != 0) {
            KLog.e(TAG, "Synthesis faile: " + ret);
            if (ret == SpeechEngineDefines.ERR_SYNTHESIS_PLAYER_IS_BUSY) {
            } else {
                String message = "发送合成指令失败, " + ret;
                sendSynthesisDirectiveFailed(message);
            }
        }
    }

    private void sendSynthesisDirectiveFailed(String tipText) {
        Log.e(TAG, tipText);
        mSpeechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "");
    }


}

