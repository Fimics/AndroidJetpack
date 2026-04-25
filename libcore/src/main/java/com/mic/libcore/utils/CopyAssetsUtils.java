package com.mic.libcore.utils;

import android.content.Context;

import com.mic.libcore.utils.KLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyAssetsUtils {
    // 资源文件拷贝
    public static void portingFile(Context context, String path) {
//        copyAssetFolder(context, "resources", String.format("%s/vtn/cae/resources", path));
        copyAssetFolder(context, "vms", path);
    }

    public static void copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            String[] fileList = context.getAssets().list(srcName);
            if (fileList == null) {
                return;
            }
            if (fileList.length == 0) {
                copyAssetFile(context, srcName, dstName);
            } else {
                File file = new File(dstName);
                file.mkdirs();
                for (String filename : fileList) {
                    copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyAssetFile(Context context, String srcName, String dstName) {
        try {
            InputStream in = context.getAssets().open(srcName);
            File outFile = new File(dstName);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
