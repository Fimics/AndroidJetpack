package com.mic.server.http;

public class DefaultTempFileManagerFactory implements TempFileManagerFactory {

        @Override
        public TempFileManager create() {
            return new DefaultTempFileManager();
        }
    }