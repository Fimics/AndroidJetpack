package com.noetix.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class PreWakeUpEvent implements LiveEvent {

    public boolean preWakeUp;

    public PreWakeUpEvent(boolean preWakeUp) {
        this.preWakeUp = preWakeUp;
    }
}
