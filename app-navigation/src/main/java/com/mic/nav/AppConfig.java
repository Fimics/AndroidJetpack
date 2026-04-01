package com.mic.nav;


import com.noetix.libcore.utils.P;

public class AppConfig {

    public static boolean smallScreen = true;

    public static final String KEY_SPEED="speed";
    public static final String KEY_VOICE="voice";


    public static final String KEY_VCN="vcn";
    public static final String VCN_MALE = "male";
    public static final String VCN_FEMALE = "female";
    public static final String KEY_TEMPLATE="template";
    public static final String CSV_FILE_HIGH = "high.csv";
    public static final String CSV_FILE_MID = "mid.csv";
    public static final String CSV_FILE_LOW = "low.csv";

    public static final String KEY_MIC="mic";
    public static final String MIC_VALUE_IN="in";
    public static final String MIC_VALUE_EXT="ext";

    public static final String KEY_LLM="llm";
    public static final String LLM_VALUE_DOUBAO="doubao";
    public static final String LLM_VALUE_XUNFEI="xunfei";

    public static final String KEY_ASR_VOICE="chat_asr";
    public static final String ASR_VALUE_AUTO="auto";
    public static final String ASR_VALUE_MANUAL="manual";
    public static final String ASR_VALUE_MODAL="modal";


    public static final String KEY_TTS="key_tts";
    public static final String KEY_TTS_XF="key_tts_xf";
    public static final String KEY_TTS_VOLCES="key_tts_volces";


//    add by ak server URL
    public  static  final  String SERVER_URL = "http://192.168.101.191:8083";

    public  static  final  String WEB_SOCKET_URL = "ws://114.251.228.195:8083/ws/relay/";

    public static boolean ttsUseXunFei(){
        String value =P.get().getString(KEY_TTS,KEY_TTS_VOLCES);
        return value.equals(KEY_TTS_XF);
    }

    public static boolean llmUseDouBao(){
        String value =P.get().getString(KEY_LLM,LLM_VALUE_XUNFEI);
        return value.equals(LLM_VALUE_DOUBAO);
    }

    public static int getTtsSpeed(){
        return P.get().getInt(KEY_SPEED,50);
    }

    public static int getTtsVolume(){
        return P.get().getInt(KEY_VOICE,50);
    }
}
