package com.mic.server.tempfile;


public interface TempFileManager {

        void clear();

        public TempFile createTempFile(String filename_hint) throws Exception;
    }