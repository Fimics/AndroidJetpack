package com.mic.libcore.utils;


import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class CPUInfoUtils {

    private static final String TAG="cpu_info";
    private static String mSerial;

    public static String getCPUSerial(String from) {

        if (!TextUtils.isEmpty(mSerial)){
            return mSerial;
        }

        try {
            // 读取 /proc/cpuinfo 文件
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Serial")) {
                    mSerial = line.split(":")[1].trim();
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        KLog.d(TAG,"mSerial->"+mSerial);
        return mSerial;
    }

}
