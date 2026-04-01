package com.noetix.libcore.utils;


import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ProcessUtils {

    public static boolean isMainProcess(Context context){
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> processInfoList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
            if (processInfo.pid == pid) {
                // 当前进程是主进程
                // 当前进程不是主进程
                return processInfo.processName.equals(context.getPackageName());
            }
        }
        return false;
    }
}
