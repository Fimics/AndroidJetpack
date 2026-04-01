package com.noetix.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class SpeechStatusEvent implements LiveEvent {

    public int status;
    public String sid="";
    public static final int wakeup = 1;
    public static final int vad_bos = 2;
    public static final int vad_eos = 3;
    public static final int vad_timeout = 4;
    public static final int vad_ls = 5;


//    ADD BY AK
    public static final int mic_on = 10;
    public static final int mic_off = 20;

    public SpeechStatusEvent(int status) {
        this.status = status;
    }

    public SpeechStatusEvent(int status, String sid) {
        this.status = status;
        this.sid = sid;
    }
}
