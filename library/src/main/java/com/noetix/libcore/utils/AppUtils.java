package com.noetix.libcore.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AppUtils {

    // 获取应用的 versionName
    public static String getAppVersionName() {
        try {
            PackageManager packageManager = AppGlobals.getApplication().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(AppGlobals.getApplication().getPackageName(), 0);
            return packageInfo.versionName;  // 返回 versionName
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取应用的 versionCode
    public static int getAppVersionCode() {
        try {
            PackageManager packageManager = AppGlobals.getApplication().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(AppGlobals.getApplication().getPackageName(), 0);
            return packageInfo.versionCode;  // 返回 versionCode
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;  // 如果未找到版本号，返回 -1
        }
    }
}
