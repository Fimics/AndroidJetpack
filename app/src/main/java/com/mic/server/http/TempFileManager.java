package com.mic.server.http;

public interface TempFileManager {

        void clear();

        public TempFile createTempFile(String filename_hint) throws Exception;
    }