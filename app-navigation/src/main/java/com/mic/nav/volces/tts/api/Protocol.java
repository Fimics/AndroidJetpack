package com.mic.nav.volces.tts.api;

public class Protocol {

    public static final int PROTOCOL_VERSION = 0b0001;
    public static final int DEFAULT_HEADER_SIZE = 0b0001;

    // Message Type:
    public static final int FULL_CLIENT_REQUEST = 0b0001;
    public static final int AUDIO_ONLY_RESPONSE = 0b1011;
    public static final int FULL_SERVER_RESPONSE = 0b1001;
    public static final int ERROR_INFORMATION = 0b1111;

    // Message Type Specific Flags
    public static final int MsgTypeFlagNoSeq = 0b0000; // Non-terminal packet with no sequence
    public static final int MsgTypeFlagPositiveSeq = 0b1;// Non-terminal packet with sequence > 0
    public static final int MsgTypeFlagLastNoSeq = 0b10;// last packet with no sequence
    public static final int MsgTypeFlagNegativeSeq = 0b11; // Payload contains event number (int32)
    public static final int MsgTypeFlagWithEvent = 0b100;
    // Message Serialization
    public static final int NO_SERIALIZATION = 0b0000;
    public static final int JSON = 0b0001;
    // Message Compression
    public static final int COMPRESSION_NO = 0b0000;
    public static final int COMPRESSION_GZIP = 0b0001;
}
