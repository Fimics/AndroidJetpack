package com.mic.server.tempfile;

public class DefaultTempFileManagerFactory implements TempFileManagerFactory {

        @Override
        public TempFileManager create() {
            return new DefaultTempFileManager();
        }
    }