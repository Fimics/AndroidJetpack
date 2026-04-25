package com.mic.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class SpeechDataEvent implements LiveEvent {

    public String content;
    public String filtered;
    public String sid;
    public boolean ls;


    public SpeechDataEvent(String content, boolean ls,String sid) {
        this.content = content;
        this.ls = ls;
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "SpeechDataEvent{" +
                "content='" + content + '\'' +
                ", filtered='" + filtered + '\'' +
                ", sid='" + sid + '\'' +
                ", ls=" + ls +
                '}';
    }
}
