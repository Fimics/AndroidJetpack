package com.noetix.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class AnswerEvent implements LiveEvent {
    public String text;
    public String sid;
    public int seq;
    public int status;
    public String from;

    public AnswerEvent(String text, String sid,int seq,int status,String from) {
        this.text = text;
        this.sid = sid;
        this.seq = seq;
        this.status=status;
        this.from=from;
    }

    @Override
    public String toString() {
        return "AnswerEvent{" +
                "text='" + text + '\'' +
                ", sid='" + sid + '\'' +
                ", seq=" + seq +
                ", status=" + status +
                ", from='" + from + '\'' +
                '}';
    }
}
