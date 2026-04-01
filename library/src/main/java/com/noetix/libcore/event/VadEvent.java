package com.noetix.libcore.event;

import com.jeremyliao.liveeventbus.core.LiveEvent;

public class VadEvent implements LiveEvent {

    public int status;
    public static final int vad_bos = 2;
    public static final int vad_eos = 3;
    public static final int vad_timeout = 4;

    public VadEvent(int status) {
        this.status = status;
    }
}
