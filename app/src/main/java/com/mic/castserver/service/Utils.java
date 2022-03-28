package com.mic.castserver.service;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class Utils {
    public static boolean isAppOnForeground(Context context) {

        if (context == null) return false;

        try {
            ActivityManager activityManager = (ActivityManager) context.getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);

            if (activityManager == null) return false;

            String packageName = context.getApplicationContext().getPackageName();
            List<ActivityManager.RunningAppProcessInfo> appProcesses =
                    activityManager.getRunningAppProcesses();
            if (appProcesses == null)
                return false;

            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                // The name of the process that this object is associated with.
                if (appProcess.processName.equals(packageName)
                        && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
