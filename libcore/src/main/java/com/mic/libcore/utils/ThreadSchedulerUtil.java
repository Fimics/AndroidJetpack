package com.mic.libcore.utils;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.os.Process;

public class ThreadSchedulerUtil {
    public static boolean setThreadScheduler(int pid, int policy, int priority) {
        try {
            // 获取Process类的setThreadScheduler方法
            @SuppressLint("SoonBlockedPrivateApi") Method setThreadSchedulerMethod = Process.class.getDeclaredMethod(
                    "setThreadScheduler", int.class, int.class, int.class);

            // 设置方法可访问
            setThreadSchedulerMethod.setAccessible(true);

            // 调用该方法并返回结果
            setThreadSchedulerMethod.invoke(null, pid, policy, priority);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setThreadGroupAndCpuset(int tid, int group) {
        try {
            // 获取Process类的setThreadScheduler方法
            @SuppressLint("SoonBlockedPrivateApi") Method setThreadSchedulerMethod = Process.class.getDeclaredMethod(
                    "setThreadGroupAndCpuset", int.class, int.class);

            // 设置方法可访问
            setThreadSchedulerMethod.setAccessible(true);

            // 调用该方法并返回结果
            setThreadSchedulerMethod.invoke(null, tid, group);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
