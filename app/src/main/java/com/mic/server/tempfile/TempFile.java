package com.mic.server.tempfile;

import java.io.OutputStream;

public interface TempFile {

        public void delete() throws Exception;

        public String getName();

        public OutputStream open() throws Exception;
    }