package com.kk.speech.aiui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.kk.speech.aiui.callback.IASRCallBack;
import com.kk.speech.aiui.callback.ITTSCallBack;
import com.kk.speech.aiui.callback.AudioPlayListener;
import com.kk.speech.aiui.support.PcmAudioPlayer;
import com.kk.speech.aiui.callback.IWakeUpCallBack;
import com.kk.speech.cae.VoiceEngineMgr;
import com.kk.speech.cae.bean.WakeInfo;
import com.kk.speech.cae.dataAdapter.UsbMicAdapter;
import com.kk.speech.cae.engine.HlwNearEngine;
import com.kk.speech.cae.engine.IEngineCallBack;
import com.kk.speech.cae.record.UsbMicRecorder;
import com.kk.speech.cae.record.UsbMicRecorderCallBack;
import com.noetix.libcore.utils.AppGlobals;
import com.noetix.libcore.utils.ByteUtils;
import com.noetix.libcore.utils.FileUtils;
import com.noetix.libcore.utils.KLog;
import com.noetix.libcore.utils.NContext;
import com.noetix.libcore.utils.PcmFileUtils;
import com.noetix.libcore.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AIUISDK {
    private final String TAG = "AIUISDK";
    private static volatile AIUISDK mInstance;
    private Context mContext;
    private AIUIAgent mAiuiAgent;
    private int mCurrentState = AIUIConstant.STATE_IDLE;
    private String mTtsName = "xiaoyan";
    private String mTtsSpeed = "50";
    private final String mTtsPitch = "50";
    private final String mTtsVolume = "100";
    private boolean isRecording = false;
    private volatile boolean isSpeaking = false;
    private String mTtsContent;
    private ITTSCallBack iTtsCallBack;
    private IASRCallBack iAsrCallBack;
    private IWakeUpCallBack iWakeUpCallBack;
    private final String[] mAsrPgsStack = new String[256];
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private String mTextParams;
    private String mAudioParams;
    private final Map<String, Object> mAddedParams = new LinkedHashMap<>();
    private final VoiceEngineMgr mCaeEngineMgr = VoiceEngineMgr.getInstance();
    private volatile PcmFileUtils mTtsFileUtils;

    public static AIUISDK getInstance() {
        if (mInstance == null) {
            synchronized (AIUISDK.class) {
                if (mInstance == null) {
                    mInstance = new AIUISDK();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化SDK
     * (请将aiui_basis.cfg文件放在项目的/assets/cfg文件夹下)
     *
     * @param caeSN 拾音设备SN码
     */
    public void init(String caeSN) {
        initForTTS();
        // 初始化CAE引擎
        initCaeEngine(caeSN);
    }

    /**
     * 仅初始化 AIUI Agent，供 TTS 播报使用（不初始化硬件麦克风阵列 CAE）
     */
    public void initForTTS() {
        this.mContext = AppGlobals.getApplication();
        // 读取配置信息
        try {
            KLog.d(TAG, "初始化IUI (仅TTS)");
            String basis = FileUtils.getAssetsContentForTxt(mContext, "cfg/aiui_basis.cfg");
            JSONObject config = new JSONObject(basis);
            initAiuiAgent(config.toString());
        } catch (JSONException e) {
            KLog.d(TAG, "初始化失败:配置文件解析失败");
        }
        // 格式化识别参数
        formatAiuiParams();
        if (mTtsFileUtils == null) {
            mTtsFileUtils = new PcmFileUtils("/sdcard/robot/audio7/");
        }
    }

    /**
     * 初始化AIUI
     */
    @SuppressLint("CheckResult")
    private void initAiuiAgent(String config) {
        isRecording = false;
        if (mAiuiAgent == null) {
            // 为每一个设备设置对应唯一的SN（最好使用设备硬件信息(mac地址，设备序列号等）生成）
            // 以便正确统计装机量，避免刷机或者应用卸载重装导致装机量重复计数
            KLog.d(TAG, "initAiuiAgent");
            AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, Build.SERIAL);
            mAiuiAgent = AIUIAgent.createAgent(mContext, config, mAiuiListener);
        } else {
            KLog.d(TAG, "initAiuiAgent:---");
            Observable.timer(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        AIUIAgent temp = mAiuiAgent;
                        mAiuiAgent = null;
                        temp.destroy();
                        Observable.timer(300, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(aLong1 -> initAiuiAgent(config));
                    });
        }
    }

    /**
     * 格式化参数
     */
    private void formatAiuiParams() {
        StringBuilder addeds = new StringBuilder();
        if (!mAddedParams.isEmpty()) {
            Set<String> keys = mAddedParams.keySet();
            for (String key : keys) {
                Object value = mAddedParams.get(key);
                if (value != null) {
                    addeds.append(",").append(key).append("=").append(value);
                }
            }
        }
        mTextParams = "data_type=text" + addeds;
        mAudioParams = "data_type=audio,sample_rate=16000" + addeds;
        KLog.d(TAG, "Text:" + mTextParams + "\nAudio:" + mAudioParams);
    }

    /**
     * 发送消息基类
     */
    private void sendMessage(AIUIMessage message) {
        if (message == null) return;
        KLog.d(TAG, "sendMessage0:" + message);
        if (message.data!=null){
            KLog.d(TAG, "sendMessage1:" + new String(message.data));
        }
        if (mAiuiAgent != null) {
            // 确保AIUI处于唤醒状态
            if (mCurrentState != AIUIConstant.STATE_WORKING) {
                wakeupAiuiService(0, 0);
            }
            KLog.d(TAG, "sendMessage2:");
            mAiuiAgent.sendMessage(message);
        }
        KLog.d(TAG, "sendMessage3:" );
    }

    /**
     * 发送文本至AIUI
     */
    public void sendMessage(String message) {
        sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, mTextParams, message.getBytes()));
    }

    /**
     * AIUI全局回调
     */
    private final AIUIListener mAiuiListener = event -> {
        switch (event.eventType) {
            case AIUIConstant.EVENT_CONNECTED_TO_SERVER:// 连接服务器成功
                KLog.d(TAG, "与AIUI服务器连接");
                startAiuiService();
                break;
            case AIUIConstant.EVENT_SERVER_DISCONNECTED:// 与服务器断连
                KLog.d(TAG, "与AIUI服务器断开");
                break;
            case AIUIConstant.EVENT_TTS:
                analysisTts(event);
                break;
            case AIUIConstant.EVENT_RESULT:
                analysisResult(event);
                break;
            case AIUIConstant.EVENT_STATE:
                mCurrentState = event.arg1;
                break;
            case AIUIConstant.EVENT_SLEEP:
                isRecording = false;
                break;
            case AIUIConstant.EVENT_VAD:
                // 当检测到输入音频的前端点后，会抛出该事件
                // 用arg1标识前后端点或者音量信息:0(前端点)、1(音量)、2(后端点)、3（前端点超时）。
                // 当arg1取值为1时，arg2为音量大小。
                if (event.arg1 == AIUIConstant.VAD_VOL) {
                    if (iAsrCallBack != null) {
                        mHandler.post(() -> iAsrCallBack.onVolumeChanged(event.arg2));
                    }
                } else {
                    KLog.d(TAG, "AIUIConstant.VAD:" + event.arg1);
                }
                break;
            case AIUIConstant.EVENT_ERROR:
                KLog.d(TAG, "AiuiError:info-" + event.info + ",data-" + event.data + ",arg1-" + event.arg1 + ",arg2-" + event.arg2);
                ToastUtils.showLong("aiui error "+event.arg1);
                break;
        }
    };

    /**
     * 注销CAE引擎
     */
    public void releaseCaeEngine() {
        // 注销CAE
        mCaeEngineMgr.stopWork();
        // 关闭录音
        AIUISDK.getInstance().stopRecognize();
    }

    /**
     * 初始化CAE引擎
     */
    public void initCaeEngine(String caeSN) {
        // 初始化降噪算法
//        mCaeEngineMgr.init(mContext, new HlwNearEngine(),
//                new BothlentRecord(new PcmDevice(ShellUtil.fetchCard())), new BothlentAdapter());
        UsbMicRecorder usbMicRecorder = new UsbMicRecorder();
        usbMicRecorder.setUsbMicRecorderCallBack(new UsbMicRecorderCallBack() {
            @Override
            public void onAudioDataChanged(byte[] data, int len) {
                mAiuiAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, mAudioParams, data));
            }
        });
        mCaeEngineMgr.init(mContext, new HlwNearEngine(), usbMicRecorder, new UsbMicAdapter());

        mCaeEngineMgr.checkEngine(caeSN);
        mCaeEngineMgr.startWork(new IEngineCallBack() {
            @Override
            public void onWakeup(WakeInfo info) {
                int angle = info.getAngle();
                int beam = info.getBeam();
                if (iWakeUpCallBack != null) {
                    mHandler.post(() -> iWakeUpCallBack.onWakeUp(angle, beam));
                }
                wakeupAiuiService(angle, beam);
            }

            @Override
            public void onAudioCallback(byte[] bytes, int len) {
                if (isRecording && !NContext.isUseExtMic) {
                    mTtsFileUtils.writeByMessageId("onAudioCallback",bytes, 0, len);
                    mAiuiAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, mAudioParams, bytes));
                }
            }
        });
        mCaeEngineMgr.checkStartRecord();
    }

    // --------------------------------AIUI服务----------------------------------

    private void startAiuiService() {
        // 清除云端交互历史
        KLog.d(TAG, "startAiuiService"+"清除云端交互历史");
        clearDialogHistory();
        sendMessage(new AIUIMessage(AIUIConstant.CMD_START, 0, 0, "", null));
    }

    /**
     * 唤醒AIUI服务
     */
    public void wakeupAiuiService(int angle, int beam) {
        mAiuiAgent.sendMessage(new AIUIMessage(AIUIConstant.CMD_WAKEUP, angle, beam, "", null));
    }

    /**
     * 清除AIUI云端交互历史
     */
    public void clearDialogHistory() {
        // 需要在CONNECT_TO_SERVER后才能发送清除历史指令，清除云端的交互历史
        KLog.d(TAG, "clearDialogHistory"+"清除AIUI云端交互历史");
        sendMessage(new AIUIMessage(AIUIConstant.CMD_CLEAN_DIALOG_HISTORY, 0, 0, null, null));
    }

    /**
     * 重置会话ID
     */
    public void resetSessionId() {
        String params = "{\"userparams\":{\"stream\":true,\"robUuid\":\"" + NContext.ROBOT_ID + "\",\"sessionId\":\"" + ByteUtils.getShortUuid() + "\"}}";
        sendMessage(new AIUIMessage(AIUIConstant.CMD_SET_PARAMS, 0, 0, params, null));
    }

    // --------------------------------语音合成----------------------------------

    public void setTtsName(String name) {
        if (TextUtils.isEmpty(name)) {
            mTtsName = "x2_xiaojuan";
        } else {
            mTtsName = name;
        }
    }

    public void setTtsSpeed(String speed) {
        if (TextUtils.isEmpty(speed)) {
            mTtsSpeed = "50";
        } else {
            mTtsSpeed = speed;
        }
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void stopSpeaking() {
        PcmAudioPlayer.getInstance().stopPlayAudio(false);
        isSpeaking = false;
    }

    @SuppressLint("CheckResult")
    public void startSpeaking(String content, ITTSCallBack callBack) {
        this.mTtsContent = content;
        this.iTtsCallBack = callBack;
        stopSpeaking();
        Observable.timer(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    PcmAudioPlayer.getInstance().startPlayAudio();
                    PcmAudioPlayer.getInstance().setPlayListener(mAudioPlayListener);
                    onSpeaking(mTtsContent);
                });
    }

    private void onSpeaking(String content) {
        KLog.d(TAG, "startSpeaking");
        StringBuffer params = new StringBuffer();
        params.append("vcn=" + mTtsName);  //发音人
        params.append(",speed=" + mTtsSpeed);  //语速
        params.append(",pitch=" + mTtsPitch);  //音调
        params.append(",volume=" + mTtsVolume);  //音量
        params.append(",ent=aisound");
        KLog.d(TAG, "onSpeaking：" + content);
        sendMessage(new AIUIMessage(AIUIConstant.CMD_TTS, AIUIConstant.START, 0, params.toString(), content.getBytes()));
    }

    private void analysisTts(AIUIEvent event) {
        switch (event.arg1) {
            case AIUIConstant.TTS_SPEAK_BEGIN:
                KLog.d(TAG, "AIUIEvent:TTS_SPEAK_BEGIN");
                isSpeaking = true;
                if (iTtsCallBack != null) {
                    mHandler.post(() -> iTtsCallBack.onSpeakBegin());
                }
                break;
            case AIUIConstant.TTS_SPEAK_COMPLETED:
                KLog.d(TAG, "AIUIEvent:TTS_SPEAK_COMPLETED");
                isSpeaking = false;
                if (iTtsCallBack != null) {
                    mHandler.post(() -> iTtsCallBack.onCompleted());
                }
                break;
            case AIUIConstant.TTS_SPEAK_PAUSED:
                KLog.d(TAG, "AIUIEvent:TTS_SPEAK_PAUSED");
                if (iTtsCallBack != null) {
                    mHandler.post(() -> iTtsCallBack.onPause(false));
                }
                break;
            case AIUIConstant.TTS_SPEAK_RESUMED:
                KLog.d(TAG, "AIUIEvent:TTS_SPEAK_RESUMED");
                if (iTtsCallBack != null) {
                    mHandler.post(() -> iTtsCallBack.onResume(false));
                }
                break;
            case AIUIConstant.TTS_SPEAK_PROGRESS:
                if (iTtsCallBack != null) {
                    android.os.Bundle data = event.data;
                    if (data != null) {
                        mHandler.post(() -> iTtsCallBack.onProgress(data.getInt("percent")));
                    }
                }
                break;
        }
    }

    private final AudioPlayListener mAudioPlayListener = new AudioPlayListener() {
        @Override
        public void onPlaybackBegin() {
            if (iTtsCallBack != null) {
                mHandler.post(() -> iTtsCallBack.onSpeakBegin());
            }
        }

        @Override
        public void onPlaybackCompleted() {
            isSpeaking = false;
            if (iTtsCallBack != null) {
                mHandler.post(() -> iTtsCallBack.onCompleted());
            }
        }

        @Override
        public void onPlaybackError(String error) {
            isSpeaking = false;
            if (iTtsCallBack != null) {
                mHandler.post(() -> iTtsCallBack.onError(error));
            }
        }
    };

    // --------------------------------语音识别----------------------------------

    /**
     * 开始语音识别
     */
    public void startRecognize() {
        KLog.d(TAG, "startRecognize");
        if (!isRecording) {
            String params = "data_type=audio,sample_rate=16000,dwa=wpgs";
            sendMessage(new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null));
        }
        isRecording = true;
        if (iAsrCallBack != null) {
            mHandler.post(() -> iAsrCallBack.onBeginOfSpeech());
        }
        mCaeEngineMgr.getBaseEngine().setBeam(1);
    }

    /**
     * 结束语音识别
     */
    public void stopRecognize() {
        KLog.d(TAG, "stopRecognize");
        if (isRecording) {
            String params = "data_type=audio,sample_rate=16000";
            sendMessage(new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null));
        }
        isRecording = false;
    }

    /**
     * 是否在录音状态
     *
     * @return 是否在录音状态
     */
    public boolean isListening() {
        return isRecording;
    }

    /**
     * 设置语音识别回调
     *
     * @param callBack 语音识别状态和结果回调
     */
    public void setAsrCallBack(IASRCallBack callBack) {
        this.iAsrCallBack = callBack;
    }

    /**
     * 设置语音唤醒回调
     *
     * @param callBack 语音唤醒结果回调
     */
    public void setWakeUpCallBack(IWakeUpCallBack callBack) {
        this.iWakeUpCallBack = callBack;
    }

    /**
     * 停止接收语音唤醒
     */
    public void stopWakeUpCallBack() {
        this.iWakeUpCallBack = null;
    }

    /**
     * 设置经纬度
     * (询问位置有关问题时使用,例如询问天气信息,会回复当前经纬度的天气)
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public void setLngLat(String lng, String lat) {
        if (TextUtils.isEmpty(lng) || TextUtils.isEmpty(lat)) {
            mAddedParams.remove("msc.lng");
            mAddedParams.remove("msc.lat");
        } else {
            mAddedParams.put("msc.lng", lng);
            mAddedParams.put("msc.lat", lat);
        }
        // 格式化参数
        formatAiuiParams();
    }

    /**
     * 设置用户级热词
     *
     * @param words 热词列表
     */
    public void setHotWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            mAddedParams.remove("rec_user_data");
        } else {
            try {
                String join = String.join("|", words);
                JSONObject object = new JSONObject();
                object.put("recHotWords", join);
                object.put("sceneInfo", new JSONObject());
                mAddedParams.put("rec_user_data", object);
            } catch (Exception e) {
                KLog.d("添加用户级热词失败:" + e.getMessage());
            }
        }
        // 格式化参数
        formatAiuiParams();
    }

    /**
     * 处理结果事件（听写结果和语义结果）
     */
    private void analysisResult(AIUIEvent event) {
        String info = event.info;
        if (TextUtils.isEmpty(info)) {
            return;
        }
        try {
            JSONObject infoJson = new JSONObject(info);
            JSONObject data = infoJson.getJSONArray("data").getJSONObject(0);
            String sub = data.getJSONObject("params").optString("sub");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);
            String cntId = content.optString("cnt_id");
            byte[] cntData = event.data.getByteArray(cntId);
            if (cntData != null) {
                if (sub.equals("tts")) {
                    // dts：音频块进度信息，取值：0音频开始,1音频中间块可出现多次,2音频结束,3独立音频合成短文本时出现
                    int dts = content.getInt("dts");
                    analysisResultTts(cntData, dts);
                } else {
                    String cntString = new String(cntData, StandardCharsets.UTF_8);
                    switch (sub) {
                        case "iat":
                            analysisResultAsr(cntString);
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            KLog.d(TAG, "analysisResult:Error=" + e + ",Info=" + info);
        }
    }

    /**
     * 语音合成结果
     */
    private void analysisResultTts(byte[] datas, int dts) {
        // dts：音频块进度信息，取值：0音频开始,1音频中间块可出现多次,2音频结束,3独立音频合成短文本时出现
        isSpeaking = true;
        PcmAudioPlayer.getInstance().playAudioData(datas, dts);
        // 是否保存合成音频
        if (mTtsFileUtils != null) {
            if (dts == 0 || dts == 3) {
                mTtsFileUtils.createPcmFile();
            }
            mTtsFileUtils.write(datas, 0, datas.length);
        }
    }

    /**
     * 处理听写结果
     */
    private void analysisResultAsr(String string) throws JSONException {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        KLog.d(TAG, "analysisResultAsr:" + string);
        JSONObject asrJson = new JSONObject(string);
        JSONObject text = asrJson.optJSONObject("text");
        // 解析拼接此次听写结果
        StringBuilder asrVoice = new StringBuilder();
        JSONArray words = text.optJSONArray("ws");
        boolean lastResult = text.optBoolean("ls");
        for (int i = 0; i < words.length(); i++) {
            JSONArray word = words.optJSONObject(i).optJSONArray("cw");
            for (int j = 0; j < word.length(); j++) {
                asrVoice.append(word.optJSONObject(j).opt("w"));
            }
        }
        StringBuffer asrResult = new StringBuffer();
        String pgsMode = text.optString("pgs");
        // 非PGS模式结果
        if (TextUtils.isEmpty(pgsMode)) {
            if (TextUtils.isEmpty(asrVoice)) {
                return;
            }
            asrResult.append(asrVoice);
        } else {
            mAsrPgsStack[text.optInt("sn")] = asrVoice.toString();
            // pgs结果两种模式rpl和apd模式（替换和追加模式）
            if (pgsMode.equals("rpl")) {
                // 根据replace指定的range，清空stack中对应位置值
                JSONArray range = text.optJSONArray("rg");
                int start = range.optInt(0);
                int end = range.optInt(1);
                for (int index = start; index <= end; index++) {
                    mAsrPgsStack[index] = null;
                }
            }
            StringBuilder pgsResult = new StringBuilder();
            // 汇总stack经过操作后的剩余的有效结果信息
            for (int index = 0; index < mAsrPgsStack.length; index++) {
                if (TextUtils.isEmpty(mAsrPgsStack[index])) {
                    continue;
                }
                pgsResult.append(mAsrPgsStack[index]);
                // 如果是最后一条听写结果，则清空stack便于下次使用
                if (lastResult) {
                    mAsrPgsStack[index] = null;
                }
            }
            asrResult.append(pgsResult);
        }
        if (iAsrCallBack != null) {
            if (!lastResult) {
                mHandler.post(() -> iAsrCallBack.dynamicResult(asrResult.toString()));
            } else {
                mHandler.post(() -> iAsrCallBack.onEndOfSpeech(asrResult.toString(), ""));
            }
        }
    }
}

