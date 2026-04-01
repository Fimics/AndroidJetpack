package com.mic.nav.volces.tts.sdk;

import com.bytedance.speech.speechengine.SpeechEngine;
import com.bytedance.speech.speechengine.SpeechEngineDefines;
import com.noetix.libcore.utils.KLog;

import org.json.JSONException;
import org.json.JSONObject;

public class NoetixSpeechListener implements SpeechEngine.SpeechListener {

    private static final String TAG = "TTEngine";
    private SpeechEngine speechEngine;

    public void setSpeechEngine(SpeechEngine speechEngine) {
        this.speechEngine = speechEngine;
    }

    @Override
    public void onSpeechMessage(int type, byte[] data, int len) {
        KLog.d(TAG,"type ->"+type + "  data []"+data.length +" len "+len);
        String stdData = "";
        stdData = new String(data);

        switch (type) {
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_START:
                // Callback: 引擎启动成功回调
                KLog.i(TAG, "Callback: 引擎启动成功: data: " + stdData);
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_STOP:
                // Callback: 引擎关闭回调
                KLog.i(TAG, "Callback: 引擎关闭: data: " + stdData);
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_ENGINE_ERROR:
                // Callback: 错误信息回调
                KLog.e(TAG, "Callback: 错误信息: " + stdData);
                speechError(stdData);
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_TTS_SYNTHESIS_BEGIN:
                // Callback: 合成开始回调
                KLog.e(TAG, "Callback: 合成开始: " + stdData);
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_TTS_SYNTHESIS_END:
                // Callback: 合成结束回调
                KLog.e(TAG, "Callback: 合成结束: " + stdData);
                break;
            case SpeechEngineDefines.MESSAGE_TYPE_TTS_AUDIO_DATA:
                // Callback: 音频数据回调
                KLog.e(TAG, String.format("Callback: 音频数据，长度 %d 字节", stdData.length()));
                break;
            default:
                break;
        }
    }

    public void speechError(final String data) {
        try {
            JSONObject reader = new JSONObject(data);
            if (!reader.has("err_code") || !reader.has("err_msg")) {
                return;
            }
            int code = reader.getInt("err_code");
            switch (code) {
                case SpeechEngineDefines.CODE_TTS_LIMIT_QPS:
                case SpeechEngineDefines.CODE_TTS_LIMIT_COUNT:
                case SpeechEngineDefines.CODE_TTS_SERVER_BUSY:
                case SpeechEngineDefines.CODE_TTS_LONG_TEXT:
                case SpeechEngineDefines.CODE_TTS_INVALID_TEXT:
                case SpeechEngineDefines.CODE_TTS_SYNTHESIS_TIMEOUT:
                case SpeechEngineDefines.CODE_TTS_SYNTHESIS_ERROR:
                case SpeechEngineDefines.CODE_TTS_SYNTHESIS_WAITING_TIMEOUT:
                case SpeechEngineDefines.CODE_TTS_ERROR_UNKNOWN:
                    KLog.w(TAG, "When meeting this kind of error, continue to synthesize.");
                    break;
                case SpeechEngineDefines.CODE_CONNECT_TIMEOUT:
                case SpeechEngineDefines.CODE_RECEIVE_TIMEOUT:
                case SpeechEngineDefines.CODE_NET_LIB_ERROR:
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            if (speechEngine!=null){
                speechEngine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "");
            }
        }
    }
}