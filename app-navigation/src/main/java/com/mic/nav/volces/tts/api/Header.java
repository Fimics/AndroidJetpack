package com.mic.nav.volces.tts.api;

public class Header {

        public int protocol_version = Protocol.PROTOCOL_VERSION;
        public int header_size = Protocol.DEFAULT_HEADER_SIZE;
        public int message_type;
        public int message_type_specific_flags = Protocol.MsgTypeFlagWithEvent;
        public int serialization_method = Protocol.NO_SERIALIZATION;
        public int message_compression = Protocol.COMPRESSION_NO;
        public int reserved = 0;

        public Header() {
        }

        public Header(int protocol_version, int header_size, int message_type, int message_type_specific_flags,
                int serialization_method, int message_compression, int reserved) {
            this.protocol_version = protocol_version;
            this.header_size = header_size;
            this.message_type = message_type;
            this.message_type_specific_flags = message_type_specific_flags;
            this.serialization_method = serialization_method;
            this.message_compression = message_compression;
            this.reserved = reserved;
        }

        /**
         * 转成 byte 数组
         *
         * @return
         */
        public byte[] getBytes() {
            return new byte[]{
                    // Protocol version | Header size (4x)
                    (byte) ((protocol_version << 4) | header_size),
                    // Message type | Message type specific flags
                    (byte) (message_type << 4 | message_type_specific_flags),
                    // Serialization method | Compression method
                    (byte) ((serialization_method << 4) | message_compression),
                    (byte) reserved
            };
        }
    }