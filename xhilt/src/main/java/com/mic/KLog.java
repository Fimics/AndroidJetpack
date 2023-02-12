package com.mic;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Function ：    日志工具类
 * @Description ： 打印
 *                  1、线程名、
 *                  2、类名、
 *                  3、方法名、
 *                  4、源文件行数;
 *                  5、点击行数跳转到源码;
 *                  6、支持格式化json打印。
*/
public class KLog {

    private static String TAG = "klog";
    private static boolean isWriteLog2File = true;
    private static final Boolean DEBUG = true;

    private KLog() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("klog cannot be instantiated");
    }

    /**
     * 初始化
     *
     * @param logTag 全局日志tag
     */
    public static void initTAG(String logTag) {
        TAG = logTag;
    }

    public static void setWriteLog2File(boolean write) {
        isWriteLog2File = write;
    }


    public static void v(String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.i(TAG, msg);
        }
    }


    public static void w(String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.e(TAG, msg);
        }
        if (DEBUG && isWriteLog2File) {
            writeLog2File(TAG, msg);
        }
    }

    // 下面是传入自定义tag的函数
    public static void v(String tag, String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.i(tag, msg);
        }
    }


    public static void w(String tag, String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.e(tag, msg);
        }
        if (DEBUG && isWriteLog2File) {
            writeLog2File(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable th) {
        if (DEBUG) {
            msg = createLog(msg);
            Log.e(tag, msg, th);
        }
        if (DEBUG && isWriteLog2File) {
            writeLog2File(tag, msg);
        }
    }

    public static void json(String tag, String json) {
        if (DEBUG) {
            String msg = formatJson(json);
            json = createLog("\n" + msg);
            Log.i(tag, json);
        }
    }

    private static String createLog(String log) {
//        printThreadStackTrace();
        StackTraceElement LogElement = Thread.currentThread().getStackTrace()[4];
        String fullClassName = LogElement.getClassName();
        String threadName = Thread.currentThread().getName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = LogElement.getMethodName();
        String fileName = LogElement.getFileName();
        int lineNumber = LogElement.getLineNumber();

        StringBuffer buffer = new StringBuffer();
        buffer.append("at ");//链接到源码
        buffer.append("[");
        buffer.append(threadName);
        buffer.append(":");
        buffer.append(className);
        buffer.append(".");
        buffer.append(methodName);
        buffer.append("(");
        buffer.append(fileName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append(")");
        buffer.append("] ");
        buffer.append(log);

        return buffer.toString();
    }

    public static void printThreadStackTrace() {
        //通过线程栈帧元素获取相应信息
        Log.i("KLog", "sElements[0] = " + Thread.currentThread().getStackTrace()[0]);//VMStack
        Log.i("KLog", "sElements[1] = " + Thread.currentThread().getStackTrace()[1]);//Thread
        Log.i("KLog", "sElements[2] = " + Thread.currentThread().getStackTrace()[2]);//当前方法帧元素
        Log.i("KLog", "sElements[3] = " + Thread.currentThread().getStackTrace()[3]);//KLog.x栈元素
        Log.i("KLog", "sElements[4] = " + Thread.currentThread().getStackTrace()[4]);//KLog.x上层调用者
    }


    /**
     * 写日志信息到文件，调试用，日志信息会自动换行.
     * 需要写外部存储权限
     *
     * @param dir
     * @param data
     */
    public synchronized static void writeLog2File(String dir, String data) {
        if (TextUtils.isEmpty(dir)) {
            dir = TAG;
        }
        try {
            String logPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/MyApp/logs";
            File logDir = new File(logPath + "/" + dir);
            if (!logDir.exists()) {
                if (!logDir.mkdirs()) {
                    return;
                }
            }
            String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File file = new File(logDir, fileName + ".txt");
            if (file.exists()) {
                if (file.length() > 200 * 1024) {
                    file.delete();
                    if (!file.createNewFile()) {
                        return;
                    }
                }
            } else {
                if (!file.createNewFile()) {
                    return;
                }
            }

            StringBuilder buffer = new StringBuilder();
            SimpleDateFormat sDateFormatYMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            String dateString = sDateFormatYMD.format(new Date(System.currentTimeMillis()));
            buffer.append(dateString).append("   ").append(data).append("\r\n");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");// "rw" 读写权限
            raf.seek(file.length());
            raf.write(buffer.toString().getBytes());
            raf.close();
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    private static String formatJson(String json){
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }

}

