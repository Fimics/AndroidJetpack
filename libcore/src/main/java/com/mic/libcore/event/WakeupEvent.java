package com.mic.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class WakeupEvent implements LiveEvent {

    public String score;
    public String angle ;
    public String keyWord;

    public WakeupEvent(String score, String angle, String keyWord) {
        this.score = score;
        this.angle = angle;
        this.keyWord = keyWord;
    }
}
