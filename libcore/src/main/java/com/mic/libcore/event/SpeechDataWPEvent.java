package com.mic.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class SpeechDataWPEvent implements LiveEvent {

    public String content;
    public String eventType;
    public String filtered;
    public boolean ls;
    public String uuid;
    public static final String EVENT_ASR = "event_asr";
    public static final String EVENT_ESR = "event_esr";
    public String sid;

    public SpeechDataWPEvent(String content, String eventType, boolean ls, String filtered, String uuid, String sid) {
        this.content = content;
        this.eventType = eventType;
        this.ls = ls;
        this.uuid = uuid;
        this.filtered = filtered;
        this.sid=sid;
    }
}
