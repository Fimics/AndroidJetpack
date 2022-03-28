package com.mic.castserver;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class FileUtils {

    public static @Nullable
    String getExtension(@NonNull String path) {

        if(TextUtils.isEmpty(path)) return null;

        int lastSeparator = -1, lastDot = -1;

        for (int i = path.length() - 1; i >= 0; --i) {
            char ch = path.charAt(i);
            if (ch == '.') {
                if (lastDot < 0) {
                    lastDot = i;
                    if (lastSeparator >= 0)
                        break;
                }
            } else if (ch == File.separatorChar) {
                if (lastSeparator < 0) {
                    lastSeparator = i;
                    if (lastDot >= 0)
                        break;
                }
            }
        }

        if (lastSeparator < lastDot)
            return path.substring(lastDot + 1);

        return null;
    }
}
