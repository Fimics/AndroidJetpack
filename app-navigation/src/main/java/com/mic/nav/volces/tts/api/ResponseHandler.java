package com.mic.nav.volces.tts.api;

public class ResponseHandler {

    public TTSResponse parserResponse(byte[] res) {
        if (res == null || res.length == 0) {
            return null;
        }
        final TTSResponse response = new TTSResponse();
        Header header = new Header();
        response.header = header;

        // 当符号位为1时进行 >> 运算后高位补1（预期是补0），导致结果错误，所以增加个数再与其& 运算，目的是确保高位是补0.
        final byte num = 0b00001111;
        // header 32 bit=4 byte
        header.protocol_version = (res[0] >> 4) & num;
        header.header_size = res[0] & 0x0f;
        header.message_type = (res[1] >> 4) & num;
        header.message_type_specific_flags = res[1] & 0x0f;
        header.serialization_method = res[2] >> num;
        header.message_compression = res[2] & 0x0f;
        header.reserved = res[3];

        int offset = 4;
        response.optional = new Optional();
        // 正常Response
        if (header.message_type == Protocol.FULL_SERVER_RESPONSE || header.message_type == Protocol.AUDIO_ONLY_RESPONSE) {
            // 如果有event
            offset = readEvent(res, header.message_type_specific_flags, response, offset);
            final int event = response.optional.event;
            // 根据 event 类型解析
            switch (event) {
                case Event.EVENT_ConnectionStarted:
                    readConnectStarted(res, response, offset);
                    break;
                case Event.EVENT_ConnectionFailed:
                    readConnectFailed(res, response, offset);
                    break;
                case Event.EVENT_SessionStarted:
                    offset = readSessionId(res, response, offset);
                    break;
                case Event. EVENT_TTSResponse:
                    offset = readSessionId(res, response, offset);
                    offset = readPayload(res, response, offset);
                    break;
                case Event.EVENT_TTSSentenceStart:
                case Event.EVENT_TTSSentenceEnd:
                    offset = readSessionId(res, response, offset);
                    offset = readPayloadJson(res, response, offset);
                    break;

                case Event.EVENT_SessionFailed:
                case Event.EVENT_SessionFinished:
                    offset = readSessionId(res, response, offset);
                    readMetaJson(res, response, offset);
                    break;
                default:
                    break;


            }
        }
        // 错误
        else if (header.message_type == Protocol.ERROR_INFORMATION) {
            offset = readErrorCode(res, response, offset);
            readPayload(res, response, offset);
        }
        return response;
    }

    private void readConnectStarted(byte[] res, TTSResponse response, int start) {
        // 8--11: connection id size
        start = readConnectId(res, response, start);
    }

    private void readConnectFailed(byte[] res, TTSResponse response, int start) {
        // 8--11: connection id len
        start = readConnectId(res, response, start);

        readMetaJson(res, response, start);
    }

    private int readConnectId(byte[] res, TTSResponse response, int start) {
        Pair<Integer, String> r = readText(res, start);
        start = r.fst;
        response.optional.connectionId = r.snd;
        return start;
    }


    private int readMetaJson(byte[] res, TTSResponse response, int start) {
        Pair<Integer, String> r = readText(res, start);
        start = r.fst;
        response.optional.response_meta_json = r.snd;
        return start;
    }

    private int readPayloadJson(byte[] res, TTSResponse response, int start) {
        Pair<Integer, String> r = readText(res, start);
        start = r.fst;
        response.payloadJson = r.snd;
        return start;
    }

    private Pair<Integer, String> readText(byte[] res, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        int size = ByteUtils.bytesToInt(b);
        b = new byte[size];
        System.arraycopy(res, start, b, 0, b.length);
        String text = new String(b);
        start += b.length;
        return new Pair<>(start, text);
    }

    private int readPayload(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        int size = ByteUtils.bytesToInt(b);
        response.payloadSize += size;
        b = new byte[size];
        System.arraycopy(res, start, b, 0, b.length);
        response.payload = b;
        start += b.length;
        if (response.optional.event == Protocol.FULL_SERVER_RESPONSE) {
            System.out.println("===> payload:" + new String(b));
        }
        return start;
    }

    private int readErrorCode(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        response.optional.errorCode = ByteUtils.bytesToInt(b);
        return start;
    }


    private int readEvent(byte[] res, int masTypeFlag, TTSResponse response, int start) {
        if (masTypeFlag == Protocol.MsgTypeFlagWithEvent) {
            byte[] temp = new byte[4];
            System.arraycopy(res, start, temp, 0, temp.length);
            response.optional.event = ByteUtils.bytesToInt(temp);
            start += temp.length;
        }
        return start;
    }


    private int readSessionId(byte[] res, TTSResponse response, int start) {
        Pair<Integer, String> r = readText(res, start);
        start = r.fst;
        response.optional.sessionId = r.snd;
        return start;
    }
}
