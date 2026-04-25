package com.mic.libcore.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    private final static String PCM_SURFFIX = ".pcm";
    private final String WRITE_PCM_DIR;
    private FileOutputStream mFos;

    private FileInputStream mFis;

    public FileUtil(String writeDir) {
        WRITE_PCM_DIR = writeDir;
    }

    /**
     * 读取AIUI配置
     */
    public static String getAIUIParams(Context context, String cfgFilePath) {
        String params = "";
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open(cfgFilePath);
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 申请权限
     */
    public static void requestPermissions(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.INTERNET}, 0x0010);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 便捷但性能差，而且线程不安全，只用于调试，默认追加模式
     *
     * @param data     待保存数据
     * @param filePath 存储文件的绝对路径，例如 /sdcard/aiui/16k16bit-1ch1mic-weather.pcm
     */
    public static void writeFile(byte[] data, String filePath) {

        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            //文件不存在则创建文件
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
            fos = new FileOutputStream(file, true);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeBitmap(Bitmap bitmap, String filePath) {
        if(bitmap == null) {
            return;
        }
        FileOutputStream fos = null;
        try {
            //文件不存在则创建文件
            File parentFile = new File(getFileRoot()+"/bitmap/");
            boolean ismkdirs = false;
            if (parentFile != null && !parentFile.exists()) {
                 ismkdirs = parentFile.mkdirs();

            }
            File file = new File(getFileRoot()+"/bitmap/"+filePath);
            if (!file.exists()) {
                file.createNewFile();
            }else{
                return;
            }
            fos = new FileOutputStream(file, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getFileRoot() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = AppGlobals.getApplication().getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return AppGlobals.getApplication().getFilesDir().getAbsolutePath();
    }

    /**
     * 便捷但性能差，而且线程不安全，只用于调试
     * 示例：FileUtil.writeFile(input, "/sdcard/test/1.txt", true);
     *
     * @param content  待保存数据
     * @param filePath 生成文件的绝对路径，例如 /sdcard/aiui/1.txt
     * @param isAppend
     */
    public static void writeStringToTxt(String content, String filePath, Boolean isAppend) {
        try {
            File file = new File(filePath);
            //文件不存在则创建文件
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
            FileOutputStream outputStream = new FileOutputStream(file, isAppend);
            outputStream.write(content.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读取pcm文件
    public static byte[] readPcmFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("file not exit " + filePath);
        }
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        fi.close();
        return buffer;
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public boolean openPcmFile(String filePath) {
        File file = new File(filePath);
        try {
            mFis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mFis = null;
            return false;
        }

        return true;
    }

    public int read(byte[] buffer) {
        if (null != mFis) {
            try {
                return mFis.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                closeReadFile();
                return 0;
            }
        }

        return -1;
    }

    public void closeReadFile() {
        if (null != mFis) {
            try {
                mFis.close();
                mFis = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取asset目录下文件。
     *
     * @return content
     */
    public static String readFile(Context mContext, String file, String code) {
        int len = 0;
        byte[] buf = null;
        String result = "";
        try {
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf, 0, len);

            result = new String(buf, code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void createPcmFile() {
        File dir = new File(WRITE_PCM_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (null != mFos) {
            return;
        }

        DateFormat df = new SimpleDateFormat("MM-dd-hh-mm-ss", Locale.CHINA);
        String filename = df.format(new Date());
        String pcmPath = WRITE_PCM_DIR + filename + PCM_SURFFIX;
        Log.i("SaveAudio", "保存音频地址：" + pcmPath);
        File pcm = new File(pcmPath);
        try {
            if (pcm.createNewFile()) {
                mFos = new FileOutputStream(pcm);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void write(byte[] data) {
        synchronized (FileUtil.this) {
            if (null != mFos) {
                try {
                    mFos.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void write(byte[] data, int offset, int len) {
        synchronized (FileUtil.this) {
            if (null != mFos) {
                try {
                    mFos.write(data, offset, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void closeWriteFile() {
        synchronized (FileUtil.this) {
            if (null != mFos) {
                try {
                    mFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mFos = null;
            }
        }
    }

    //TODO for test
    public static void appendTextToSdcard(String content,String fileName){
        TaskExecutors.get().onIOTask(new Runnable() {
            @Override
            public void run() {
                appendTextToFileInExternalStorage(content,fileName);
            }
        });
    }

    public static void appendTextToFileInExternalStorage(String content, String fileName) {
        File dir = new File("/sdcard/nxlog/");
        if (!dir.exists()){
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
