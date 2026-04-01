package com.mic.nav.volces.tts.sdk;


import android.os.Environment;
import android.util.Log;

import java.io.File;

public class TTSConfig {

    private static final String TAG = "TTSConfig";
    private static final String TTS_DEBUG_DIR= Environment.getExternalStorageDirectory().getAbsolutePath() + "/tts_debug";
    // User Info
    public static final String UID = "YOUR USER ID";

    // Device Info
    public static final String DID = "YOUR DEVICE ID";

    // Online & Resource Authorization
    public static final String APPID = "8245179685";
    public static final String TOKEN = "Bearer;FjJXNGResq3qAeh0w1CJW73Y9qdBy3MX";
    public static final String APP_VERSION = "YOUR APP VERSION";

    // Offline Authorization
    public static final String AUTHENTICATE_ADDRESS = "AUTHENTICAT ADDRESS";
    public static final String AUTHENTICATE_URI = "AUTHENTICATE URI";
    public static final String LICENSE_NAME = "YOUR LICENSE NAME";
    public static final String LICENSE_BUSI_ID = "YOUR LICENSE BUSI_ID";
    public static final String SECRET = "24QT-lT-nlHq_rEcMDuamGzAXNtQoXyG";
    public static final String BUSINESS_KEY = "YOUR BUSINESS KEY";

    // Address
    public static final String DEFAULT_ADDRESS = "wss://openspeech.bytedance.com";
    public static final String DEFAULT_HTTP_ADDRESS = "https://openspeech.bytedance.com";

    // ASR
    public static final String ASR_DEFAULT_URI = "/api/v2/asr";
    public static final String ASR_DEFAULT_CLUSTER = "";
    public static final String ASR_DEFAULT_MODEL_NAME = "YOUR ASR MODEL NAME";
    
    // AU
    public static final String AU_DEFAULT_APP_ID = APPID;
    public static final String AU_DEFAULT_ADDRESS = DEFAULT_ADDRESS;
    public static final String AU_DEFAULT_URI = "/api/v1/sauc";
    public static final String AU_DEFAULT_CLUSTER = "YOUR AU CLUSTER";

    // TTS
    public static final String TTS_DEFAULT_URI = "/api/v1/tts/ws_binary";
    public static final String TTS_DEFAULT_CLUSTER = "volcano_tts";
    public static final String TTS_DEFAULT_BACKEND_CLUSTER = "YOUR TTS BACKEND CLUSTER";
    public static final String TTS_DEFAULT_ONLINE_VOICE = "TTS ONLINE VOICE";
    public static final String TTS_DEFAULT_ONLINE_VOICE_TYPE = "TTS ONLINE VOICE TYPE";
    public static final String TTS_DEFAULT_OFFLINE_VOICE = "TTS OFFLINE VOICE";
    public static final String TTS_DEFAULT_OFFLINE_VOICE_TYPE = "TTS OFFLINE VOICE TYPE";
    public static final String TTS_DEFAULT_ONLINE_LANGUAGE = "TTS ONLINE LANGUAGE";
    public static final String TTS_DEFAULT_OFFLINE_LANGUAGE = "TTS OFFLINE LANGUAGE";
    public static final String[] TTS_DEFAULT_DOWNLOAD_OFFLINE_VOICES = new String[]{};

    // VoiceClone
    public static final String VOICECLONE_DEFAULT_UIDS = "uid_1;uid_2";
    public static final int VOICECLONE_DEFAULT_TASK_ID = -1;

    // VoiceConv
    public static final String VOICECONV_DEFAULT_URI = "/api/v1/voice_conv/ws";
    public static final String VOICECONV_DEFAULT_CLUSTER = "YOUR VOICECONV CLUSTER";
    public static final String VOICECONV_DEFAULT_VOICE = "VOICECONV VOICE";
    public static final String VOICECONV_DEFAULT_VOICE_TYPE = "VOICECONV VOICE TYPE";

    // Fulllink
    public static final String FULLLINK_DEFAULT_URI = "FULLLINK URI";

    // Dialog
    public static final String DIALOG_DEFAULT_URI = "DIALOG URI";
    public static final String DIALOG_DEFAULT_APP_ID = "DIALOG APP ID";
    public static final String DIALOG_DEFAULT_ID = "DIALOG ID";
    public static final String DIALOG_DEFAULT_ROLE = "DIALOG ROLE";
    public static final String DIALOG_DEFAULT_CLOTHES_TYPE = "DIALOG CLOTHES TYPE";
    public static final String DIALOG_DEFAULT_TTA_VOICE_TYPE = "DIALOG TTA_VOICE_TYPE";

    // CAPT
    public static final String CAPT_DEFAULT_MDD_URI = "CAPT MDD URI";
    public static final String CAPT_DEFAULT_CLUSTER = "YOUR CAPT CLUSTER";

    public String doTryCreateTargetDir() {
        return tryCreateTargetDir(TTS_DEBUG_DIR);
    }

    private String tryCreateTargetDir(String path) {
        File dir = new File(path);
        String pathName=null;

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                Log.d(TAG, "robot_config directory created successfully at: " + dir.getAbsolutePath());
            } else {
                Log.e(TAG, "Failed to create robot_config directory at: " + dir.getAbsolutePath());
            }
            pathName = dir.getAbsolutePath();
        } else {
            Log.d(TAG, "robot_config directory already exists at: " + dir.getAbsolutePath());
            pathName= dir.getAbsolutePath();
        }
        return pathName;
    }

}
