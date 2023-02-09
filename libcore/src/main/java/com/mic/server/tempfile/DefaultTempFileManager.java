package com.mic.server.tempfile;

import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DefaultTempFileManager implements TempFileManager {
        private static final String TAG = "server";
        private final File tmpdir;

        private final List<TempFile> tempFiles;

        public DefaultTempFileManager() {
            this.tmpdir = new File(System.getProperty("java.io.tmpdir"));
            if (!tmpdir.exists()) {
                tmpdir.mkdirs();
            }
            this.tempFiles = new ArrayList<TempFile>();
        }

        @Override
        public void clear() {
            for (TempFile file : this.tempFiles) {
                try {
                    file.delete();
                } catch (Exception e) {
                    Log.d(TAG, "could not delete file "+e.getMessage());
                }
            }
            this.tempFiles.clear();
        }

        @Override
        public TempFile createTempFile(String filename_hint) throws Exception {
            DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
            this.tempFiles.add(tempFile);
            return tempFile;
        }
    }