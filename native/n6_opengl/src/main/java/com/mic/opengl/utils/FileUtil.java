package com.mic.opengl.utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// 就是把 assets目录里面的文件 Copy 到 SDCard目录下 的工具类
public class FileUtil {
    public static void copyAssets2SDCard(Context context, String src, String dst) {
        try {
            File file = new File(dst);
            if (!file.exists()) {
                InputStream is = context.getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
