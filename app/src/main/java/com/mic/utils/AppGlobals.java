package com.mic.utils;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import java.lang.reflect.InvocationTargetException;

public class AppGlobals {
    private static Application sApplication;
    private static Boolean sDebuggable;

    public static Application getApplication() {
        if (sApplication == null) {
            try {
                sApplication = (Application) Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication")
                        .invoke(null, (Object[]) null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
               e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }

    public static boolean isDebuggable() {
        if (sDebuggable == null) {
            Application app = getApplication();
            if (app == null) {
                sDebuggable = false;
                return false;
            }

            ApplicationInfo appInfo = app.getApplicationInfo();
            sDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return sDebuggable;
    }

}
