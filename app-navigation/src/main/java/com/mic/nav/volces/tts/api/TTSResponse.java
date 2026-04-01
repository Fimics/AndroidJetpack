package com.mic.nav.volces.tts.api;

import com.google.gson.Gson;

public class TTSResponse {

        public Header header;
        public Optional optional;
        public int payloadSize;
        transient public byte[] payload;

        public String payloadJson;

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }