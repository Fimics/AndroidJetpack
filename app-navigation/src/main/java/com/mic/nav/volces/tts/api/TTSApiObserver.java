package com.mic.nav.volces.tts.api;

import androidx.lifecycle.Observer;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.noetix.libcore.event.SpeechDataEvent;

public class TTSApiObserver {

    private TTSWebsocket ttsWebsocket;

    public void observer() {
        observerSpeechDataEvent();
    }

    private void observerSpeechDataEvent() {
        LiveEventBus.get(SpeechDataEvent.class).observeForever(new Observer<>() {
            @Override
            public void onChanged(SpeechDataEvent speechDataEvent) {
                boolean ls = speechDataEvent.ls;
                String sid = speechDataEvent.sid;
                String content = speechDataEvent.content;

                if (ls) {
                    if (ttsWebsocket != null) {
                        ttsWebsocket.disconnect();
                        ttsWebsocket = null;
                    }
                    ttsWebsocket = new TTSWebsocket();
                    ttsWebsocket.connect();
                }
            }
        });
    }

    public void addText(String text, int seq, int status) {
        if (ttsWebsocket != null) {
            ttsWebsocket.addText(text, seq, status);
        }
    }
}
