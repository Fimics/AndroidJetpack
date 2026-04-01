package com.mic.nav.volces.tts.sdk;

public class TTSEngineProxy {
    private final TTSEngine ttsEngine;

    public TTSEngineProxy() {
        this.ttsEngine = new TTSEngine();
    }

    public void init(){
        this.ttsEngine.init();
    }

    public void unInit(){
        this.unInit();
    }

    public void startEngine(){
        this.ttsEngine.startEngine();
    }


    public void stopEngine(){
        this.ttsEngine.stopEngine();
    }

    public void triggerSynthesis(int seq,int status,String text){
        this.ttsEngine.triggerSynthesis(seq, status,text);
    }
}
