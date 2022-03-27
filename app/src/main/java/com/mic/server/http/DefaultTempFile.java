package com.mic.server.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DefaultTempFile implements TempFile {

        private final File file;

        private final OutputStream fstream;

        public DefaultTempFile(File tempdir) throws IOException {
            this.file = File.createTempFile("NanoHTTPD-", "", tempdir);
            this.fstream = new FileOutputStream(this.file);
        }

        @Override
        public void delete() throws Exception {
            Utils.safeClose(this.fstream);
            if (!this.file.delete()) {
                throw new Exception("could not delete temporary file");
            }
        }

        @Override
        public String getName() {
            return this.file.getAbsolutePath();
        }

        @Override
        public OutputStream open() throws Exception {
            return this.fstream;
        }
    }