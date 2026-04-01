package com.noetix.libcore.utils;



import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageUtils {

    public static long getVersionCode(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = AppGlobals.getApplication().getPackageManager().getPackageInfo(AppGlobals.getApplication().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (packageInfo==null) return -1;
        long versionCode;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            versionCode = packageInfo.getLongVersionCode();
        } else {
            versionCode = packageInfo.versionCode;
        }

        return versionCode;
    }
}
