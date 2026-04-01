package com.mic.nav.volces.tts.api;

public class Optional {

        public int event = Event.EVENT_NONE;
        public String sessionId;

        public int errorCode;
        public String connectionId;

        public String response_meta_json;

        public Optional(int event, String sessionId) {
            this.event = event;
            this.sessionId = sessionId;
        }

        public Optional() {
        }

        public byte[] getBytes() {
            byte[] bytes = new byte[0];
            if (event != Event.EVENT_NONE) {
                bytes = ByteUtils.intToBytes(event);
            }
            if (sessionId != null) {
                byte[] sessionIdSize = ByteUtils.intToBytes(sessionId.getBytes().length);
                final byte[] temp = bytes;
                int desPos = 0;
                bytes = new byte[temp.length + sessionIdSize.length + sessionId.getBytes().length];
                System.arraycopy(temp, 0, bytes, desPos, temp.length);
                desPos += temp.length;
                System.arraycopy(sessionIdSize, 0, bytes, desPos, sessionIdSize.length);
                desPos += sessionIdSize.length;
                System.arraycopy(sessionId.getBytes(), 0, bytes, desPos, sessionId.getBytes().length);

            }
            return bytes;
        }
    }