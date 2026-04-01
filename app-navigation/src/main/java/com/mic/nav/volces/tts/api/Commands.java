package com.mic.nav.volces.tts.api;

import com.google.gson.JsonObject;

import okhttp3.WebSocket;
import okio.ByteString;

public class Commands {

    public boolean startConnection(WebSocket webSocket) {
        byte[] optional = new Optional(Event.EVENT_Start_Connection, null).getBytes();
        byte[] payload = "{}".getBytes();
        return sendEvent(webSocket, headerBytes(), optional, payload);
    }

    public boolean finishConnection(WebSocket webSocket) {
        byte[] optional = new Optional(Event.EVENT_FinishConnection, null).getBytes();
        byte[] payload = "{}".getBytes();
        return sendEvent(webSocket, headerBytes(), optional, payload);

    }

    public boolean finishSession(WebSocket webSocket, String sessionId) {
        byte[] optional = new Optional(Event.EVENT_FinishSession, sessionId).getBytes();
        byte[] payload = "{}".getBytes();
        return sendEvent(webSocket, headerBytes(), optional, payload);
    }

    public boolean startTTSSession(WebSocket webSocket, String sessionId) {

        final int event = Event.EVENT_StartSession;
        byte[] optional = new Optional(event, sessionId).getBytes();
        JsonObject payloadJObj = new JsonObject();
        JsonObject user = new JsonObject();
        user.addProperty("uid", "123456");
        payloadJObj.add("user", user);

        payloadJObj.addProperty("event", event);
        payloadJObj.addProperty("namespace", "BidirectionalTTS");

        JsonObject req_params = new JsonObject();
        req_params.addProperty("speaker", Config.SPEAKER);

        JsonObject audio_params = new JsonObject();
        audio_params.addProperty("format", "pcm");
        audio_params.addProperty("sample_rate", 16000);
        audio_params.addProperty("enable_timestamp", true);

        req_params.add("audio_params", audio_params);
        payloadJObj.add("req_params", req_params);
        byte[] payload = payloadJObj.toString().getBytes();
        return sendEvent(webSocket, headerBytes(), optional, payload);
    }

    /**
     * 分段合成音频
     * @param webSocket
     * @param speaker
     * @param sessionId
     * @param text
     * @return
     */
    public boolean sendMessage(WebSocket webSocket, String sessionId, String text) {

        final int event = Event.EVENT_TaskRequest;
        byte[] optional = new Optional(event, sessionId).getBytes();

        JsonObject payloadJObj = new JsonObject();
        JsonObject user = new JsonObject();
        user.addProperty("uid", "123456");
        payloadJObj.add("user", user);

        payloadJObj.addProperty("event", event);
        payloadJObj.addProperty("namespace", "BidirectionalTTS");

        JsonObject req_params = new JsonObject();
        req_params.addProperty("text", text);
        req_params.addProperty("speaker", Config.SPEAKER);

        JsonObject audio_params = new JsonObject();
        audio_params.addProperty("format", "pcm");
        audio_params.addProperty("sample_rate", 16000);

        req_params.add("audio_params", audio_params);
        payloadJObj.add("req_params", req_params);
        byte[] payload = payloadJObj.toString().getBytes();
        return sendEvent(webSocket, headerBytes(), optional, payload);
    }

    private byte [] headerBytes(){
        byte[] header = new Header(
                Protocol.PROTOCOL_VERSION,
                Protocol.FULL_CLIENT_REQUEST,
                Protocol.DEFAULT_HEADER_SIZE,
                Protocol.MsgTypeFlagWithEvent,
                Protocol.JSON,
                Protocol.COMPRESSION_NO,
                0).getBytes();
        return header;
    }

    public boolean sendEvent(WebSocket webSocket, byte[] header, byte[] optional, byte[] payload) {
        assert webSocket != null;
        assert header != null;
        assert payload != null;
        final byte[] payloadSizeBytes = ByteUtils.intToBytes(payload.length);
        byte[] requestBytes = new byte[
                header.length
                        + (optional == null ? 0 : optional.length)
                        + payloadSizeBytes.length + payload.length];
        int desPos = 0;
        System.arraycopy(header, 0, requestBytes, desPos, header.length);
        desPos += header.length;
        if (optional != null) {
            System.arraycopy(optional, 0, requestBytes, desPos, optional.length);
            desPos += optional.length;
        }
        System.arraycopy(payloadSizeBytes, 0, requestBytes, desPos, payloadSizeBytes.length);
        desPos += payloadSizeBytes.length;
        System.arraycopy(payload, 0, requestBytes, desPos, payload.length);
        return webSocket.send(ByteString.of(requestBytes));
    }
}
