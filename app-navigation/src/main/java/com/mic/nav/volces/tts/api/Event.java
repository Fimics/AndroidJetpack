package com.mic.nav.volces.tts.api;

public class Event {
    // event
    // 默认事件,对于使用事件的方案，可以通过非0值来校验事件的合法性
    public static final int EVENT_NONE = 0;

    public static final int EVENT_Start_Connection = 1;


    // 上行Connection事件
    public static final int EVENT_FinishConnection = 2;

    // 下行Connection事件
    public static final int EVENT_ConnectionStarted = 50; // 成功建连

    public static final int EVENT_ConnectionFailed = 51; // 建连失败（可能是无法通过权限认证）

    public static final int EVENT_ConnectionFinished = 52; // 连接结束

    // 上行Session事件
    public static final int EVENT_StartSession = 100;

    public static final int EVENT_FinishSession = 102;

    // 下行Session事件
    public static final int EVENT_SessionStarted = 150;

    public static final int EVENT_SessionFinished = 152;

    public static final int EVENT_SessionFailed = 153;

    // 上行通用事件
    public static final int EVENT_TaskRequest = 200;

    // 下行TTS事件
    public static final int EVENT_TTSSentenceStart = 350;

    public static final int EVENT_TTSSentenceEnd = 351;

    public static final int EVENT_TTSResponse = 352;

}
